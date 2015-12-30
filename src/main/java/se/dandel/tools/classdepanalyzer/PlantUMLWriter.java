package se.dandel.tools.classdepanalyzer;

import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Embeddable;
import javax.persistence.Entity;

import org.apache.commons.lang.StringUtils;

public class PlantUMLWriter {

    private Set<ClassDefinition> printedClassDefinitions = new HashSet<>();

    private PrintWriter _pw;

    @Inject
    private Settings settings;

    public void write(ClassTreeNode root, List<ClassPackage> packages, List<Class<?>> clazzes) {
        pw().println("@startuml");
        for (ClassPackage packaze : packages) {
            writeDefinitions(packaze);
        }
        writeRelations(root, clazzes);
        pw().println("@enduml");
        pw().close();
    }

    private PrintWriter pw() {
        if (_pw == null) {
            try {
                _pw = new PrintWriter(settings.getOutputFilename());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return _pw;
    }

    private void writeRelations(ClassTreeNode node, List<Class<?>> clazzes) {
        if (!printedClassDefinitions.contains(node.getDefinition())) {
            printedClassDefinitions.add(node.getDefinition());
            List<ClassTreeNode> printedChildren = new ArrayList<>();
            for (ClassTreeNode child : node.getChildren()) {
                if (!node.getDefinition().equals(child.getDefinition())
                        && clazzes.contains(getClazz(child.getDefinition().getClassname()))) {
                    pw().println(camelCasedClassname(node.getDefinition()) + " --> "
                            + camelCasedClassname(child.getDefinition()));
                    printedChildren.add(child);
                }
            }
            for (ClassTreeNode child : printedChildren) {
                if (!node.getDefinition().equals(child.getDefinition())) {
                    writeRelations(child, clazzes);
                }
            }
        }
    }

    private Class<?> getClazz(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("For name " + name, e);
        }
    }

    private void writeDefinitions(ClassPackage packaze) {
        writeDefinitions(packaze, 0);
    }

    private void writeDefinitions(ClassPackage packaze, int indent) {
        pw().println(getIndent(indent) + getPackageDefinition(packaze, indent == 0) + " {");
        for (Class<?> clazz : packaze.getClazzes()) {
            pw().println(getIndent(indent + 1) + getClassDefinition(clazz));
        }
        for (ClassPackage childPackage : packaze.getPackages()) {
            writeDefinitions(childPackage, indent + 1);
        }
        pw().println(getIndent(indent) + "}");
    }

    private String getPackageDefinition(ClassPackage packaze, boolean fullname) {
        String name = packaze.getName();
        return "package " + (fullname ? name : name.substring(name.lastIndexOf(".") + 1));
    }

    private String getClassDefinition(Class<?> clazz) {
        StringBuilder builder = new StringBuilder(getIdentifier(clazz));
        builder.append(" \"" + clazz.getSimpleName() + "\"");
        builder.append(" as " + camelCasedClassname(clazz));
        String stereotype = getStereotype(clazz);
        if (StringUtils.isNotBlank(stereotype)) {
            builder.append(" << " + stereotype + " >>");
        }
        return builder.toString();
    }

    private String camelCasedClassname(ClassDefinition definition) {
        return camelCasedClassname(definition.getClassname());
    }

    private String camelCasedClassname(Class<?> clazz) {
        return camelCasedClassname(clazz.getName());
    }

    private String camelCasedClassname(String classname) {
        StringBuilder builder = new StringBuilder();
        String[] split = classname.split("\\.");
        for (String str : split) {
            builder.append(StringUtils.capitalize(str));
        }
        return builder.toString();
    }

    private String getIndent(int indent) {
        return StringUtils.repeat("  ", null, indent);
    }

    private String getStereotype(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Stateless.class)) {
            return "(B,pink) Stateless";
        } else if (clazz.isAnnotationPresent(Entity.class)) {
            return "(P,orange) Entity";
        } else if (clazz.isAnnotationPresent(Embeddable.class)) {
            return "(P,orange) Embeddable";
        }
        return null;
    }

    private String getIdentifier(Class<?> clazz) {
        String identifier = "class";
        if (isInterface(clazz)) {
            identifier = "interface";
        } else if (isAbstract(clazz)) {
            identifier = "abstract";
        } else if (isEnum(clazz)) {
            identifier = "enum";
        }
        return identifier;
    }

    private boolean isEnum(Class<?> clazz) {
        return clazz.isEnum();
    }

    private boolean isInterface(Class<?> clazz) {
        return clazz.isInterface();
    }

    private boolean isAbstract(Class<?> clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

}
