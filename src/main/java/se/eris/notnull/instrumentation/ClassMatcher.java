package se.eris.notnull.instrumentation;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public final class ClassMatcher {

    @NotNull
    private final Pattern pattern;

    @NotNull
    public static ClassMatcher namePattern(@NotNull final String classNamePattern) {
        final StringWorker worker = new StringWorker(classNamePattern);
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < worker.length(); i++) {
            if (worker.isString(i, "$")) {
                sb.append("\\$");
            } else if (worker.isString(i, ".**")) {
                sb.append("(\\.[^\\.]*)*");
                i += 2;
            } else if (worker.isChar(i, '.')) {
                sb.append("\\.");
            } else if (worker.isString(i, "**")) {
                sb.append(".*");
                i += 1;
            } else if (worker.isChar(i, '*')) {
                sb.append("[^\\.]*");
            } else {
                sb.append(classNamePattern.charAt(i));
            }
        }
        return new ClassMatcher(Pattern.compile("^" + sb.toString() + "$"));
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
