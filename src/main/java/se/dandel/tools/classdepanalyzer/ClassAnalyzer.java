package se.dandel.tools.classdepanalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.matcher.Matchers;

public class ClassAnalyzer {

    @Inject
    private DependencyTracker tracker;

    @Inject
    private PlantUMLWriter writer;

    @Inject
    private Settings settings;

    private ClassAnalyzer() {
        // Create instance using static methods
    }

    public void analyze() {
        ClassTreeNode root = tracker.track();
        List<ClassDefinition> definitions =
                filterUnwantedClasses(sortByName(filterAllowed(getAllDistinctDefinitions(root))));
        List<ClassPackage> packages = organizeIntoPackages(definitions);
        writer.write(root, packages, definitions);
    }

    private List<ClassPackage> organizeIntoPackages(List<ClassDefinition> list) {
        Map<String, ClassPackage> packages = new HashMap<String, ClassPackage>();

        for (ClassDefinition definition : list) {
            ClassPackage packaze = getPackage(packages, definition.getPackagename());
            packaze.addDefinition(definition);
        }

        removePackagesWithOnlyOneChild(packages);

        Set<ClassPackage> rootPackages = getRootPackages(packages.values());
        List<ClassPackage> sortedRootPackages = new ArrayList<>(rootPackages);
        Collections.sort(sortedRootPackages, new Comparator<ClassPackage>() {
            @Override
            public int compare(ClassPackage o1, ClassPackage o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return sortedRootPackages;
    }

    private void removePackagesWithOnlyOneChild(Map<String, ClassPackage> packages) {
        Iterator<Entry<String, ClassPackage>> iterator = packages.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, ClassPackage> next = iterator.next();
            ClassPackage packaze = next.getValue();
            if (packaze.getDefinitions().isEmpty() && packaze.getPackages().size() <= 1) {
                ClassPackage parent = packaze.getParent();
                if (parent != null) {
                    parent.getPackages().remove(packaze);
                    parent.getPackages().addAll(packaze.getPackages());
                }
                for (ClassPackage childPackage : packaze.getPackages()) {
                    childPackage.setParent(parent);
                }
                iterator.remove();
            }
        }
    }

    private ClassPackage getPackage(Map<String, ClassPackage> packages, String packagename) {
        ClassPackage packaze = packages.get(packagename);
        if (packaze == null) {
            packaze = new ClassPackage(packagename);
            if (packagename.contains(".")) {
                ClassPackage parent = getPackage(packages, packagename.substring(0, packagename.lastIndexOf(".")));
                packaze.setParent(parent);
                parent.addPackage(packaze);
            }
            packages.put(packaze.getName(), packaze);
        }
        return packaze;
    }

    private Set<ClassPackage> getRootPackages(Collection<ClassPackage> collection) {
        Set<ClassPackage> set = new HashSet<>();
        for (ClassPackage packaze : collection) {
            if (packaze.getParent() == null) {
                set.add(packaze);
            }
        }
        return set;
    }

    private Set<ClassDefinition> getAllDistinctDefinitions(ClassTreeNode node) {
        Set<ClassDefinition> s = new HashSet<>();
        s.add(node.getDefinition());
        for (ClassTreeNode child : node.getChildren()) {
            s.addAll(getAllDistinctDefinitions(child));
        }
        return s;
    }

    private List<ClassDefinition> filterUnwantedClasses(List<ClassDefinition> classDefinitions) {
        List<ClassDefinition> list = new ArrayList<>();
        for (ClassDefinition classDefinition : classDefinitions) {
            boolean filter = classDefinition.isEnum();
            filter |= classDefinition.isEmbeddable();
            if (!filter) {
                list.add(classDefinition);
            }
        }
        return list;
    }

    private boolean isAllowed(String name) {
        return name.startsWith(settings.getIncludes());
    }

    private Set<ClassDefinition> filterAllowed(Set<ClassDefinition> definitions) {
        Set<ClassDefinition> set = new HashSet<>();
        for (ClassDefinition classDefinition : definitions) {
            if (isAllowed(classDefinition.getClassname())) {
                set.add(classDefinition);
            }
        }
        return set;
    }

    private List<ClassDefinition> sortByName(Set<ClassDefinition> definitions) {
        List<ClassDefinition> list = new ArrayList<>(definitions);
        Collections.sort(list, new Comparator<ClassDefinition>() {
            @Override
            public int compare(ClassDefinition o1, ClassDefinition o2) {
                return o1.getClassname().compareTo(o2.getClassname());
            }
        });
        return list;
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
