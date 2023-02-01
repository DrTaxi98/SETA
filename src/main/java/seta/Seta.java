package seta;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;
import taxi.model.RideRequest;
import utils.MqttUtils;
import utils.SmartCityUtils;

public class Seta {

    private static final String BROKER = "tcp://localhost:1883";
    private static final String CLIENT_ID = MqttClient.generateClientId();
    private static final String PUB_TOPIC_PREFIX = "seta/smartcity/rides/district";
    private static final int PUB_QOS = 2;
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        MqttClient client;

        try {
            client = new MqttClient(BROKER, CLIENT_ID);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            System.out.println(CLIENT_ID + " Connecting to broker " + BROKER);
            client.connect(connOpts);
            System.out.println(CLIENT_ID + " Connected - Thread PID: " + Thread.currentThread().getId());

            client.setCallback(new MqttCallback() {

                public void messageArrived(String topic, MqttMessage message) {}

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

            while (client.isConnected()) {
                for (int i = 0; i < 2; i++)
                    generateAndPublish(client);

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (client.isConnected())
                client.disconnect();
            System.out.println(CLIENT_ID + " Disconnected - Thread PID: " + Thread.currentThread().getId());
        } catch (MqttException me) {
            MqttUtils.printMqttException(me);
        }
    }

    private static void generateAndPublish(MqttClient client) throws MqttException {
        RideRequest rideRequest = SmartCityUtils.randomRideRequest();
        System.out.println(CLIENT_ID + " Generated " + rideRequest);

        String payload = gson.toJson(rideRequest);
        MqttMessage message = new MqttMessage(payload.getBytes());

        String pubTopic = PUB_TOPIC_PREFIX + rideRequest.getStartingPosition().getDistrict();
        message.setQos(PUB_QOS);
        System.out.println(CLIENT_ID + " Publishing message to topic " + pubTopic);
        client.publish(pubTopic, message);
        System.out.println(CLIENT_ID + " Message published - Thread PID: " + Thread.currentThread().getId());
    }
}
