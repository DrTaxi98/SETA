package taxi.mqtt;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;
import taxi.model.RideRequest;
import utils.MqttUtils;

import java.sql.Timestamp;

public class TaxiMqttClient {

    private static final String BROKER = "tcp://localhost:1883";
    private static final String SUB_TOPIC_PREFIX = "seta/smartcity/rides/district";
    private static final int SUB_QOS = 2;

    private MqttClient client;
    private final String clientId = MqttClient.generateClientId();
    private int district = 0;
    private final Gson gson = new Gson();

    private String getSubTopic() {
        return SUB_TOPIC_PREFIX + district;
    }

    public void start(int district) {
        try {
            client = new MqttClient(BROKER, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            System.out.println(clientId + " Connecting to broker " + BROKER);
            client.connect(connOpts);
            System.out.println(clientId + " Connected - Thread PID: " + Thread.currentThread().getId());

            client.setCallback(new MqttCallback() {

                public void messageArrived(String topic, MqttMessage message) {
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
                }

                public void connectionLost(Throwable cause) {
                    System.out.println(clientId + " Connection lost! cause:" + cause.getMessage() +
                            " - Thread PID: " + Thread.currentThread().getId());
                }

                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            subscribe(district);
        } catch (MqttException me) {
            MqttUtils.printMqttException(me);
        }
    }

    public void subscribe(int district) {
        this.district = district;
        String subTopic = getSubTopic();
        System.out.println(clientId + " Subscribing to topic: " + subTopic);
        try {
            client.subscribe(subTopic, SUB_QOS);
            System.out.println(clientId + " Subscribed - Thread PID: " + Thread.currentThread().getId());
        } catch (MqttException me) {
            MqttUtils.printMqttException(me);
        }
    }

    public void unsubscribe() {
        String subTopic = getSubTopic();
        System.out.println(clientId + " Unsubscribing from topic: " + subTopic);
        try {
            client.unsubscribe(subTopic);
            System.out.println(clientId + " Unsubscribed - Thread PID: " + Thread.currentThread().getId());
        } catch (MqttException me) {
            MqttUtils.printMqttException(me);
        }
    }

    public void disconnect() {
        if (client.isConnected()) {
            System.out.println(clientId + " Disconnecting...");
            try {
                client.disconnect();
                System.out.println(clientId + " Disconnected - Thread PID: " + Thread.currentThread().getId());
            } catch (MqttException me) {
                MqttUtils.printMqttException(me);
            }
        }
    }
}
