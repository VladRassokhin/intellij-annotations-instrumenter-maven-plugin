package se.eris.exclude;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestExclude {

    public static void notNullParameter(@NotNull final String s) {
    }

    public static void unAnnotatedParameter(final String s) {
    }

    @NotNull
    public static String notNullReturn(final String s) {
        return s;
    }

    public static String unAnnotatedReturn(final String s) {
        return s;
    }

}
