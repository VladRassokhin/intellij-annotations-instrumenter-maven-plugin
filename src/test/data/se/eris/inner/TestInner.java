package se.eris.inner;

import org.jetbrains.annotations.*;

public class TestInner {

    public TestInner(String string, Integer inner, Integer nested) {
        new InnerClass(string, inner);
        new NestedClass(string, nested);
    }

    public class InnerClass {
        public InnerClass(@Nullable String nullable, @NotNull Integer notNull) {
        }
    }

    public static class NestedClass {
        public NestedClass(@Nullable String nullable, @NotNull Integer notNull) {
        }
    }

}