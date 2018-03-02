package org.lucapascarella.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.lucapascarella.beans.TestSerializable;
import org.lucapascarella.gerrit.GerritMiner;
import org.lucapscarella.JMSAPI.ProducerImpl;

public class WorkerProducer extends ProducerImpl {
    
    public WorkerProducer(String jmsHost, String jmsPort, String jmsQueue) {
        super(jmsHost, jmsPort, jmsQueue);

    }

    public void onMessage(Message message) {
        try {
            String correlationId = message.getJMSCorrelationID();
            if (message instanceof TextMessage) {
                String requestedMessage = (String) getValue(correlationId);
                String receivedMessage = ((TextMessage) message).getText();
                System.out.println("Request: " + requestedMessage + ". Respose: " + receivedMessage);
            } else if (message instanceof ObjectMessage) {
                TestSerializable requestedObject = (TestSerializable) getValue(correlationId);
                TestSerializable receivedObject = (TestSerializable) ((ObjectMessage) message).getObject();
                System.out.println("Request: " + requestedObject.getString() + ". Respose: " + receivedObject.getString());
            }
            removeKeyFromMap(correlationId);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
