package se.dandel.tools.classdepanalyzer;

import java.io.PrintWriter;
import java.lang.reflect.Modifier;
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

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Embeddable;
import javax.persistence.Entity;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class DependencyWriter {

    private Set<ClassDefinition> printedClassDefinitions = new HashSet<>();

    private PrintWriter _pw;

    @Inject
    private Settings settings;

    private boolean isAllowed(String name) {
        return name.startsWith(settings.getIncludes());
    }

    public void write(DependencyClassTreeNode root) {
        pw().println("@startuml");
        List<Class<?>> clazzes = filterUnwantedClasses(sortByName(filterAllowed(getAllDistinctDefinitions(root))));
        List<Package> packages = organizeIntoPackages(clazzes);
        for (Package packaze : packages) {
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

    private List<Class<?>> filterUnwantedClasses(List<ClassDefinition> classDefinitions) {
        List<Class<?>> list = new ArrayList<>();
        for (ClassDefinition classDefinition : classDefinitions) {
            Class<?> clazz = getClazz(classDefinition.getClassname());
            boolean filter = clazz.isEnum();
            filter |= clazz.isAnnotationPresent(Embeddable.class);
            if (!filter) {
                list.add(clazz);
            }
        }
        return list;
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
        list.sort(new Comparator<ClassDefinition>() {

            @Override
            public int compare(ClassDefinition o1, ClassDefinition o2) {
                return o1.getClassname().compareTo(o2.getClassname());
            }
        });
        return list;
    }

    private Set<ClassDefinition> getAllDistinctDefinitions(DependencyClassTreeNode node) {
        Set<ClassDefinition> s = new HashSet<>();
        s.add(node.getDefinition());
        for (DependencyClassTreeNode child : node.getChildren()) {
            s.addAll(getAllDistinctDefinitions(child));
        }
        return s;
    }

    private void writeRelations(DependencyClassTreeNode node, List<Class<?>> clazzes) {
        if (!printedClassDefinitions.contains(node.getDefinition())) {
            printedClassDefinitions.add(node.getDefinition());
            List<DependencyClassTreeNode> printedChildren = new ArrayList<>();
            for (DependencyClassTreeNode child : node.getChildren()) {
                if (!node.getDefinition().equals(child.getDefinition())
                        && clazzes.contains(getClazz(child.getDefinition().getClassname()))) {
                    pw().println(node.getDefinition().getSimpleClassname() + " --> "
                            + child.getDefinition().getSimpleClassname());
                    printedChildren.add(child);
                }
            }
            for (DependencyClassTreeNode child : printedChildren) {
                if (!node.getDefinition().equals(child.getDefinition())) {
                    writeRelations(child, clazzes);
                }
            }
        }
    }

    public static class Package {
        private final String name;
        private Package parent;
        private List<Class<?>> clazzes = new ArrayList<>();
        private List<Package> packages = new ArrayList<>();

        public Package(String name) {
            this.name = name;
        }

        public void setParent(Package parent) {
            this.parent = parent;
        }

        public Package getParent() {
            return parent;
        }

        public void addPackage(Package packaze) {
            packages.add(packaze);
        }

        public void addPackages(Collection<Package> packaze) {
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

        public List<Package> getPackages() {
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
            Package other = (Package) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
        }

    }

    private void writeDefinitions(Package packaze) {
        writeDefinitions(packaze, 0);
    }

    private void writeDefinitions(Package packaze, int indent) {
        pw().println(getIndent(indent) + getPackageDefinition(packaze, indent == 0) + " {");
        for (Class<?> clazz : packaze.getClazzes()) {
            pw().println(getIndent(indent + 1) + getClassDefinition(clazz));
        }
        for (Package childPackage : packaze.getPackages()) {
            writeDefinitions(childPackage, indent + 1);
        }
        pw().println(getIndent(indent) + "}");
    }

    private String getPackageDefinition(Package packaze, boolean fullname) {
        String name = packaze.getName();
        return "package " + (fullname ? name : name.substring(name.lastIndexOf(".") + 1));
    }

    private String getClassDefinition(Class<?> clazz) {
        StringBuilder builder = new StringBuilder(getIdentifier(clazz));
        builder.append(" " + clazz.getSimpleName());
        String stereotype = getStereotype(clazz);
        if (StringUtils.isNotBlank(stereotype)) {
            builder.append(" << " + stereotype + " >>");
        }
        return builder.toString();
    }

    private String getIndent(int indent) {
        return StringUtils.repeat("  ", null, indent);
    }

    private List<Package> organizeIntoPackages(List<Class<?>> list) {
        Map<String, Package> packages = new HashMap<String, Package>();

        for (Class<?> clazz : list) {
            Package packaze = getPackage(packages, clazz.getPackage().getName());
            packaze.addClazz(clazz);
        }

        removePackagesWithOnlyOneChild(packages);

        Set<Package> rootPackages = getRootPackages(packages.values());
        List<Package> sortedRootPackages = new ArrayList<>(rootPackages);
        Collections.sort(sortedRootPackages, new Comparator<Package>() {
            @Override
            public int compare(Package o1, Package o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return sortedRootPackages;
    }

    private void removePackagesWithOnlyOneChild(Map<String, Package> packages) {
        Iterator<Entry<String, Package>> iterator = packages.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Package> next = iterator.next();
            Package packaze = next.getValue();
            if (packaze.getClazzes().isEmpty() && packaze.getPackages().size() <= 1) {
                Package parent = packaze.getParent();
                if (parent != null) {
                    parent.getPackages().remove(packaze);
                    parent.getPackages().addAll(packaze.getPackages());
                }
                for (Package childPackage : packaze.getPackages()) {
                    childPackage.setParent(parent);
                }
                iterator.remove();
            }
        }
    }

    private Package getPackage(Map<String, Package> packages, String packagename) {
        Package packaze = packages.get(packagename);
        if (packaze == null) {
            packaze = new Package(packagename);
            if (packagename.contains(".")) {
                Package parent = getPackage(packages, packagename.substring(0, packagename.lastIndexOf(".")));
                packaze.setParent(parent);
                parent.addPackage(packaze);
            }
            packages.put(packaze.getName(), packaze);
        }
        return packaze;
    }

    private Set<Package> getRootPackages(Collection<Package> collection) {
        Set<Package> set = new HashSet<>();
        for (Package packaze : collection) {
            if (packaze.getParent() == null) {
                set.add(packaze);
            }
        }
        return set;
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

    private Class<?> getClazz(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("For name " + name, e);
        }
    }

}
