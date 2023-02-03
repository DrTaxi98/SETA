package taxi.model;

import beans.OtherTaxisSet;
import beans.Position;
import beans.TaxiBean;
import beans.TaxiStartInfo;
import exceptions.RestException;
import taxi.grpc.presentation.PresentationClient;
import taxi.grpc.recharge.RechargeClient;
import taxi.grpc.ride.RideClient;
import taxi.mqtt.TaxiMqttClient;
import taxi.grpc.server.TaxiGrpcServer;
import taxi.rest.TaxiRestClient;
import taxi.simulators.PM10Simulator;
import taxi.simulators.Simulator;
import taxi.simulators.SlidingWindow;
import taxi.statistics.StatisticsComputer;
import utils.StringUtils;

import java.io.IOException;

public class Taxi {

    private static final int RIDE_TIME = 5000;
    private static final int RECHARGE_TIME = 10000;

    private final int id;
    private final String ipAddress;
    private int portNumber;
    private int batteryLevel;

    private Position position = null;
    private OtherTaxisSet otherTaxisSet = null;

    private final TaxiRestClient restClient;
    private final TaxiGrpcServer grpcServer = new TaxiGrpcServer(this);
    private final PresentationClient presentationClient = new PresentationClient(this);
    private final RideClient rideClient = new RideClient(this);
    private final RechargeClient rechargeClient = new RechargeClient(this);
    private final Simulator pollutionSensor = new PM10Simulator(new SlidingWindow());
    private final StatisticsComputer statisticsComputer = new StatisticsComputer();
    private final RidesQueue ridesQueue = new RidesQueue();
    private final RideElectionsSet rideElectionsSet = new RideElectionsSet();
    private final RidesConsumer ridesConsumer = new RidesConsumer(this, ridesQueue);
    private final TaxiMqttClient mqttClient = new TaxiMqttClient(ridesQueue);

    private boolean available = true;

    public Taxi(int id, String ipAddress, int portNumber, String administratorServerAddress) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        restClient = new TaxiRestClient(administratorServerAddress);
        batteryLevel = 100;
    }

    public int getId() {
        return id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public String getSocketAddress() {
        return StringUtils.getSocketAddress(ipAddress, portNumber);
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    private void consumeBatteryLevel(int amount) {
        batteryLevel = Math.max(batteryLevel - amount, 0);
    }

    public Position getPosition() {
        return position;
    }

    public boolean isInDistrict(Ride ride) {
        return position.getDistrict() == ride.getStartingPosition().getDistrict();
    }

    public OtherTaxisSet getOtherTaxisSet() {
        return otherTaxisSet;
    }

    public boolean addOtherTaxi(TaxiBean taxi) {
        return otherTaxisSet.add(taxi);
    }

    public TaxiBean getNextTaxi() {
        return otherTaxisSet.getNext(id);
    }

    public boolean isOtherTaxiPresent(int id) {
        return otherTaxisSet.isPresent(id);
    }

    public boolean removeOtherTaxi(int id) {
        return otherTaxisSet.remove(id);
    }

    private void setStartInfo(TaxiStartInfo startInfo) {
        position = startInfo.getStartPosition();
        otherTaxisSet = startInfo.getOtherTaxisSet();
    }

    public RideCriteria getRideCriteria(Ride ride) {
        double distance = position.distanceFrom(ride.getStartingPosition());
        return new RideCriteria(distance, batteryLevel, id);
    }

    public RideElection getRideElection(Ride ride) {
        return rideElectionsSet.getOrAdd(ride, rideClient);
    }

    public RideElection getRideElection(int rideId) {
        return rideElectionsSet.getOrAdd(rideId, rideClient);
    }

    public boolean removeRideElection(RideElection rideElection) {
        return rideElectionsSet.remove(rideElection);
    }

    public boolean isAvailable() {
        return available;
    }

    public void start() throws IOException, RestException {
        portNumber = grpcServer.startServer();

        register();

        System.out.println("Starting pollution sensor...");
        pollutionSensor.start();
        System.out.println("Pollution sensor started.");

        System.out.println("Starting statistics computer...");
        statisticsComputer.start();
        System.out.println("Statistics computer started.");

        System.out.println("Presenting to other taxis...");
        presentationClient.present(otherTaxisSet);
        System.out.println("Presented to other taxis.");

        System.out.println("Starting rides consumer...");
        ridesConsumer.start();
        System.out.println("Rides consumer started.");

        System.out.println("Starting MQTT client...");
        mqttClient.start(position.getDistrict());
    }

    private void register() throws RestException {
        TaxiBean taxiBean = new TaxiBean(id, ipAddress, portNumber);
        System.out.println("Registering " + taxiBean);
        TaxiStartInfo startInfo = restClient.addTaxi(taxiBean);
        System.out.println("Taxi registered.");
        setStartInfo(startInfo);
    }

    public synchronized void accomplishRide(Ride ride) {
        available = false;

        int kms = (int) position.distanceFrom(ride.getStartingPosition());
        consumeBatteryLevel(kms);

        try {
            System.out.println("Accomplishing " + ride);
            Thread.sleep(RIDE_TIME);
            System.out.println("Ride accomplished.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        kms = (int) ride.getDistance();
        consumeBatteryLevel(kms);

        position = ride.getDestinationPosition();
        int startDistrict = ride.getStartingPosition().getDistrict();
        int destinationDistrict = position.getDistrict();
        if (startDistrict != destinationDistrict) {
            mqttClient.unsubscribe();
            ridesQueue.clear();
            mqttClient.subscribe(destinationDistrict);
        }

        available = true;
    }

    public synchronized void recharge() {
        available = false;

        try {
            System.out.println("Recharging...");
            Thread.sleep(RECHARGE_TIME);
            System.out.println("Recharged.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        batteryLevel = 100;
        available = true;
    }

    public synchronized void quit() {
        mqttClient.disconnect();

        System.out.println("Shutting down ride consumer...");
        ridesConsumer.shutdown();
        System.out.println("Ride consumer shut down.");

        System.out.println("Shutting down statistics computer...");
        statisticsComputer.shutdown();
        System.out.println("Statistics computer shut down.");

        System.out.println("Shutting down pollution sensor...");
        pollutionSensor.stopMeGently();
        System.out.println("Pollution sensor shut down.");

        grpcServer.shutdown();

        System.out.println("Notifying the other taxis...");
        presentationClient.notifyQuit(otherTaxisSet);
        System.out.println("Other taxis notified.");

        try {
            System.out.println("Requesting the Administrator Server to leave the smart city...");
            restClient.removeTaxi(id);
            System.out.println("Taxi removed from the Administrator Server.");
        } catch (RestException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "Taxi {" +
                "ID = " + id +
                ", socket address = " + getSocketAddress() + '}' +
                "\n\tBattery level = " + batteryLevel + '%' +
                "\n\tPosition = " + position;
    }
}
