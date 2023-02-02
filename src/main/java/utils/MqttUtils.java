package utils;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttUtils {

    public static MqttClient connect(String broker, String clientId) throws MqttException {
        MqttClient client = new MqttClient(broker, clientId);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);

        System.out.println(clientId + " Connecting to broker " + broker);
        client.connect(connOpts);
        System.out.println(clientId + " Connected - Thread PID: " + Thread.currentThread().getId());

        return client;
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
