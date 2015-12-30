package se.dandel.tools.classdepanalyzer;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

public class PlantUMLWriter {

    private Set<ClassDefinition> printedClassDefinitions = new HashSet<>();

    private PrintWriter _pw;

    @Inject
    private Settings settings;

    public void write(ClassTreeNode root, List<ClassPackage> packages, List<ClassDefinition> definitions) {
        pw().println("@startuml");
        for (ClassPackage packaze : packages) {
            writeDefinitions(packaze);
        }
        writeRelations(root, definitions);
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

    private void writeRelations(ClassTreeNode node, List<ClassDefinition> definitions) {
        if (!printedClassDefinitions.contains(node.getDefinition())) {
            printedClassDefinitions.add(node.getDefinition());
            List<ClassTreeNode> printedChildren = new ArrayList<>();
            for (ClassTreeNode child : node.getChildren()) {
                if (!node.getDefinition().equals(child.getDefinition())
                        && definitions.contains(child.getDefinition())) {
                    pw().println(camelCasedClassname(node.getDefinition()) + " --> "
                            + camelCasedClassname(child.getDefinition()));
                    printedChildren.add(child);
                }
            }
            for (ClassTreeNode child : printedChildren) {
                if (!node.getDefinition().equals(child.getDefinition())) {
                    writeRelations(child, definitions);
                }
            }
        }
    }

    private void writeDefinitions(ClassPackage packaze) {
        writeDefinitions(packaze, 0);
    }

    private void writeDefinitions(ClassPackage packaze, int indent) {
        pw().println(getIndent(indent) + getPackageDefinition(packaze, indent == 0) + " {");
        for (ClassDefinition definition : packaze.getClazzes()) {
            pw().println(getIndent(indent + 1) + getClassDefinition(definition));
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

    private String getClassDefinition(ClassDefinition definition) {
        StringBuilder builder = new StringBuilder(getIdentifier(definition));
        builder.append(" \"" + definition.getSimpleClassname() + "\"");
        builder.append(" as " + camelCasedClassname(definition));
        String stereotype = getStereotype(definition);
        if (StringUtils.isNotBlank(stereotype)) {
            builder.append(" << " + stereotype + " >>");
        }
        return builder.toString();
    }

    private String camelCasedClassname(ClassDefinition definition) {
        return camelCasedClassname(definition.getClassname());
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

    private String getStereotype(ClassDefinition definition) {
        if (definition.isStateless()) {
            return "(B,pink) Stateless";
        } else if (definition.isEntity()) {
            return "(P,orange) Entity";
        } else if (definition.isEmbeddable()) {
            return "(P,orange) Embeddable";
        }
        return null;
    }

    private String getIdentifier(ClassDefinition definition) {
        String identifier = "class";
        if (definition.isInterface()) {
            identifier = "interface";
        } else if (definition.isAbstract()) {
            identifier = "abstract";
        } else if (definition.isEnum()) {
            identifier = "enum";
        }
        return identifier;
    }

}
