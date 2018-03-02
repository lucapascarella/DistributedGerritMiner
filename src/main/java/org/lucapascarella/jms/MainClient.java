
package org.lucapascarella.jms;

import org.lucapascarella.beans.TestSerializable;
import org.lucapascarella.db.MySQL;
import org.lucapascarella.utils.Args;
import org.lucapascarella.utils.Config;
import org.lucapascarella.utils.ExecTime;
import org.lucapascarella.utils.PropDef;
import org.lucapscarella.JMSAPI.Producer;

public class MainClient {

    public static void main(String[] args) {
        // Start time monitoring
        ExecTime et = new ExecTime();
        System.out.println("*** Program (Client) " + PropDef.progName[1] + " version: " + PropDef.progVersion[1] + " ***\n");

        // Parse CLI parameters and load program configurations
        Config config = new Config(new Args(args));

         // Test MySQL connection
         String sqlHost = config.getProp(PropDef.defaultDBHost[0]);
         String sqlPort = config.getProp(PropDef.defaultDBPort[0]);
         String sqlDBName = config.getProp(PropDef.defaultDBName[0]);
         String sqlUser = config.getProp(PropDef.defaultDBUser[0]);
         String sqlPass = config.getProp(PropDef.defaultDBPassword[0]);
         MySQL mysql = new MySQL(sqlHost, sqlPort, sqlDBName, sqlUser, sqlPass);
        
         // Check for 'reviews' tables
         String table = "test";
         String[] params = { "id", "name", "surname" };
         String[] types = { "int(11)", "varchar(64)", "varchar(255)" };
         String[] notNull = { "id", "surname" };
         String[] autoIncrement = { "id" };
         String[] unique = { "id", "name" };
         String[] primaryKey = { "id"};
         String[] foreignKey = { "name", "test2(id)" };
         mysql.createTableIfNotExists(table, params, types, notNull, autoIncrement, unique, primaryKey, foreignKey);
        
         String[] params2 = { "name", "surname" };
         String[] values = { "luca", "pascarella" };
         mysql.insertComments(table, params2, values);


        // Create a connection to JMS Host dispatcher
        String jmsHost = config.getProp(PropDef.defaultJMSHost[0]);
        String jmsPort = config.getProp(PropDef.defaultJMSPort[0]);
        String jmsQueue = config.getProp(PropDef.defaultJMSQueue[0]);
        Producer producer = new WorkerProducer(jmsHost, jmsPort, jmsQueue);
        // Send the Hello message
        System.out.println("Send hello messages");
        producer.sendTextMessage("4");
        producer.sendTextMessage("1");
        System.out.println("Hello messages sent");
        
        producer.sendObject(new TestSerializable("Ciao 1"));
        producer.sendObject(new TestSerializable("Ciao 2"));
        producer.sendObject(new TestSerializable("Ciao 3"));
        producer.sendObject(new TestSerializable("Ciao 4"));
        producer.sendObject(new TestSerializable("Ciao 5"));
        producer.sendObject(new TestSerializable("Ciao 6"));
        producer.sendObject(new TestSerializable("Ciao 7"));
        producer.sendObject(new TestSerializable("Ciao 8"));
        
        // Wait until all requests have been executed
        producer.waitTermination();

        et.printExecutionTime(PropDef.progName[1]);
    }

}
