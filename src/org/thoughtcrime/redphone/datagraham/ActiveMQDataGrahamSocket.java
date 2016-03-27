package org.thoughtcrime.redphone.datagraham;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.MissingResourceException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Julian on 3/22/2016.
 */
public class ActiveMQDataGrahamSocket implements DataGrahamSocket {

    protected MqttClient client;
    protected BlockingQueue<byte[]> receivedMessages = new LinkedBlockingQueue<>();
    protected String brokerUrl;
    protected static final String PHONE_TO_DONGLE_TOPIC = "phone_to_dongle1";
    protected static final String DONGLE_TO_PHONE_TOPIC = "dongle_to_phone1";
    protected static final long TIMEOUT = 5000;

    public ActiveMQDataGrahamSocket(){
        super();

        brokerUrl = "tcp://192.168.0.167:1883";
        setupActiveMQ();
    }

    protected void setupActiveMQ(){
        try {
            SecureRandom random = new SecureRandom();
            client = new MqttClient(brokerUrl, new BigInteger(130, random).toString(32));
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
        catch (MissingResourceException e) {
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
            if (client != null) {
                client.disconnect();
                client.close();
            }
        } catch(MqttException e){
            e.printStackTrace();
        }
    }
}
