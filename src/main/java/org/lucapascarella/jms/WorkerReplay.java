package org.lucapascarella.jms;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import org.lucapascarella.beans.GerritBean;
import org.lucapascarella.gerrit.GerritMiner;
import org.lucapscarella.JMSAPI.ConsumerImpl;

import com.google.gerrit.extensions.restapi.RestApiException;

public class WorkerReplay extends ConsumerImpl {

    private GerritMiner gerritMiner;
    
    public WorkerReplay(String host, String port, String queueName) throws JMSException {
        super(host, Integer.parseInt(port), queueName);
    }

    public WorkerReplay(String host, int port, String queueName) throws JMSException {
        super(host, port, queueName);
    }
    
    public void setGerritMiner(GerritMiner gerritMiner) {
        this.gerritMiner = gerritMiner;
    }

    public void onMessage(Message message) {
        try {
            Message response = null;
            if (message instanceof TextMessage) {
                TextMessage txtMsg = (TextMessage) message;
                String messageText = txtMsg.getText();
                System.out.println("Message received: " + messageText);
                TimeUnit.SECONDS.sleep(Integer.parseInt(messageText));
                response = (TextMessage) session.createTextMessage();
                ((TextMessage) response).setText(messageProtocol.handleProtocolMessage(messageText));
            } else if (message instanceof ObjectMessage) {
                GerritBean gb = (GerritBean) ((ObjectMessage) message).getObject();
                startMiner(gb);
                gb.setFinish("Mining finished");
                response = (ObjectMessage) session.createObjectMessage();
                ((ObjectMessage) response).setObject(gb);
            }
            // Set the correlation ID from the received message to be the correlation id of the response message
            // this lets the client identify which message this is a response to if it has more than
            // one outstanding message to the server
            response.setJMSCorrelationID(message.getJMSCorrelationID());
            // Send the response to the Destination specified by the JMSReplyTo field of the received message,
            // this is presumably a temporary queue created by the client
            replyProducer.send(message.getJMSReplyTo(), response);
        } catch (JMSException e) {
            // Handle the exception appropriately
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private void startMiner(GerritBean gb) {
        try {
            gerritMiner.start(gb.getGerritUrl(), Integer.parseInt(gb.getGerritStart()), Integer.parseInt(gb.getGerritStop()));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (RestApiException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
