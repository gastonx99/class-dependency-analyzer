package se.dandel.tools.classdepanalyzer;

import javax.inject.Inject;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.matcher.Matchers;

public class DependencyAnalyzer {

    @Inject
    private DependencyTracker tracker;

    @Inject
    private DependencyWriter writer;

    private DependencyAnalyzer() {
        // Create instance using static methods
    }

    public void analyze() {
        writer.write(tracker.track());
    }

    public static DependencyAnalyzer newInjectedInstance(final Settings settings) {
        DependencyAnalyzer analyzer = new DependencyAnalyzer();

        AbstractModule module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Settings.class).toInstance(settings);
                if (settings.isTraceEnabled()) {
                    bindInterceptor(Matchers.inPackage(DependencyTracker.class.getPackage()), Matchers.any(),
                            new DebugDependencyVisitorInterceptor());
                }
            }
        };
        Guice.createInjector(module).injectMembers(analyzer);
        return analyzer;
    }

}
