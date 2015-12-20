package se.dandel.tools.classdepanalyzer;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {
    public static void main(final String[] args) throws IOException {
        try {
            CommandLine cmd = parseOptions(args);
            Settings settings = parseSettings(cmd);

            DependencyAnalyzer analyzer = DependencyAnalyzer.newInjectedInstance(settings);
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
        return settings;
    }

    private static CommandLine parseOptions(final String[] args) throws ParseException {
        Options options = new Options();
        options.addOption(Option.builder("classname").required().hasArg().desc("root classname to inspect").build());
        options.addOption(Option.builder("includes").required().hasArg().desc("includes").build());
        options.addOption(Option.builder("output").required().hasArg().desc("output filename").build());
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        return cmd;
    }

}
