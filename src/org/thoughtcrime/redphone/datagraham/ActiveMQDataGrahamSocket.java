package org.thoughtcrime.redphone.datagraham;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.JMSException;
import javax.jms.Topic;

/**
 * Created by Julian on 3/22/2016.
 */
public class ActiveMQDataGrahamSocket extends DataGrahamSocket {

    protected Connection connection;
    protected Session session;
    protected MessageConsumer consumer;
    protected MessageProducer producer;
    protected String brokerUrl;
    protected static final String PHONE_TO_DONGLE_TOPIC = "phone_to_dongle";
    protected static final String DONGLE_TO_PHONE_TOPIC = "dongle_to_phone";
    protected static final long TIMEOUT = 5000;

    public ActiveMQDataGrahamSocket(){
        super();

        brokerUrl = "tcp://localhost:61616";
        setupActiveMQ();
    }

    protected void setupActiveMQ(){
        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic phoneToDongleTopic = session.createTopic(PHONE_TO_DONGLE_TOPIC);
            Topic dongleToPhoneTopic = session.createTopic(DONGLE_TO_PHONE_TOPIC);
            consumer = session.createConsumer(dongleToPhoneTopic);
            producer = session.createProducer(phoneToDongleTopic);
        } catch(JMSException e){
            e.printStackTrace();
        }
    }

    @Override
    public void send(byte[] data) {

        try {
            BytesMessage msg = session.createBytesMessage();
            msg.writeBytes(data);
            producer.send(msg);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] receive() {
        try {
            Message message = consumer.receive();
            if(message instanceof BytesMessage){
                byte[] bytes = new byte[(int)((BytesMessage) message).getBodyLength()];
                ((BytesMessage) message).readBytes(bytes);
                return bytes;
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close(){
        try {
            consumer.close();
            producer.close();
            session.close();
            connection.close();
        } catch(JMSException e){
            e.printStackTrace();
        }
    }
}
