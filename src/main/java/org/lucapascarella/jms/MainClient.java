
package org.lucapascarella.jms;

import org.lucapascarella.gerrit.GerritProducer;
import org.lucapascarella.utils.Args;
import org.lucapascarella.utils.Config;
import org.lucapascarella.utils.ExecTime;
import org.lucapascarella.utils.PropDef;

public class MainClient {

    public static void main(String[] args) {
        // boolean rtn;

        // Start time monitoring
        ExecTime et = new ExecTime();
        System.out.println("*** Program (MainClient) " + PropDef.progName[1] + " version: " + PropDef.progVersion[1] + " ***\n");

        // Parse CLI parameters and load program configurations
        Config config = new Config(new Args(args), "config.main.txt");

        // Create a connection to JMS Host dispatcher
        String jmsHost = config.getProp(PropDef.defaultJMSHost[0]);
        String jmsPort = config.getProp(PropDef.defaultJMSPort[0]);
        String jmsQueue = config.getProp(PropDef.defaultJMSQueue[0]);
        // Create GerritProducer
        GerritProducer producer = new GerritProducer(jmsHost, jmsPort, jmsQueue);

        // Start mining
        producer.startMining(config);

        // Wait until all requests have been executed
        producer.waitTermination();

        // Program terminated print time and exit
        et.printExecutionTime("Main Client");
    }

}
