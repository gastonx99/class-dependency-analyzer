package se.dandel.tools.classdepanalyzer;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.Stateless;
import javax.persistence.Embeddable;
import javax.persistence.Entity;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class ClassDefinition {

    private static ClassDefinition current;

    public static void setup(String classname) {
        current = new ClassDefinition(classname);
    }

    private Class<?> clazz;

    private ClassDefinition(String classname) {
        this.classname = classname;
        this.clazz = getClazz(classname);
    }

    private Class<?> getClazz(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("For name " + name, e);
        }
    }

    public static ClassDefinition current() {
        return current;
    }

    private final String classname;

    private String superClassname;

    private Set<String> interfaces = new HashSet<>();

    private Set<String> classAnnotations = new HashSet<>();

    private Set<String> members = new HashSet<>();

    private Set<String> memberAnnotations = new HashSet<>();

    private Set<String> methodParameters = new HashSet<>();

    private Set<String> methodParameterAnnotations = new HashSet<>();

    private Set<String> methodExceptions = new HashSet<>();

    private Set<String> methodAnnotations = new HashSet<>();

    private Set<String> methodLocalVariables = new HashSet<>();

    private Set<String> methodLocalAnnotations = new HashSet<>();

    private Set<String> methodReturnTypes = new HashSet<>();

    private Set<String> allClassnames = new HashSet<>();

    public String getClassname() {
        return classname;
    }

    public void setSuperClassname(String name) {
        this.superClassname = name;
        // allClassnames.add(name);
    }

    public String getSuperClassname() {
        return superClassname;
    }

    public void addInterfaces(Collection<String> names) {
        this.interfaces.addAll(names);
        // allClassnames.addAll(names);
    }

    public void addClassAnnotations(String name) {
        this.classAnnotations.add(name);
        allClassnames.add(name);
    }

    public void addMember(String name) {
        this.members.add(name);
        allClassnames.add(name);
    }

    public void addMemberAnnotation(String name) {
        this.memberAnnotations.add(name);
        allClassnames.add(name);
    }

    public void addMethodParameters(Collection<String> names) {
        this.methodParameters.addAll(names);
        allClassnames.addAll(names);
    }

    public void addMethodParameterAnnotation(String name) {
        this.methodParameterAnnotations.add(name);
        allClassnames.add(name);
    }

    public void addMethodAnnotation(String name) {
        this.methodAnnotations.add(name);
        allClassnames.add(name);
    }

    public void addMethodException(String name) {
        this.methodExceptions.add(name);
        allClassnames.add(name);
    }

    public void addMethodLocalAnnotation(String name) {
        this.methodLocalAnnotations.add(name);
        allClassnames.add(name);
    }

    public void addMethodReturnTypes(String name) {
        this.methodReturnTypes.add(name);
        allClassnames.add(name);
    }

    public void addMethodLocalVariable(String name) {
        this.methodLocalVariables.add(name);
        allClassnames.add(name);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((classname == null) ? 0 : classname.hashCode());
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
        ClassDefinition other = (ClassDefinition) obj;
        if (classname == null) {
            if (other.classname != null)
                return false;
        } else if (!classname.equals(other.classname))
            return false;
        return true;
    }

    public Set<String> getAllClassnames() {
        return allClassnames;
    }

    public boolean isEnum() {
        return clazz.isEnum();
    }

    public boolean isEmbeddable() {
        return clazz.isAnnotationPresent(Embeddable.class);
    }

    public String getPackagename() {
        return clazz.getPackage().getName();
    }

    public boolean isInterface() {
        return clazz.isInterface();
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    public boolean isStateless() {
        return clazz.isAnnotationPresent(Stateless.class);
    }

    public boolean isEntity() {
        return clazz.isAnnotationPresent(Entity.class);
    }

    public String getSimpleClassname() {
        return clazz.getSimpleName();
    }

}
