package org.lucapascarella.jms;

import javax.jms.JMSException;

import org.lucapascarella.gerrit.GerritConsumer;
import org.lucapascarella.utils.Args;
import org.lucapascarella.utils.Config;
import org.lucapascarella.utils.ExecTime;
import org.lucapascarella.utils.PropDef;

public class MainWorker {
    public static void main(String[] args) {
        // Start time monitoring
        ExecTime et = new ExecTime();
        System.out.println("*** Program (MainWorker) " + PropDef.progName[1] + " version: " + PropDef.progVersion[1] + " ***\n");

        // Parse CLI parameters and load program configurations
        Config config = new Config(new Args(args));

        // Connect to JMS Host and start Gerrit Miner worker
        try {
            String jmsHost = config.getProp(PropDef.defaultJMSHost[0]);
            String jmsPort = config.getProp(PropDef.defaultJMSPort[0]);
            String jmsQueue = config.getProp(PropDef.defaultJMSQueue[0]);
            GerritConsumer worker = new GerritConsumer(jmsHost, jmsPort, jmsQueue);
            worker.startAndWait();
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            // Program terminated print time and exit
            et.printExecutionTime(PropDef.progName[1]);
        }
    }
}
