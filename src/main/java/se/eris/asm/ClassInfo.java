package se.eris.asm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;

/**
* Collects class info. That is name, modifiers, super class, and interfaces.
*/
public class ClassInfo {

    private final int version;
    private final int access;
    @NotNull
    private final String name;
    @NotNull
    private final String signature;
    @Nullable
    private final String superName;
    @Nullable
    private final String[] interfaces;

    public ClassInfo(final int version, final int access, @NotNull final String name, @NotNull final String signature, @Nullable final String superName, @Nullable final String[] interfaces) {
        this.version = version;
        this.access = access;
        this.name = name;
        this.signature = signature;
        this.superName = superName;
        this.interfaces = interfaces;
    }

    public int getVersion() {
        return version;
    }

    public int getAccess() {
        return access;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getSignature() {
        return signature;
    }

    @Nullable
    public String getSuperName() {
        return superName;
    }

    @Nullable
    public String[] getInterfaces() {
        return interfaces;
    }

    public boolean isInterface() {
        return (access & Opcodes.ACC_INTERFACE) > 0;
    }
}
