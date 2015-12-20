package se.dandel.tools.classdepanalyzer;

import javax.inject.Inject;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.matcher.Matchers;

public class ClassAnalyzer {

    @Inject
    private DependencyTracker tracker;

    @Inject
    private PlantUMLWriter writer;

    private ClassAnalyzer() {
        // Create instance using static methods
    }

    public void analyze() {
        writer.write(tracker.track());
    }

    public static ClassAnalyzer newInjectedInstance(final Settings settings) {
        ClassAnalyzer analyzer = new ClassAnalyzer();

        AbstractModule module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Settings.class).toInstance(settings);
                if (settings.isTraceEnabled()) {
                    bindInterceptor(Matchers.inPackage(DependencyTracker.class.getPackage()), Matchers.any(),
                            new DebugVisitorInterceptor());
                }
            }
        };
        Guice.createInjector(module).injectMembers(analyzer);
        return analyzer;
    }

}
