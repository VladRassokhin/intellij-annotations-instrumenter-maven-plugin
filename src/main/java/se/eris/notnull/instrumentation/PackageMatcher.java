package se.eris.notnull.instrumentation;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class PackageMatcher {

    @NotNull
    private final Pattern pattern;

    @NotNull
    public static PackageMatcher fromPackage(@NotNull final String aPackage) {
        final StringWorker worker = new StringWorker(aPackage);
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < worker.length(); i++) {
            if (worker.isString(i, ".**")) {
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
                sb.append(aPackage.charAt(i));
            }
        }
        return new PackageMatcher(Pattern.compile("^" + sb.toString() + "$"));
    }

    private PackageMatcher(@NotNull final Pattern pattern) {
        this.pattern = pattern;
    }

    @SuppressWarnings("ControlFlowStatementWithoutBraces")
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final PackageMatcher aPackageMatcher = (PackageMatcher) o;

        return pattern.equals(aPackageMatcher.pattern);
    }

    @Override
    public int hashCode() {
        return pattern.hashCode();
    }

    public boolean matches(final String packageName) {
        return pattern.matcher(packageName).matches();
    }
}
