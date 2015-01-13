package se.eris.test;

import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

public class TestNotNull {

    public static void notNullParameter(@NotNull final String s) {
    }

    @NotNull
    public static String notNullReturn(final String s) {
        return s;
    }

}
