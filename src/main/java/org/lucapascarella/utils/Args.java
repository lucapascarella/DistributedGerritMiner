package org.lucapascarella.utils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Args {

    CommandLine cmd;

    public Args(String[] args) {
        Options options = new Options();

        // Example of file config.txt given by input
        Option prop = new Option("p", "prop", true, "Full path of properties file. If not provided 'config.txt' in the current working directory will be used");
        prop.setRequired(false);
        options.addOption(prop);

        // Overwrite working directory
        Option client = new Option("w", PropDef.defaultWorkingDirectory[0], true, "Full path of of the desidered working directory '" + PropDef.progName[1] + "'");
        client.setRequired(false);
        options.addOption(client);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println("*** " + PropDef.progName[1] + " v" + PropDef.progVersion[1] + " ***");
            System.out.println(e.getMessage());
            formatter.printHelp("java - jar -p /user/home/config.txt -w /user/home/", options);

            System.exit(1);
            return;
        }

    }

    public String getArg(String key, String def) {
        return cmd.getOptionValue(key, def);
    }

}
