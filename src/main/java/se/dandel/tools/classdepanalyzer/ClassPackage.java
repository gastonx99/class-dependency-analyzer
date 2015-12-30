package se.dandel.tools.classdepanalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class ClassPackage {
    private final String name;
    private ClassPackage parent;
    private List<Class<?>> clazzes = new ArrayList<>();
    private List<ClassPackage> packages = new ArrayList<>();

    public ClassPackage(String name) {
        this.name = name;
    }

    public void setParent(ClassPackage parent) {
        this.parent = parent;
    }

    public ClassPackage getParent() {
        return parent;
    }

    public void addPackage(ClassPackage packaze) {
        packages.add(packaze);
    }

    public void addPackages(Collection<ClassPackage> packaze) {
        packages.addAll(packaze);
    }

    public void addClazz(Class<?> clazz) {
        clazzes.add(clazz);
    }

    public List<Class<?>> getClazzes() {
        return clazzes;
    }

    public String getName() {
        return name;
    }

    public List<ClassPackage> getPackages() {
        return packages;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("name", name)
                .append("parent", parent != null ? parent.getName() : "null").append("classes", clazzes).toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClassPackage other = (ClassPackage) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}