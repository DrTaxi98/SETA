package taxi.model;

import beans.Position;
import beans.TaxiBean;
import beans.TaxiStartInfo;
import taxi.mqtt.TaxiMqttClient;
import taxi.grpc.TaxiGrpcServer;
import taxi.rest.TaxiRestClient;
import taxi.simulators.PM10Simulator;
import taxi.simulators.Simulator;
import taxi.simulators.SlidingWindow;
import taxi.statistics.StatisticsComputer;
import utils.StringUtils;

import java.util.Set;

public class Taxi {

    private static final int RIDE_TIME = 5000;
    private static final int RECHARGE_TIME = 10000;

    private final int id;
    private final String ipAddress;
    private final int portNumber;
    private int batteryLevel;

    private Position position = null;
    private Set<TaxiBean> otherTaxis = null;

    private final TaxiRestClient restClient;
    private Simulator pollutionSensor = null;
    private StatisticsComputer statisticsComputer = null;
    private TaxiGrpcServer grpcServer = null;
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

    public Set<TaxiBean> getOtherTaxis() {
        return otherTaxis;
    }

    private void setStartInfo(TaxiStartInfo startInfo) {
        position = startInfo.getStartPosition();
        otherTaxis = startInfo.getOtherTaxis();
    }

    public boolean start() {
        if (!register())
            return false;

        startPollutionSensor();
        startStatisticsComputer();

        startGrpcServer();
        presentToOtherTaxis();

        startMqttClient();

        return true;
    }

    private boolean register() {
        TaxiBean taxiBean = new TaxiBean(id, ipAddress, portNumber);
        System.out.println("Registering " + taxiBean);
        TaxiStartInfo startInfo = restClient.addTaxi(taxiBean);

        if (startInfo == null) {
            System.out.println("An error occurred during registration.");
            return false;
        }

        System.out.println("Taxi registered.");
        setStartInfo(startInfo);

        return true;
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

    private void startGrpcServer() {
        grpcServer = new TaxiGrpcServer(portNumber);
        System.out.println("Starting taxi gRPC server...");
        grpcServer.start();
    }

    private void presentToOtherTaxis() {
        System.out.println("Presenting to other taxis...");
        //grpc present
        System.out.println("Presented to other taxis.");
    }

    private void startMqttClient() {
        mqttClient = new TaxiMqttClient();
        System.out.println("Starting MQTT Client...");
        mqttClient.start(position.getDistrict());
    }

    public void accomplishRide(RideRequest ride) {
        try {
            Thread.sleep(RIDE_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int startDistrict = ride.getStartingPosition().getDistrict();
        int destinationDistrict = ride.getDestinationPosition().getDistrict();
        if (startDistrict != destinationDistrict) {
            mqttClient.unsubscribe();
            mqttClient.subscribe(destinationDistrict);
        }
    }

    public void recharge() {
        try {
            Thread.sleep(RECHARGE_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        batteryLevel = 100;
    }

    public void quit() {

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
