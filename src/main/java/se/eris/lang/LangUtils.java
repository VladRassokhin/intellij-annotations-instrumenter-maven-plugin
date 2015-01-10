package se.eris.lang;

import org.jetbrains.annotations.NotNull;

public final class LangUtils {

    private LangUtils() {
    }

    @NotNull
    public static String convertToJavaClassName(@NotNull final String cls) {
        return "L" + cls.replace(".", "/") + ";";
    }

}
