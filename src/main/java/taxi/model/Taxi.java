package taxi.model;

import beans.OtherTaxisSet;
import beans.Position;
import beans.TaxiBean;
import beans.TaxiStartInfo;
import exceptions.RestException;
import taxi.grpc.TaxiGrpcClient;
import taxi.mqtt.TaxiMqttClient;
import taxi.grpc.TaxiGrpcServer;
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
    private OtherTaxisSet otherTaxis = null;

    private final TaxiRestClient restClient;
    private Simulator pollutionSensor = null;
    private StatisticsComputer statisticsComputer = null;
    private TaxiGrpcServer grpcServer = null;
    private TaxiGrpcClient grpcClient = null;
    private RidesQueue ridesQueue = null;
    private RidesConsumer ridesConsumer = null;
    private TaxiMqttClient mqttClient = null;

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

    public void consumeBatteryLevel(int amount) {
        batteryLevel = Math.max(batteryLevel - amount, 0);
    }

    public Position getPosition() {
        return position;
    }

    public OtherTaxisSet getOtherTaxis() {
        return otherTaxis;
    }

    private void setStartInfo(TaxiStartInfo startInfo) {
        position = startInfo.getStartPosition();
        otherTaxis = startInfo.getOtherTaxis();
    }

    public boolean addOtherTaxi(TaxiBean taxi) {
        return otherTaxis.add(taxi);
    }

    public boolean removeOtherTaxi(int id) {
        return otherTaxis.remove(id);
    }

    public void start() throws IOException, RestException {
        startGrpcServer();
        register();

        startPollutionSensor();
        startStatisticsComputer();

        presentToOtherTaxis();

        startRidesConsumer();
        startMqttClient();
    }

    private void startGrpcServer() throws IOException {
        grpcServer = new TaxiGrpcServer(this);
        portNumber = grpcServer.startServer();
    }

    private void register() throws RestException {
        TaxiBean taxiBean = new TaxiBean(id, ipAddress, portNumber);
        System.out.println("Registering " + taxiBean);
        TaxiStartInfo startInfo = restClient.addTaxi(taxiBean);
        System.out.println("Taxi registered.");
        setStartInfo(startInfo);
    }

    private void startPollutionSensor() {
        pollutionSensor = new PM10Simulator(new SlidingWindow());
        System.out.println("Starting pollution sensor...");
        pollutionSensor.start();
        System.out.println("Pollution sensor started.");
    }

    private void startStatisticsComputer() {
        statisticsComputer = new StatisticsComputer();
        System.out.println("Starting statistics computer...");
        statisticsComputer.start();
        System.out.println("Statistics computer started.");
    }

    private void presentToOtherTaxis() {
        grpcClient = new TaxiGrpcClient(this);
        System.out.println("Presenting to other taxis...");
        grpcClient.present(otherTaxis);
        System.out.println("Presented to other taxis.");
    }

    private void startRidesConsumer() {
        ridesQueue = new RidesQueue();
        ridesConsumer = new RidesConsumer(this, ridesQueue);
        System.out.println("Starting rides consumer...");
        ridesConsumer.start();
        System.out.println("Rides consumer started.");
    }

    private void startMqttClient() {
        mqttClient = new TaxiMqttClient(ridesQueue);
        System.out.println("Starting MQTT client...");
        mqttClient.start(position.getDistrict());
    }

    public synchronized void startElection(RideRequest ride) {
        grpcClient.startElection(ride);
    }

    public synchronized void accomplishRide(RideRequest ride) {
        try {
            Thread.sleep(RIDE_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        position = ride.getDestinationPosition();
        int startDistrict = ride.getStartingPosition().getDistrict();
        int destinationDistrict = position.getDistrict();
        if (startDistrict != destinationDistrict) {
            mqttClient.unsubscribe();
            mqttClient.subscribe(destinationDistrict);
        }
    }

    public synchronized void recharge() {
        try {
            Thread.sleep(RECHARGE_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        batteryLevel = 100;
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
        grpcClient.notifyQuit(otherTaxis);
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
