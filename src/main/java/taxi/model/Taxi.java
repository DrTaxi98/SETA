package taxi.model;

import beans.*;
import com.seta.taxi.RechargeServiceOuterClass.*;
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
import taxi.statistics.SensorReader;
import taxi.statistics.StatisticsComputer;
import utils.SmartCityUtils;
import utils.StringUtils;

import java.io.IOException;
import java.util.Random;

public class Taxi {

    private static final long RIDE_TIME = 5000;
    public static final long RECHARGE_TIME = 10000;

    public enum Status {
        AVAILABLE,
        RIDING,
        TRYING_TO_RECHARGE,
        RECHARGING
    }

    private volatile Status status = Status.AVAILABLE;

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
    private final RidesQueue ridesQueue = new RidesQueue();
    private final RidesConsumer ridesConsumer = new RidesConsumer(this, ridesQueue);
    private final RideElectionsSet rideElectionsSet = new RideElectionsSet();
    private final RechargeClient rechargeClient = new RechargeClient(this);
    private Recharge rechargeRequest = null;
    private final RechargeQueue rechargeQueue = new RechargeQueue();
    private final SlidingWindow slidingWindow = new SlidingWindow();
    private final Simulator pollutionSensor = new PM10Simulator(slidingWindow);
    private final StatisticsComputer statisticsComputer = new StatisticsComputer(this);
    private final SensorReader sensorReader = new SensorReader(statisticsComputer, slidingWindow);
    private final TaxiMqttClient mqttClient = new TaxiMqttClient(ridesQueue);

    private long timestampOffset;

    public Taxi(int id, String ipAddress, int portNumber, String administratorServerAddress) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        restClient = new TaxiRestClient(administratorServerAddress);
        batteryLevel = 100;
        timestampOffset = new Random().nextInt();
    }

    public Status getStatus() {
        return status;
    }

    public synchronized void setStatusAvailable() {
        status = Status.AVAILABLE;
        publishAvailable();
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

    public TaxiBean getTaxiBean() {
        return new TaxiBean(id, ipAddress, portNumber);
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    private void travelKms(double kms) {
        statisticsComputer.addTravelledKms(kms);
        batteryLevel = Math.max(batteryLevel - (int) kms, 0);
        System.out.println(this);

        if (batteryLevel < 30 && status != Status.RECHARGING)
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

    public Recharge getRechargeRequest() {
        return rechargeRequest;
    }

    public void setRechargeRequest(Recharge rechargeRequest) {
        this.rechargeRequest = rechargeRequest;
    }

    public void addRechargeRequest(Recharge rechargeRequest) {
        rechargeQueue.add(rechargeRequest);
    }

    public void waitForRecharged() {
        rechargeQueue.waitForRecharged();
    }

    public long getTimestamp() {
        return System.currentTimeMillis() + timestampOffset;
    }

    public void adjustTimestamp(long thisTimestamp, long otherTimestamp) {
        System.out.println("[Taxi " + id + "] This timestamp: " + thisTimestamp +
                "\nReceived timestamp: " + otherTimestamp);

        long adjustedTimestamp = Math.max(thisTimestamp, otherTimestamp + 1);
        System.out.println("[Taxi " + id + "] Adjusted timestamp: " + adjustedTimestamp);

        long offsetAdjustment = adjustedTimestamp - thisTimestamp;
        timestampOffset += offsetAdjustment;
    }

    public void publishAccomplished(RideRequest rideRequest) {
        mqttClient.publishAccomplished(rideRequest);
    }

    private void publishAvailable() {
        mqttClient.publishAvailable(getDistrict());
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

        System.out.println("Starting sensor reader...");
        sensorReader.start();
        System.out.println("Sensor reader started.");

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
        status = Status.RIDING;

        Position startingPosition = rideRequest.getStartingPosition();
        Position destinationPosition = rideRequest.getDestinationPosition();
        double distanceFromStart = position.distanceFrom(startingPosition);

        try {
            System.out.println(this +
                    "\nAccomplishing " + rideRequest);
            Thread.sleep(RIDE_TIME);
            System.out.println("Ride accomplished.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        position = destinationPosition;

        int startDistrict = startingPosition.getDistrict();
        int destinationDistrict = destinationPosition.getDistrict();
        if (startDistrict != destinationDistrict) {
            mqttClient.unsubscribe();
            mqttClient.subscribe(destinationDistrict);
        }

        statisticsComputer.incrementAccomplishedRides();
        publishAccomplished(rideRequest);
        setStatusAvailable();
        travelKms(distanceFromStart + rideRequest.getDistance());
    }

    public synchronized void tryToRecharge() {
        status = Status.TRYING_TO_RECHARGE;
        rechargeClient.recharge();
    }

    public synchronized void recharge() {
        status = Status.RECHARGING;
        Position rechargingStation = SmartCityUtils.getRechargingStation(position.getDistrict());
        position = rechargingStation;
        travelKms(position.distanceFrom(rechargingStation));

        try {
            System.out.println("Recharging " + this);
            Thread.sleep(RECHARGE_TIME);
            System.out.println("Recharged.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        batteryLevel = 100;
        System.out.println(this);

        rechargeQueue.setRecharged();
        setStatusAvailable();
    }

    public void lamport(int startTaxiId) {
        rechargeClient.lamport(startTaxiId);
    }

    public void sendStatistics(LocalStatistics stats) {
        try {
            System.out.println("Sending statistics...");
            restClient.addStatistics(stats);
            System.out.println("Statistics sent.");
        } catch (RestException e) {
            System.out.println(e.getMessage());
        }
    }

    public void removeOtherTaxiFromServer(int otherTaxiId) {
        try {
            System.out.println("[Taxi " + id + "] Notifying the Administrator Server to remove Taxi " + otherTaxiId);
            restClient.removeTaxi(otherTaxiId);
            System.out.println("[Taxi " + id + "] Taxi " + otherTaxiId + " removed from the Administrator Server.");
        } catch (RestException e) {
            System.out.println(e.getMessage());
        }
    }

    public synchronized void quit() {
        mqttClient.disconnect();

        System.out.println("Shutting down ride consumer...");
        ridesConsumer.shutdown();
        System.out.println("Ride consumer shut down.");

        System.out.println("Shutting down sensor reader...");
        sensorReader.shutdown();
        System.out.println("Sensor reader shut down.");

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
