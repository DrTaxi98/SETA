package utils;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import taxi.model.RideRequest;

import java.sql.Timestamp;

public class MqttUtils {

    private static final Gson gson = new Gson();

    public static MqttClient connect(String broker, String clientId) throws MqttException {
        MqttClient client = new MqttClient(broker, clientId);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);

        System.out.println(clientId + " Connecting to broker " + broker);
        client.connect(connOpts);
        System.out.println(clientId + " Connected - Thread PID: " + Thread.currentThread().getId());

        return client;
    }

    public static void subscribe(MqttClient client, String subTopic, int subQos) {
        try {
            System.out.println(client.getClientId() + " Subscribing to topic: " + subTopic);
            client.subscribe(subTopic, subQos);
            System.out.println(client.getClientId() + " Subscribed - Thread PID: " + Thread.currentThread().getId());
        } catch (MqttException me) {
            printMqttException(me);
        }
    }

    public static void unsubscribe(MqttClient client, String subTopic) {
        try {
            System.out.println(client.getClientId() + " Unsubscribing from topic: " + subTopic);
            client.unsubscribe(subTopic);
            System.out.println(client.getClientId() + " Unsubscribed - Thread PID: " + Thread.currentThread().getId());
        } catch (MqttException me) {
            MqttUtils.printMqttException(me);
        }
    }

    public static void publish(MqttClient client, MqttMessage message, String pubTopic, int pubQos) {
        message.setQos(pubQos);
        try {
            System.out.println(client.getClientId() + " Publishing message to topic " + pubTopic);
            client.publish(pubTopic, message);
            System.out.println(client.getClientId() + " Message published - Thread PID: " + Thread.currentThread().getId());
        } catch (MqttException me) {
            MqttUtils.printMqttException(me);
        }
    }

    public static void disconnect(MqttClient client) {
        if (client.isConnected()) {
            System.out.println(client.getClientId() + " Disconnecting...");
            try {
                client.disconnect();
                System.out.println(client.getClientId() + " Disconnected - Thread PID: " + Thread.currentThread().getId());
            } catch (MqttException me) {
                MqttUtils.printMqttException(me);
            }
        }
    }

    public static RideRequest rideRequestArrived(String clientId, String topic, MqttMessage message) {
        String time = new Timestamp(System.currentTimeMillis()).toString();
        String payload = new String(message.getPayload());
        RideRequest rideRequest = gson.fromJson(payload, RideRequest.class);

        System.out.println(clientId + " Received a message! - Callback - Thread PID: " +
                Thread.currentThread().getId() +
                "\nTime: " + time +
                "\nTopic: " + topic +
                "\nQoS: " + message.getQos() +
                "\nMessage: " + rideRequest +
                '\n');

        return rideRequest;
    }

    public static void printMqttException(MqttException me) {
        System.out.println("Reason: " + me.getReasonCode());
        System.out.println("Message: " + me.getMessage());
        System.out.println("Localized message: " + me.getLocalizedMessage());
        System.out.println("Cause: " + me.getCause());
        System.out.println("Exception: " + me);
        me.printStackTrace();
    }
}
