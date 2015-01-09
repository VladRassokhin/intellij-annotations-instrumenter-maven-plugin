package se.eris.asm;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

public final class AsmUtils {

    private AsmUtils() {
    }

    public static boolean isReferenceType(@NotNull final Type type) {
        final int typeSort = type.getSort();
        return typeSort == Type.OBJECT || typeSort == Type.ARRAY;
    }

    public static boolean javaVersionSupportsAnnotations(final int opcodeVersion) {
        return asmOpcodeToJavaVersion(opcodeVersion) >= 5;
    }

    /**
     * @return the Java version as an integer (ie. 1, 2, 3, ... 8)
     */
    public static int asmOpcodeToJavaVersion(final int versionOpcode) {
        return versionOpcode % (3 << 16) - 44;
    }
}
