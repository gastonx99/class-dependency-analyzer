package se.dandel.tools.classdepanalyzer;

import java.util.HashSet;
import java.util.Set;

public class ClassTreeNode {

    private ClassDefinition classDefinition;

    private Set<ClassTreeNode> children = new HashSet<>();
    private ClassTreeNode parent;

    public ClassTreeNode(ClassTreeNode parent, ClassDefinition classDefinition) {
        this.parent = parent;
        this.classDefinition = classDefinition;
    }

    public void add(ClassTreeNode child) {
        children.add(child);
    }

    public boolean isCyclic() {
        ClassTreeNode t = parent;
        while (t != null) {
            if (t.getDefinition().getClassname().equals(this.getDefinition().getClassname())) {
                return true;
            }
            t = t.parent;
        }
        return false;
    }

    public Set<ClassTreeNode> getChildren() {
        return children;
    }

    public ClassDefinition getDefinition() {
        return classDefinition;
    }

}
