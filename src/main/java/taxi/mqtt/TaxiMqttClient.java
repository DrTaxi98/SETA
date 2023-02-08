package taxi.mqtt;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;
import taxi.model.RideRequest;
import taxi.model.RidesQueue;
import utils.MqttUtils;

public class TaxiMqttClient {

    private static final String BROKER = "tcp://localhost:1883";
    private static final String SUB_TOPIC_PREFIX = "seta/smartcity/rides/district";
    private static final int SUB_QOS = 2;
    private static final String PUB_ACCOMPLISHED_TOPIC = "seta/smartcity/rides/accomplished";
    private static final String PUB_AVAILABLE_TOPIC = "seta/smartcity/taxis/available";
    private static final int PUB_QOS = 2;

    private MqttClient client;
    private final String clientId = MqttClient.generateClientId();
    private int district = 0;
    private final Gson gson = new Gson();

    private final RidesQueue ridesQueue;

    public TaxiMqttClient(RidesQueue ridesQueue) {
        this.ridesQueue = ridesQueue;
    }

    private String getSubTopic() {
        return SUB_TOPIC_PREFIX + district;
    }

    public void start(int district) {
        try {
            client = MqttUtils.connect(BROKER, clientId);

            client.setCallback(new MqttCallback() {

                public void messageArrived(String topic, MqttMessage message) {
                    RideRequest rideRequest = MqttUtils.rideRequestArrived(clientId, topic, message);
                    ridesQueue.put(rideRequest);
                }

                public void connectionLost(Throwable cause) {
                    System.out.println(clientId + " Connection lost! cause:" + cause.getMessage() +
                            " - Thread PID: " + Thread.currentThread().getId());
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                    if (token.isComplete())
                        System.out.println(clientId + " Message delivered - Thread PID: " +
                                Thread.currentThread().getId());
                }
            });

            subscribe(district);
        } catch (MqttException me) {
            MqttUtils.printMqttException(me);
        }
    }

    public void subscribe(int district) {
        this.district = district;
        MqttUtils.subscribe(client, getSubTopic(), SUB_QOS);
    }

    public void unsubscribe() {
        MqttUtils.unsubscribe(client, getSubTopic());
    }

    public void publishAccomplished(RideRequest rideRequest) {
        String payload = gson.toJson(rideRequest);
        MqttMessage message = new MqttMessage(payload.getBytes());

        System.out.println("Publishing accomplished " + rideRequest);
        MqttUtils.publish(client, message, PUB_ACCOMPLISHED_TOPIC, PUB_QOS);
    }

    public void publishAvailable(int district) {
        String payload = gson.toJson(district);
        MqttMessage message = new MqttMessage(payload.getBytes());

        System.out.println("Publishing availability in district: " + district);
        MqttUtils.publish(client, message, PUB_AVAILABLE_TOPIC, PUB_QOS);
    }

    public void disconnect() {
        MqttUtils.disconnect(client);
    }
}
