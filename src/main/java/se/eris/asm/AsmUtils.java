package se.eris.asm;

public final class AsmUtils {

    private AsmUtils() {
    }

    /**
     * @return the Java version as an integer (ie. 1, 2, 3, ... 8)
     */
    public static int asmOpcodeToJavaVersion(final int versionOpcode) {
        return versionOpcode % (3 << 16) - 44;
    }

}
