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
import utils.SmartCityUtils;
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

    private volatile boolean available = true;

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

    private void consumeBatteryLevel(double kms) {
        batteryLevel = (int) Math.max(batteryLevel - kms, 0);
        if (batteryLevel < 30)
            tryToRecharge();
    }

    public Position getPosition() {
        return position;
    }

    public int getDistrict() {
        return position.getDistrict();
    }

    public boolean isInDistrict(RideRequest rideRequest) {
        return getDistrict() == rideRequest.getStartingPosition().getDistrict();
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

    public boolean isOtherTaxiAbsent(int id) {
        return !otherTaxisSet.isPresent(id);
    }

    public boolean removeOtherTaxi(int id) {
        return otherTaxisSet.remove(id);
    }

    private void setStartInfo(TaxiStartInfo startInfo) {
        position = startInfo.getStartPosition();
        otherTaxisSet = startInfo.getOtherTaxisSet();
    }

    public RideCriteria getRideCriteria(RideRequest rideRequest) {
        double distance = position.distanceFrom(rideRequest.getStartingPosition());
        return new RideCriteria(distance, batteryLevel, id);
    }

    public RideElection getRideElection(RideRequest rideRequest) {
        return rideElectionsSet.getOrAdd(rideRequest, rideClient);
    }

    public boolean isPresentRideElection(RideRequest rideRequest) {
        return rideElectionsSet.contains(rideRequest);
    }

    public void removeRideElection(RideElection rideElection) {
        rideElectionsSet.remove(rideElection);
    }

    public boolean isAvailable() {
        return available;
    }

    private synchronized void setAvailable(boolean available) {
        this.available = available;
        if (available)
            publishAvailability();
    }

    public void publishRide(RideRequest rideRequest) {
        mqttClient.publishRide(rideRequest);
    }

    private void publishAvailability() {
        mqttClient.publishAvailability(getDistrict());
    }

    public void start() throws IOException, RestException {
        portNumber = grpcServer.startServer();

        try {
            register();
        } catch (RestException e) {
            System.out.println(e.getMessage());
            grpcServer.shutdown();
            throw e;
        }

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
        mqttClient.start(getDistrict());
    }

    private void register() throws RestException {
        TaxiBean taxiBean = new TaxiBean(id, ipAddress, portNumber);
        System.out.println("Registering " + taxiBean);
        TaxiStartInfo startInfo = restClient.addTaxi(taxiBean);
        System.out.println("Taxi registered.");
        setStartInfo(startInfo);
    }

    public synchronized void accomplishRide(RideRequest rideRequest) {
        setAvailable(false);

        try {
            System.out.println(this +
                    "\nAccomplishing " + rideRequest);
            Thread.sleep(RIDE_TIME);
            System.out.println("Ride accomplished.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Position startingPosition = rideRequest.getStartingPosition();
        Position destinationPosition = rideRequest.getDestinationPosition();

        double kms = position.distanceFrom(startingPosition) + rideRequest.getDistance();
        consumeBatteryLevel(kms);

        position = destinationPosition;
        System.out.println(this);

        int startDistrict = startingPosition.getDistrict();
        int destinationDistrict = destinationPosition.getDistrict();
        if (startDistrict != destinationDistrict) {
            mqttClient.unsubscribe();
            mqttClient.subscribe(destinationDistrict);
        }

        setAvailable(true);
    }

    public synchronized void tryToRecharge() {
        setAvailable(false);
        rechargeClient.recharge();
    }

    public synchronized void recharge() {
        Position rechargingStation = SmartCityUtils.getRechargingStation(position.getDistrict());
        double kms = position.distanceFrom(rechargingStation);
        consumeBatteryLevel(kms);
        position = rechargingStation;

        try {
            System.out.println("Recharging " + this);
            Thread.sleep(RECHARGE_TIME);
            System.out.println("Recharged.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        batteryLevel = 100;
        System.out.println(this);

        rechargeClient.stopRecharging();
        setAvailable(true);
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
