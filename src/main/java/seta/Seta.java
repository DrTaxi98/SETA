package seta;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;
import taxi.model.RideRequest;
import utils.MqttUtils;
import utils.SmartCityUtils;

public class Seta {

    private static final String broker = "tcp://localhost:1883";
    private static final String clientId = MqttClient.generateClientId();
    private static final String pubTopicPrefix = "seta/smartcity/rides/district";
    private static final int pubQos = 2;
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        MqttClient client;

        try {
            client = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            System.out.println(clientId + " Connecting to broker " + broker);
            client.connect(connOpts);
            System.out.println(clientId + " Connected - Thread PID: " + Thread.currentThread().getId());

            client.setCallback(new MqttCallback() {

                public void messageArrived(String topic, MqttMessage message) {}

                public void connectionLost(Throwable cause) {
                    System.out.println(clientId + " Connection lost! Cause: " + cause.getMessage() +
                            " - Thread PID: " + Thread.currentThread().getId());
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                    if (token.isComplete())
                        System.out.println(clientId + " Message delivered - Thread PID: " +
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
            System.out.println(clientId + " Disconnected - Thread PID: " + Thread.currentThread().getId());
        } catch (MqttException me) {
            MqttUtils.printMqttException(me);
        }
    }

    private static void generateAndPublish(MqttClient client) throws MqttException {
        RideRequest rideRequest = SmartCityUtils.randomRideRequest();
        System.out.println(clientId + " Generated " + rideRequest);

        String payload = gson.toJson(rideRequest);
        MqttMessage message = new MqttMessage(payload.getBytes());

        String pubTopic = pubTopicPrefix + rideRequest.getStartingPosition().getDistrict();
        message.setQos(pubQos);
        System.out.println(clientId + " Publishing message to topic " + pubTopic);
        client.publish(pubTopic, message);
        System.out.println(clientId + " Message published - Thread PID: " + Thread.currentThread().getId());
    }
}
