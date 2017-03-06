package se.eris.implicit;

import org.jetbrains.annotations.Nullable;
import se.eris.notnull.ImplicitNotNull;

@ImplicitNotNull
public class TestClassImplicit$1 {

    public static String implicitReturn(@Nullable final String s) {
        return s;
    }

    public static void implicitParameter(final String s) {
    }

    public static void anonymousClassNullable() {
        new Foo((String) null) {}; // anonymous class - no way to annotate constructor parameters
    }

    public static void anonymousClassNotNull() {
        new Foo((Integer) null) {}; // anonymous class - no way to annotate constructor parameters
    }

    private static class Foo {

        Foo(@Nullable String s) {}

        Foo(Integer i) {}

    }

    public TestClassImplicit$1(String s) {
    }

}
