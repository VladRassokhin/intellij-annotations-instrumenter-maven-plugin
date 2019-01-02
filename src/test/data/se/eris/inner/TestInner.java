package se.eris.inner;

import org.jetbrains.annotations.*;

public class TestInner {

    public TestInner(String string, Integer inner, Integer nested) {
        new InnerClass(string, inner);
        new NestedClass(string, nested);
    }

    public InnerClass getInner() {
        return new InnerClass(null, 17);
    }

    public class InnerClass {
        public InnerClass(@Nullable String nullable, @NotNull Integer notNull) {
        }

        public String innerMethod(@Nullable String innerNullable, @NotNull Integer innerNotNull) {
            return innerNullable;
        }
    }

    public static class NestedClass {
        public NestedClass(@Nullable String nullable, @NotNull Integer notNull) {
        }
    }

}