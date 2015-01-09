package se.eris.asm;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
* Collects class info. That is name, modifiers, super class, and interfaces.
*/
public class ClassInfoVisitor extends ClassVisitor {

    private ClassInfo classInfo;

    public ClassInfoVisitor() {
        super(Opcodes.ASM5);
    }

    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        classInfo = new ClassInfo(version, access, name, signature, superName, interfaces);
    }

    @NotNull
    public ClassInfo getClassInfo() {
        if (classInfo == null) {
            throw new RuntimeException(ClassInfo.class.getSimpleName() + " has not benn initialized (i.e. visit(...) has not yet been called)");
        }
        return classInfo;
    }
}
