package seta;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;
import taxi.model.RideRequest;
import utils.MqttUtils;
import utils.SmartCityUtils;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Seta {

    private static final String BROKER = "tcp://localhost:1883";
    private static final String CLIENT_ID = MqttClient.generateClientId();
    private static final String PUB_TOPIC_PREFIX = "seta/smartcity/rides/district";
    private static final int PUB_QOS = 2;
    private static final String SUB_ACCOMPLISHED_TOPIC = "seta/smartcity/rides/accomplished";
    private static final String SUB_AVAILABILE_TOPIC = "seta/smartcity/taxis/available";
    private static final int SUB_QOS = 2;
    private static int RIDE_ID = 0;
    private static final Gson gson = new Gson();

    private static final Map<Integer, RetainedRidesQueue> retainedRides = new HashMap<>(SmartCityUtils.DISTRICTS);

    public static void main(String[] args) {
        try {
            MqttClient client = MqttUtils.connect(BROKER, CLIENT_ID);

            client.setCallback(new MqttCallback() {

                public void messageArrived(String topic, MqttMessage message) {
                    if (topic.equals(SUB_ACCOMPLISHED_TOPIC)) {
                        RideRequest rideRequest = MqttUtils.rideRequestArrived(CLIENT_ID, topic, message);
                        int district = rideRequest.getStartingPosition().getDistrict();
                        retainedRides.get(district).remove(rideRequest);
                        printRetainedRides();
                    }
                    else {
                        String time = new Timestamp(System.currentTimeMillis()).toString();
                        String payload = new String(message.getPayload());
                        int district = gson.fromJson(payload, Integer.class);

                        System.out.println(CLIENT_ID + " Received a message! - Callback - Thread PID: " +
                                Thread.currentThread().getId() +
                                "\nTime: " + time +
                                "\nTopic: " + topic +
                                "\nQoS: " + message.getQos() +
                                "\nMessage: District " + district +
                                '\n');

                        RetainedRidesQueue retainedRidesQueue = retainedRides.get(district);
                        System.out.println("District " + district + ' ' + retainedRidesQueue);
                        RideRequest rideRequest = retainedRidesQueue.getFirst();
                        if (rideRequest != null)
                            publish(client, rideRequest);
                    }
                }

                public void connectionLost(Throwable cause) {
                    System.out.println(CLIENT_ID + " Connection lost! Cause: " + cause.getMessage() +
                            " - Thread PID: " + Thread.currentThread().getId());
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                    if (token.isComplete())
                        System.out.println(CLIENT_ID + " Message delivered - Thread PID: " +
                                Thread.currentThread().getId());
                }
            });

            for (int i = 0; i < SmartCityUtils.DISTRICTS; i++)
                retainedRides.put(i + 1, new RetainedRidesQueue());

            MqttUtils.subscribe(client, SUB_ACCOMPLISHED_TOPIC, SUB_QOS);
            MqttUtils.subscribe(client, SUB_AVAILABILE_TOPIC, SUB_QOS);

            while (client.isConnected()) {
                for (int i = 0; i < 2; i++)
                    generateAndPublish(client);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            MqttUtils.disconnect(client);
        } catch (MqttException me) {
            MqttUtils.printMqttException(me);
        }
    }

    private static void generateAndPublish(MqttClient client) {
        RideRequest rideRequest = SmartCityUtils.randomRide(RIDE_ID++);
        System.out.println(CLIENT_ID + " Generated " + rideRequest);

        int district = rideRequest.getStartingPosition().getDistrict();
        retainedRides.get(district).add(rideRequest);

        publish(client, rideRequest);
    }

    private static void publish(MqttClient client, RideRequest rideRequest) {
        String payload = gson.toJson(rideRequest);
        MqttMessage message = new MqttMessage(payload.getBytes());
        String pubTopic = PUB_TOPIC_PREFIX + rideRequest.getStartingPosition().getDistrict();

        System.out.println(CLIENT_ID + " Publishing " + rideRequest);
        MqttUtils.publish(client, message, pubTopic, PUB_QOS);
    }

    private static void printRetainedRides() {
        retainedRides.forEach((dist, retainedRidesQueue) ->
                System.out.println("District " + dist + ' ' + retainedRidesQueue + '\n'));
    }
}
