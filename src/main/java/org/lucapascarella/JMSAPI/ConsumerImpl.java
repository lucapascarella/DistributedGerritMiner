package org.lucapascarella.JMSAPI;

import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

public abstract class ConsumerImpl implements MessageListener {

    private Connection connection;
    protected Session session;
    protected MessageProducer replyProducer;
    protected MessageProtocol messageProtocol;

    public ConsumerImpl(String host, int port, String queueName) throws JMSException {
        messageProtocol = new MessageProtocol();

        String url = "tcp://" + host + ":" + port + "?jms.prefetchPolicy.all=1";
        // String url = "tcp://192.168.0.100:61616?jms.prefetchPolicy.all=1";
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        // Set this option true if you want to receive serialized Java object
        connectionFactory.setTrustAllPackages(true);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue adminQueue = session.createQueue(queueName);

        // Setup a message producer to respond to messages from clients, we will get the destination
        // to send to from the JMSReplyTo header field from a Message
        replyProducer = session.createProducer(null);
        replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        // Set up a consumer to consume messages off of the admin queue
        MessageConsumer consumer = session.createConsumer(adminQueue);
        consumer.setMessageListener(this);
    }

    public void startAndWait() {
        try {
            connection.start();
            while (true) {
                TimeUnit.MINUTES.sleep(1);
                // receive(); blocks indefinitely until a message is produced or until this message consumer is closed
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void close() throws JMSException {
        connection.close();
    }

}
