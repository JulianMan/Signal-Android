package org.thoughtcrime.redphone.datagraham;

import com.soundcloud.android.crop.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Julian on 3/22/2016.
 */
public class ActiveMQDataGrahamSocket implements DataGrahamSocket {

    protected MqttClient client = null;
    protected BlockingQueue<byte[]> receivedMessages = new LinkedBlockingQueue<>();
    protected String brokerUrl = "tcp://192.168.0.187:1883";
    protected static final String PHONE_TO_DONGLE_TOPIC = "best_phone_to_dongle_topic";
    protected static final String DONGLE_TO_PHONE_TOPIC = "best_dongle_to_phone_topic";
    protected MemoryPersistence persistence = new MemoryPersistence();

    public ActiveMQDataGrahamSocket(){
        super();

        setupActiveMQ();
    }

    protected void setupActiveMQ(){
        try {
            client = new MqttClient(brokerUrl,"ImaPhone2", persistence);
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
        } catch(MqttException e){
            Log.e("MqttException", e);
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
            if(client != null) {
                client.disconnect();
                client.close();
            }
        } catch(MqttException e){
            e.printStackTrace();
        }
    }
}
