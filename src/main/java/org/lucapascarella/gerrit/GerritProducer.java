package org.lucapascarella.gerrit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import org.lucapascarella.beans.MyComment;
import org.lucapascarella.beans.MyDeveloper;
import org.lucapascarella.beans.MyFile;
import org.lucapascarella.beans.MyMessage;
import org.lucapascarella.beans.MyReview;
import org.lucapascarella.beans.MyRevision;
import org.lucapascarella.JMSAPI.ProducerImpl;
import org.lucapascarella.beans.MineRequest;
import org.lucapascarella.beans.MinedResults;
import org.lucapascarella.db.MySQL;
import org.lucapascarella.utils.Config;
import org.lucapascarella.utils.PropDef;

public class GerritProducer extends ProducerImpl {

    private MySQL mysql;

    public GerritProducer(String jmsHost, String jmsPort, String jmsQueue) {
        // Create a connection with JMS dispatcher
        super(jmsHost, jmsPort, jmsQueue);
    }

    public void startMining(Config config) {
        // Update remote worker with know DB host
        String gerritURL = config.getProp(PropDef.defaultGerritURL[0]);
        String gerritProject = config.getProp(PropDef.defaultGerritProject[0]);
        String sqlHost = config.getProp(PropDef.defaultDBHost[0]);
        String sqlPort = config.getProp(PropDef.defaultDBPort[0]);
        String sqlDBName = config.getProp(PropDef.defaultDBName[0]);
        String sqlUser = config.getProp(PropDef.defaultDBUser[0]);
        String sqlPass = config.getProp(PropDef.defaultDBPassword[0]);

        // Test DB tables
        mysql = new MySQL(sqlHost, sqlPort, sqlDBName, sqlUser, sqlPass);
        new MyReview(mysql).checkTable();
        new MyDeveloper(mysql).checkTable();
        new MyMessage(mysql).checkTable();
        new MyRevision(mysql).checkTable();
        new MyFile(mysql).checkTable();
        new MyComment(mysql).checkTable();

        // Get Gerrit IDs to mine
        long startIdToMine = Long.parseLong(config.getProp(PropDef.defaultGerritStartID[0]));
        long stopIdToMine = Long.parseLong(config.getProp(PropDef.defaultGerritStopID[0]));
        long requestsPerWorker = Long.parseLong(config.getProp(PropDef.defaultRequestsPerWorker[0]));

        // Send a few requests and wait before send others
        for (long i = startIdToMine; i < stopIdToMine; i += requestsPerWorker) {
            long startGerritID = i;
            long stopGerritID = i + requestsPerWorker;
            MineRequest remoteRequest = new MineRequest(gerritURL, gerritProject, startGerritID, stopGerritID, sqlHost, sqlPort, sqlDBName, sqlUser, sqlPass);
            this.sendObject(remoteRequest);
            System.out.println("Request: " + remoteRequest.getStartGerritID() + " to " + remoteRequest.getStopGerritID());
            // Check queue size wait if too much elements are present
            while (mapSize() > 20) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void confirmOrDeleteEntry(MinedResults mr) throws SQLException {
        String mineStatus;
        long gerritID;
        ResultSet resultSet = mysql.selectQuery("reviews", "ID, mineStatus", "gerritId", MySQL.SELECT_EQUAL, String.valueOf(mr.getGerritId()));
        if (resultSet != null && resultSet.next()) {
            mineStatus = resultSet.getString("mineStatus");
            gerritID = resultSet.getInt("ID");

            if (mineStatus.equals("WorkerDone") && mr.getDone()) {
                mysql.updateQuery("reviews", "mineStatus", "ClientConfirmed", "ID", String.valueOf(gerritID));
                System.out.println("\t Gerrit ID: " + mr.getGerritId() + " - Entry updated. " + mr.getStatus());
            } else {
                System.out.println("\t Gerrit ID: " + mr.getGerritId() + " - Cannot update entry. " + mr.getStatus());
            }
        } else {
            System.out.println("\t Gerrit ID: " + mr.getGerritId() + " - No entry found. " + mr.getStatus());
        }
    }

    /**
     * Asynchronous wait for remote worker reply
     */
    public void onMessage(Message message) {
        try {
            String correlationId = message.getJMSCorrelationID();
            if (message instanceof TextMessage) {
                // It was a text message
                String requestedMessage = (String) getValue(correlationId);
                String receivedMessage = ((TextMessage) message).getText();
                System.out.println("Request: " + requestedMessage + ". Respose: " + receivedMessage);
            } else if (message instanceof ObjectMessage) {
                // It was an object
                MineRequest requestedObject = (MineRequest) getValue(correlationId);
                MineRequest receivedObject = (MineRequest) ((ObjectMessage) message).getObject();
                // What happened
                List<MinedResults> minedResults = receivedObject.getMinedResults();
                if (minedResults != null) {
                    System.out.println("Requested Gerrit range: " + requestedObject.getStartGerritID() + "-" + requestedObject.getStopGerritID());
                    for (MinedResults mr : minedResults)
                        confirmOrDeleteEntry(mr);
                } else {
                    System.out.println("Requested Gerrit range: " + requestedObject.getStartGerritID() + " - " + requestedObject.getStopGerritID() + ". All failed!");
                }
            } else {

            }
            removeKeyFromMap(correlationId);
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
