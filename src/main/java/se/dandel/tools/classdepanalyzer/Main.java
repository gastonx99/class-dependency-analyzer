package se.dandel.tools.classdepanalyzer;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;

public class Main {
    public static void main(final String[] args) throws IOException {
        try {
            CommandLine cmd = parseOptions(args);
            Settings settings = parseSettings(cmd);

            ClassAnalyzer analyzer = ClassAnalyzer.newInjectedInstance(settings);
            analyzer.analyze();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Settings parseSettings(CommandLine cmd) {
        Settings settings = new Settings();
        settings.setClassname(cmd.getOptionValue("classname"));
        settings.setIncludes(cmd.getOptionValue("includes"));
        settings.setOutputFilename(cmd.getOptionValue("output"));
        String cp = cmd.getOptionValue("targetclasspath");
        if (StringUtils.isNotBlank(cp)) {
            settings.setTargetClasspath(cp);
        }
        return settings;
    }

    private static CommandLine parseOptions(final String[] args) throws ParseException {
        Options options = new Options();
        options.addOption(Option.builder("classname").required().hasArg().desc("Root classname to inspect").build());
        options.addOption(Option.builder("includes").required().hasArg().desc("Package names to include").build());
        options.addOption(Option.builder("output").required().hasArg().desc("Output filename").build());
        options.addOption(Option.builder("targetclasspath").hasArg().desc("Target classpath").build());
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }

}
