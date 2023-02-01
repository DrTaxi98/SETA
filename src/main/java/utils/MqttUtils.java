package utils;

import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttUtils {

    public static void printMqttException(MqttException me) {
        System.out.println("Reason: " + me.getReasonCode());
        System.out.println("Message: " + me.getMessage());
        System.out.println("Localized message: " + me.getLocalizedMessage());
        System.out.println("Cause: " + me.getCause());
        System.out.println("Exception: " + me);
        me.printStackTrace();
    }
}
