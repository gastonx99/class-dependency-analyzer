package se.dandel.tools.classdepanalyzer;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * DependencyVisitor
 * 
 * @author Eugene Kuleshov
 */
public class DependencyVisitor extends ClassVisitor {

    @Inject
    private MyAnnotationDependencyVisitor annotationDependencyVisitor;

    @Inject
    private MyFieldDependencyVisitor fieldDependencyVisitor;

    @Inject
    private MyMethodDependencyVisitor methodDependencyVisitor;

    @Inject
    private MyFieldSignatureDependencyVisitor fieldSignatureDependencyVisitor;

    @Inject
    private MyMethodSignatureDependencyVisitor methodSignatureDependencyVisitor;

    public DependencyVisitor() {
        super(Opcodes.ASM5);
    }

    // ClassVisitor

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        if (superName != null) {
            ClassDefinition.current().setSuperClassname(getObjectType(superName));
        }
        ClassDefinition.current().addInterfaces(getObjectTypes(interfaces));
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        ClassDefinition.current().addClassAnnotations(getSimpleType(desc));
        return annotationDependencyVisitor;
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String desc,
            final boolean visible) {
        return annotationDependencyVisitor;
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature,
            final Object value) {
        ClassDefinition.current().addMember(getSimpleType(desc));
        if (signature != null) {
            new SignatureReader(signature).acceptType(fieldSignatureDependencyVisitor);
        }
        return fieldDependencyVisitor;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
            final String[] exceptions) {
        if (signature == null) {
            ClassDefinition.current().addMethodReturnTypes(getReturnType(desc));
            ClassDefinition.current().addMethodParameters(getTypes(Type.getArgumentTypes(desc)));
        } else {
            new SignatureReader(signature).accept(methodSignatureDependencyVisitor);
        }
        if (exceptions != null) {
            for (String e : exceptions) {
                ClassDefinition.current().addMethodException(getObjectType(e));
            }
        }
        return methodDependencyVisitor;
    }

    private static String getObjectType(String e) {
        return getType(Type.getObjectType(e));
    }

    private static Collection<String> getTypes(Type[] types) {
        Collection<String> c = new ArrayList<>();
        for (Type type : types) {
            c.add(getType(type));
        }
        return c;
    }

    private static Collection<String> getObjectTypes(String... names) {
        Collection<String> c = new ArrayList<>();
        for (String name : names) {
            c.add(getObjectType(name));
        }
        return c;
    }

    private static String getSimpleType(String name) {
        Type type = Type.getType(name);
        return getType(type);
    }

    private static String getType(Type type) {
        switch (type.getSort()) {
        case Type.ARRAY:
            return type.getElementType().getClassName();
        case Type.OBJECT:
            return type.getClassName();
        case Type.LONG:
            return Long.class.getName();
        case Type.INT:
            return Integer.class.getName();
        case Type.SHORT:
            return Short.class.getName();
        case Type.CHAR:
            return Character.class.getName();
        case Type.BYTE:
            return Byte.class.getName();
        case Type.DOUBLE:
            return Double.class.getName();
        case Type.FLOAT:
            return Float.class.getName();
        case Type.BOOLEAN:
            return Boolean.class.getName();
        case Type.VOID:
            return Void.class.getName();
        default:
            return type.getClassName();
        }
    }

    private static String getReturnType(final String desc) {
        return getType(Type.getReturnType(desc));
    }

    public static class MyAnnotationDependencyVisitor extends AnnotationVisitor {
        public MyAnnotationDependencyVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public void visit(final String name, final Object value) {
        }

        @Override
        public void visitEnum(final String name, final String desc, final String value) {
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String name, final String desc) {
            return this;
        }

        @Override
        public AnnotationVisitor visitArray(final String name) {
            return this;
        }
    }

    public static class MyFieldDependencyVisitor extends FieldVisitor {
        @Inject
        private MyAnnotationDependencyVisitor annotationDependencyVisitor;

        public MyFieldDependencyVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return annotationDependencyVisitor;
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String desc,
                final boolean visible) {
            return annotationDependencyVisitor;
        }
    }

    public static class MyMethodDependencyVisitor extends MethodVisitor {
        @Inject
        private MyAnnotationDependencyVisitor annotationDependencyVisitor;

        @Inject
        private MyMethodLocalVariableSignatureDependencyVisitor methodLocalVariableSignatureDependencyVisitor;

        public MyMethodDependencyVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
            return annotationDependencyVisitor;
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
            return annotationDependencyVisitor;
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String desc,
                final boolean visible) {
            return annotationDependencyVisitor;
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc,
                final boolean visible) {
            return annotationDependencyVisitor;
        }

        @Override
        public void visitTypeInsn(final int opcode, final String type) {
        }

        @Override
        public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        }

        @Override
        public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc,
                final boolean itf) {
        }

        @Override
        public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        }

        @Override
        public void visitLdcInsn(final Object cst) {
        }

        @Override
        public void visitMultiANewArrayInsn(final String desc, final int dims) {
        }

        @Override
        public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
            return annotationDependencyVisitor;
        }

        @Override
        public void visitLocalVariable(final String name, final String desc, final String signature, final Label start,
                final Label end, final int index) {
            if (signature != null) {
                new SignatureReader(signature).acceptType(methodLocalVariableSignatureDependencyVisitor);
            } else if (!name.equals("this")) {
                ClassDefinition.current().addMethodLocalVariable(getSimpleType(desc));
            }
        }

        @Override
        public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start,
                Label[] end, int[] index, String desc, boolean visible) {
            return annotationDependencyVisitor;
        }

        @Override
        public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        }

        @Override
        public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
            return annotationDependencyVisitor;
        }
    }

    public static class MyMethodLocalVariableSignatureDependencyVisitor extends SignatureVisitor {
        public MyMethodLocalVariableSignatureDependencyVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public void visitClassType(final String name) {
            String s = getSimpleType(name);
            if (s == null) {
                s = getObjectType(name);
            }
            if (s == null || s.equals("null") || s.equals("void")) {
                System.err.println("null: " + name);
            } else {
                ClassDefinition.current().addMethodLocalVariable(s);
            }
        }

        @Override
        public void visitInnerClassType(final String name) {
        }
    }

    public static class MyFieldSignatureDependencyVisitor extends SignatureVisitor {
        public MyFieldSignatureDependencyVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public void visitClassType(final String name) {
            String s = getSimpleType(name);
            if (s == null) {
                s = getObjectType(name);
            }
            ClassDefinition.current().addMember(s);
        }

        @Override
        public void visitInnerClassType(final String name) {
        }
    }

    public static class MyMethodSignatureDependencyVisitor extends SignatureVisitor {
        public MyMethodSignatureDependencyVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public void visitClassType(final String name) {
            String s = getSimpleType(name);
            if (s == null) {
                s = getObjectType(name);
            }
            ClassDefinition.current().addMethodLocalVariable(s);
        }

        @Override
        public void visitInnerClassType(final String name) {
        }
    }
}
