package se.eris.implicit;

import org.jetbrains.annotations.Nullable;
import se.eris.notnull.ImplicitNotNull;

@ImplicitNotNull
public class TestClassImplicit {

    public static String implicitReturn(@Nullable final String s) {
        return s;
    }

    public static void implicitParameter(final String s) {
    }

    public static void anonymousClassNullable() {
        new Abstract((String) null) {}; // anonymous class - no way to annotate constructor parameters
    }

    public static void anonymousClassNotNull() {
        new Abstract((Integer) null) {}; // anonymous class - no way to annotate constructor parameters
    }

}
