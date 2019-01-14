package org.lucapascarella.gerrit;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.lucapascarella.JMSAPI.ConsumerImpl;
import org.lucapascarella.beans.MineRequest;
import org.lucapascarella.beans.MinedResults;
import org.lucapascarella.db.MySQL;

public class GerritConsumer extends ConsumerImpl {

    public GerritConsumer(String host, String port, String queueName) throws JMSException {
        super(host, Integer.parseInt(port), queueName);
    }

    public GerritConsumer(String host, int port, String queueName) throws JMSException {
        super(host, port, queueName);
    }

    public void onMessage(Message message) {
        try {
            Message response = null;
            if (message instanceof TextMessage) {
                TextMessage txtMsg = (TextMessage) message;
                String messageText = txtMsg.getText();
                // Message received
                System.out.println("Message received: " + messageText);
                // TimeUnit.SECONDS.sleep(Integer.parseInt(messageText));
                // int startpoint = Integer.parseInt(messageText);
                // gerritMiner.mine(startpoint, startpoint - 1);

                response = (TextMessage) session.createTextMessage();
                // ((TextMessage) response).setText(messageProtocol.handleProtocolMessage(messageText));
                ((TextMessage) response).setText("Working on: " + messageText);
            } else if (message instanceof ObjectMessage) {
                // Remote settings received now start gerrit miner and wait for an instance
                MineRequest mr = (MineRequest) ((ObjectMessage) message).getObject();
                MySQL mysql = new MySQL(mr.getMysqlHost(), mr.getMysqlPort(), mr.getMysqlName(), mr.getMysqlUser(), mr.getMysqlPassword());
                GerritMiner gerritMiner = new GerritMiner(mysql, mr.getGerritURL(), mr.getGerritProject());
                gerritMiner.start();
                System.out.println("Request: mine Gerrit ID from " + mr.getStartGerritID() + " to " + mr.getStopGerritID());
                List<MinedResults> minedResults = gerritMiner.mine(mr.getStartGerritID(), mr.getStopGerritID());
                if (minedResults != null && minedResults.size() > 0) {
                    // Return mined objects
                    mr.setMinedResults(minedResults);
                } else {
                    // Return null
                    mr.setMinedResults(null);
                }
                gerritMiner.close();
                response = (ObjectMessage) session.createObjectMessage();
                ((ObjectMessage) response).setObject(mr);
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
        }
    }

}
