package org.lucapascarella.utils;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

import org.lucapascarella.utils.Args;
import org.lucapascarella.utils.PropDef;

public class Config {

    private Args args;
    private PropertiesConfiguration config;
    private Map<String, String> propDefault;

    public Config(Args args, String filename) {
        this.args = args;
        // Get configuration file name
        String configFileName = args.getArg("prop", filename);
        // Get default properties
        this.propDefault = PropDef.getDefaultList();
        // Check for file existence otherwise create one
        File file = new File(configFileName);
        if (!file.exists()) {
            createDefaultOptionFile(configFileName);
        }
        // Create configuration file handler point and set auto save enabled
        try {
            Parameters params = new Parameters();
            FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<PropertiesConfiguration>(PropertiesConfiguration.class)
                    .configure(params.fileBased().setFile(new File(configFileName)));
            // enable auto save mode
            builder.setAutoSave(true);
            config = builder.getConfiguration();
            // XMLConfiguration config = configs.xml("paths.xml");
        } catch (ConfigurationException cex) {
            cex.printStackTrace();
        }
    }

    private void createDefaultOptionFile(String filename) {
        // Create header for properties file
        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
        PropertiesConfigurationLayout layout = propertiesConfiguration.getLayout();
        layout.setLineSeparator("\n");
        String headerComment = "Properties for program: " + PropDef.progName[1] + "\n";
        headerComment += "# Version: " + PropDef.progVersion[1] + "\n";
        headerComment += "# Created on: " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
        layout.setHeaderComment(headerComment);

        // layout.setBlancLinesBefore(PropDefault.configVersion[0], 1);
        // layout.setComment(PropDefault.configVersion[0], "Cron schedule for retention policy");
        // propertiesConfiguration.setProperty(PropDefault.configVersion[0], PropDefault.configVersion[1]);

        // Append all default properties
        try {
            for (Map.Entry<String, String> entry : propDefault.entrySet())
                propertiesConfiguration.setProperty(entry.getKey(), entry.getValue());
            propertiesConfiguration.write(new FileWriter(filename));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * This method checks for command line argument, if not found checks in properties file, and if not found checks for default value
     * 
     * @param key
     *            is the entry point of the property to retrieve
     * @return is a String that represents the values associated to the given key
     */
    public String getProp(String key) {
        String value = args.getArg(key, null);
        if (value == null) {
            if (config.containsKey(key)) {
                value = config.getString(key);
            } else {
                value = propDefault.get(key);
                config.setProperty(key, value); // the configuration is saved after this call
            }
        }
        return value;
    }
}
