package se.eris;

import org.jetbrains.annotations.NotNull;

public final class TestClasses {

    private static final String[] simpleNotNullClass = {
            "    package se.eris.test;                                                                     ",
            "                                                                                              ",
            "    import java.util.Arrays;                                                                  ",
            "    import org.jetbrains.annotations.NotNull;                                                 ",
            "                                                                                              ",
            "    public class TestNotNull {                                                                ",
            "                                                                                              ",
            "        public static void notNullParameter(@NotNull final String s) {                        ",
            "        }                                                                                     ",
            "                                                                                              ",
            "        @NotNull                                                                              ",
            "        public static String notNullReturn(final String s) {                                  ",
            "            return s;                                                                         ",
            "        }                                                                                     ",
            "                                                                                              ",
            "    }                                                                                         ",
    };

    @NotNull
    public static String getSimpleNotNullClass() {
        return joinClassLines(simpleNotNullClass);
    }

    @NotNull
    private static String joinClassLines(@NotNull final String... lines) {
        return String.join("\n", lines);
    }

}
