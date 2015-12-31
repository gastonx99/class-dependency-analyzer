package se.dandel.tools.classdepanalyzer;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;

public class DependencyTracker {

    @Inject
    private DependencyVisitor visitor;

    @Inject
    private Settings settings;

    public ClassTreeNode track() {
        return track(null, settings.getClassname());
    }

    public ClassTreeNode track(ClassTreeNode parent, String classname) {
        ClassDefinition.setup(getClazz(classname));
        scan(classname);
        ClassTreeNode me = new ClassTreeNode(parent, ClassDefinition.current());
        if (!me.isCyclic()) {
            for (String child : ClassDefinition.current().getAllClassnames()) {
                if (isAllowed(child)) {
                    me.add(track(me, child));
                }
            }
        }
        return me;
    }

    private Class<?> getClazz(String classname) {
        try {
            return settings.getTargetClassloader().loadClass(classname);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("For name " + classname, e);
        }
    }

    private boolean isAllowed(String name) {
        return name.startsWith(settings.getIncludes());
    }

    private void scan(String classname) {
        InputStream is = null;
        try {
            is = settings.getTargetClassloader().getResourceAsStream(classname.replace('.', '/') + ".class");
            new ClassReader(is).accept(visitor, 0);
        } catch (IOException e) {
            throw new RuntimeException("For " + classname, e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
}
