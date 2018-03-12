package org.lucapscarella.JMSAPI;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;


public abstract class ProducerImpl implements Producer, MessageListener {

    private Random random;
    private Session session;
    private Queue queue, tempQueue;
    private Connection connection;
    private MessageProducer producer;
    private Map<String, Serializable> map;

    public ProducerImpl(String jmsHost, String jmsPort, String jmsQueue) {
        try {
            random = new Random(System.currentTimeMillis());
            String url = "tcp://" + jmsHost + ":" + jmsPort;
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
            // Set this option true if you want to receive serialized Java object
            connectionFactory.setTrustAllPackages(true);
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            queue = session.createQueue(jmsQueue);

            // Setup a message producer to send message to the queue the server is consuming from
            producer = session.createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            // Create a temporary queue that this client will listen for responses on then create a consumer
            // that consumes message from this temporary queue...for a real application a client should reuse
            // the same temp queue for each message to the server...one temp queue per client
            tempQueue = session.createTemporaryQueue();
            MessageConsumer responseConsumer = session.createConsumer(tempQueue);

            // Create hash map to store client requests
            map = new HashMap<String, Serializable>();

            // This class will handle the messages to the temp queue as well
            responseConsumer.setMessageListener(this);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    // /**
    // * This method return the number of pending messages in the queue
    // *
    // * @return number of messages in the queue
    // */
    // @SuppressWarnings("unchecked")
    // public int getPendingElements() {
    // int pending = 0;
    // Enumeration<QueueBrowser> enu;
    // try {
    // enu = browser.getEnumeration();
    // while (enu.hasMoreElements()) {
    // enu.nextElement();
    // pending++;
    // }
    // } catch (JMSException e) {
    // e.printStackTrace();
    // }
    // return pending;
    // }

    /**
     * This method produce and send a text message to worker
     * 
     * @param text
     * @return true is success
     */
    public boolean sendTextMessage(String text) {
        TextMessage txtMessage;
        try {
            // Create the actual message you want to send
            txtMessage = session.createTextMessage();
            txtMessage.setText(text);
            // Set the reply to field to the temp queue you created above, this is the queue the server will respond to
            txtMessage.setJMSReplyTo(tempQueue);
            // Set a correlation ID so when you get a response you know which sent message the response is for
            // If there is never more than one outstanding message to the server then the
            // same correlation ID can be used for all the messages...if there is more than one outstanding
            // message to the server you would presumably want to associate the correlation ID with this
            // message somehow...a Map works good
            String correlationId = createRandomString();
            txtMessage.setJMSCorrelationID(addKeyToMap(correlationId, text));
            producer.send(txtMessage);
            return true;
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * This method produce and send an object message to worker
     * 
     * @param text
     * @return true is success
     */
    public boolean sendObject(Serializable obj) {
        ObjectMessage objMessage;
        try {
            // Create the actual message you want to send
            objMessage = session.createObjectMessage();
            objMessage.setObject(obj);
            // Set the reply to field to the temp queue you created above, this is the queue the server will respond to
            objMessage.setJMSReplyTo(tempQueue);
            // Set a correlation ID so when you get a response you know which sent message the response is for
            // If there is never more than one outstanding message to the server then the
            // same correlation ID can be used for all the messages...if there is more than one outstanding
            // message to the server you would presumably want to associate the correlation ID with this
            // message somehow...a Map works good
            String correlationId = createRandomString();
            objMessage.setJMSCorrelationID(addKeyToMap(correlationId, obj));
            producer.send(objMessage);
            return true;
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String createRandomString() {
        long randomLong = random.nextLong();
        return Long.toHexString(randomLong);
    }

    private synchronized String addKeyToMap(String key, Serializable obj) {
        while (map.get(key) != null) {
            long l = Long.parseLong(key, 16);
            l++;
            key = Long.toHexString(l);
        }
        map.put(key, obj);
        return key;
    }

    protected synchronized boolean isMapEmpty() {
        return map.isEmpty();
    }

    protected synchronized int mapSize() {
        return map.size();
    }
    
    protected synchronized Serializable getValue(String key) {
        return map.get(key);
    }

    protected synchronized void removeKeyFromMap(String key) {
        map.remove(key);
    }

    public void close() throws JMSException {
        connection.close();
    }

    public void waitTermination() {
        try {
            while (!isMapEmpty())
                TimeUnit.SECONDS.sleep(1);
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
