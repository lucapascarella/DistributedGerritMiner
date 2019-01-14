package org.lucapascarella.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public final class PropDef {
    // The properties are expressed in pairs key-value.
    // An array of string represents a single pair key value.
    // The 0-element is the key and the 1-element is the value.

    // Hard coded program informations
    public static String[] progName = { "prog.name", "Main interface" };
    public static String[] progVersion = { "prog.ver", "0.1" };
    public static String[] defaultWorkingDirectory = { "prog.workdir", "/Users/luca/TUProjects/workspace/ActiveMQTest" };

    // Program configuration files
    public static String[] configVersion = { "config.version", "0.1" };
    // public static String[] defaultFilePropName = { "config.file", "config.txt" };
    // public static String[] defaultClientList = { "config.clients", "clients.txt" };

    // Database values
    public static String defaultDBHost[] = { "database.host", "benevento.pascarella.cloud" };
    public static String defaultDBPort[] = { "database.port", "3306" };
    public static String defaultDBUser[] = { "database.user", "luca" };
    public static String defaultDBPassword[] = { "database.password", "master" };
    public static String defaultDBName[] = { "database.name", "gerritMiner" };

    // JMS values
    public static String defaultJMSHost[] = { "jms.host", "delft.pascarella.cloud" };
    public static String defaultJMSPort[] = { "jms.port", "61616" };
    public static String defaultJMSQueue[] = { "jms.queue", "miner" };

    // Gerrit settings
    public static String defaultGerritURL[] = { "gerrit.url", "https://codereview.qt-project.org" };
    public static String defaultGerritStartID[] = { "gerrit.start", "1" };
    public static String defaultGerritStopID[] = { "gerrit.stop", "1000" };
    public static String defaultRequestsPerWorker[] = { "gerrit.requests", "5" };

    public static char getFileSeparator() {
        String rtn = System.getProperty("file.separator");
        if (rtn.length() == 1)
            return rtn.charAt(0);
        return '\0';
    }

    public static char getPathSeparator() {
        String rtn = System.getProperty("path.separator");
        if (rtn.length() == 1)
            return rtn.charAt(0);
        return '\0';
    }

    public static Map<String, String> getDefaultList() {
        Map<String, String> pairs = new HashMap<String, String>();

        // Use reflection to list all instances variables
        PropDef pd = new PropDef();
        Class<?> objClass = pd.getClass();
        Field[] fields = objClass.getFields();
        for (Field field : fields) {
            // String name = field.getName();
            String[] value;
            try {
                value = (String[]) field.get(pd);
                pairs.put(value[0], value[1]);
                // System.out.println(name + ": " + value.toString());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return pairs;
    }
}
