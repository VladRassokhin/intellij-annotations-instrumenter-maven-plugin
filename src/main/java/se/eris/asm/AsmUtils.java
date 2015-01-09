package se.eris.asm;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

public final class AsmUtils {

    private AsmUtils() {
    }

    /**
     * @return the Java version as an integer (ie. 1, 2, 3, ... 8)
     */
    public static int asmOpcodeToJavaVersion(final int versionOpcode) {
        return versionOpcode % (3 << 16) - 44;
    }

    public static boolean isReferenceType(@NotNull final Type type) {
        return type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY;
    }

}
