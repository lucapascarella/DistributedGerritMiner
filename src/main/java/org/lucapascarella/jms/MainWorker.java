package org.lucapascarella.jms;

import javax.jms.JMSException;

import org.lucapascarella.gerrit.GerritMiner;
import org.lucapascarella.utils.Args;
import org.lucapascarella.utils.Config;
import org.lucapascarella.utils.ExecTime;
import org.lucapascarella.utils.PropDef;

public class MainWorker {
    public static void main(String[] args) {
        // Start time monitoring
        ExecTime et = new ExecTime();
        System.out.println("*** Program (Worker) " + PropDef.progName[1] + " version: " + PropDef.progVersion[1] + " ***\n");

        // Parse CLI parameters and load program configurations
        Config config = new Config(new Args(args));

        GerritMiner gerritMiner = new GerritMiner(config);
        
        try {
            String jmsHost = config.getProp(PropDef.defaultJMSHost[0]);
            String jmsPort = config.getProp(PropDef.defaultJMSPort[0]);
            String jmsQueue = config.getProp(PropDef.defaultJMSQueue[0]);
            WorkerReplay worker = new WorkerReplay(jmsHost, jmsPort, jmsQueue);
            worker.setGerritMiner(gerritMiner);
            worker.startAndWait();
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            et.printExecutionTime(PropDef.progName[1]);
        }
    }
}
