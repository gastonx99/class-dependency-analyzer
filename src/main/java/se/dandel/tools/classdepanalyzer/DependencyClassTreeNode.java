package se.dandel.tools.classdepanalyzer;

import java.util.HashSet;
import java.util.Set;

public class DependencyClassTreeNode {

    private ClassDefinition classDefinition;

    private Set<DependencyClassTreeNode> children = new HashSet<>();
    private DependencyClassTreeNode parent;

    public DependencyClassTreeNode(DependencyClassTreeNode parent, ClassDefinition classDefinition) {
        this.parent = parent;
        this.classDefinition = classDefinition;
    }

    public void add(DependencyClassTreeNode child) {
        children.add(child);
    }

    public boolean isCyclic() {
        DependencyClassTreeNode t = parent;
        while (t != null) {
            if (t.getDefinition().getClassname().equals(this.getDefinition().getClassname())) {
                return true;
            }
            t = t.parent;
        }
        return false;
    }

    public Set<DependencyClassTreeNode> getChildren() {
        return children;
    }

    public ClassDefinition getDefinition() {
        return classDefinition;
    }

}
