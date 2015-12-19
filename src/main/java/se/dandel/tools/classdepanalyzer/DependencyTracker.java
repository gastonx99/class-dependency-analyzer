package se.dandel.tools.classdepanalyzer;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.objectweb.asm.ClassReader;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;

public class DependencyTracker {

    private static Module DEBUG_MODULE = new AbstractModule() {
        @Override
        protected void configure() {
            bindInterceptor(Matchers.inPackage(DependencyTracker.class.getPackage()), Matchers.any(),
                    new DebugDependencyVisitorInterceptor());
        }
    };

    private static Module NOOP_MODULE = new AbstractModule() {
        @Override
        protected void configure() {
        }
    };

    @Inject
    private DependencyVisitor visitor;

    private String includes;

    public static void main(final String[] args) throws IOException {
        try {
            CommandLine cmd = parseOptions(args);
            String name = cmd.getOptionValue("classname");

            DependencyTracker tracker = new DependencyTracker();
            tracker.setIncludes(cmd.getOptionValue("includes"));
            Guice.createInjector(createModule(false)).injectMembers(tracker);
            DependencyClassTreeNode root = tracker.track(name);
            PrintWriter pw = new PrintWriter(cmd.getOptionValue("output"));
            DependencyWriter writer = new DependencyWriter(pw);
            writer.writePlantuml(root);
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setIncludes(String includes) {
        this.includes = includes;
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

    private DependencyClassTreeNode track(String name) {
        return track(null, name);
    }

    private static Module createModule(boolean debug) {
        return debug ? DEBUG_MODULE : NOOP_MODULE;
    }

    public DependencyClassTreeNode track(DependencyClassTreeNode parent, String classname) {
        ClassDefinition.setup(classname);
        scan(classname);
        DependencyClassTreeNode me = new DependencyClassTreeNode(parent, ClassDefinition.current());
        if (!me.isCyclic()) {
            for (String child : ClassDefinition.current().getAllClassnames()) {
                if (isAllowed(child)) {
                    me.add(track(me, child));
                }
            }
        }
        return me;
    }

    private boolean isAllowed(String name) {
        return name.startsWith(includes);
    }

    private void scan(String classname) {
        try {
            new ClassReader(classname).accept(visitor, 0);
        } catch (IOException e) {
            throw new RuntimeException("For " + classname, e);
        }
    }
}
