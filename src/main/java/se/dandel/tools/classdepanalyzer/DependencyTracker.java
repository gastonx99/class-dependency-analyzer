package se.dandel.tools.classdepanalyzer;

import java.io.IOException;

import javax.inject.Inject;

import org.objectweb.asm.ClassReader;

public class DependencyTracker {

    @Inject
    private DependencyVisitor visitor;

    @Inject
    private Settings settings;

    public DependencyClassTreeNode track() {
        return track(null, settings.getClassname());
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
        return name.startsWith(settings.getIncludes());
    }

    private void scan(String classname) {
        try {
            new ClassReader(classname).accept(visitor, 0);
        } catch (IOException e) {
            throw new RuntimeException("For " + classname, e);
        }
    }
}
