package se.eris.notnull.instrumentation;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public final class ClassMatcher {

    private static final StringReplacer PATTERN_REPLACER = StringReplacer.init()
            .prefix("^")
            .add("$", "\\$")
            .add(".**", "(\\.[^\\.]*)*")
            .add(".", "\\.")
            .add("**", ".*")
            .add("*", "[^\\.]*")
            .sufix("$")
            .build();

    @NotNull
    private final Pattern pattern;

    @NotNull
    public static ClassMatcher namePattern(@NotNull final String classNamePattern) {
        final String pattern = PATTERN_REPLACER.apply(classNamePattern);
        return new ClassMatcher(Pattern.compile(pattern));
    }

    private ClassMatcher(@NotNull final Pattern pattern) {
        this.pattern = pattern;
    }

    @SuppressWarnings("ControlFlowStatementWithoutBraces")
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ClassMatcher aClassMatcher = (ClassMatcher) o;

        return pattern.equals(aClassMatcher.pattern);
    }

    @Override
    public int hashCode() {
        return pattern.hashCode();
    }

    public boolean matches(final CharSequence classFileName) {
        return pattern.matcher(classFileName).matches();
    }

}
