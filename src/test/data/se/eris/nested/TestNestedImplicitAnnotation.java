package se.eris.nested;

import org.jetbrains.annotations.Nullable;
import se.eris.notnull.ImplicitNotNull;

@ImplicitNotNull
public class TestNestedImplicitAnnotation {

    public TestNestedImplicitAnnotation(String s) {
    }

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

    public static class Superarg {}
    public static class Subarg extends Superarg {}

    public static class Super<S extends Superarg> {
        public void overload(S s) {}
    }

    public static class Sub extends Super<Subarg> {
        @Override
        public void overload(Subarg s) {}
    }

    public static class NestedClassesSegmentIsPreserved {
        public static class ASub {}
    }


}
