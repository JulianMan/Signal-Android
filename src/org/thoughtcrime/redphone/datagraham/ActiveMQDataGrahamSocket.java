package org.thoughtcrime.redphone.datagraham;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Julian on 3/22/2016.
 */
public class ActiveMQDataGrahamSocket implements DataGrahamSocket {

    protected MqttClient client;
    protected BlockingQueue<byte[]> receivedMessages = new LinkedBlockingQueue<>();
    protected String brokerUrl;
    protected static final String PHONE_TO_DONGLE_TOPIC = "phone_to_dongle";
    protected static final String DONGLE_TO_PHONE_TOPIC = "dongle_to_phone";
    protected static final long TIMEOUT = 5000;

    public ActiveMQDataGrahamSocket(){
        super();

        brokerUrl = "tcp://localhost:1883";
        setupActiveMQ();
    }

    protected void setupActiveMQ(){
        try {
            client = new MqttClient(brokerUrl,"ImaPhone");
            client.connect();
            client.subscribe(DONGLE_TO_PHONE_TOPIC);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    //be sad
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    receivedMessages.offer(message.getPayload());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //no-op
                }
            });
        }
        catch(MqttException e){
            e.printStackTrace();
        }
    }

    @Override
    public void send(byte[] data) {
        try {
            client.publish(PHONE_TO_DONGLE_TOPIC, data, 2, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] receive() {
        try {
            return receivedMessages.take();
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close(){
        try {
            client.disconnect();
            client.close();
        } catch(MqttException e){
            e.printStackTrace();
        }
    }
}
