package se.eris.notnull.instrumentation;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class PackageMatcher {

    @NotNull
    private final Pattern pattern;

    @NotNull
    public static PackageMatcher fromPackage(@NotNull final String aPackage) {
        return new PackageMatcher(Pattern.compile("^"+aPackage
                .replace(".", "\\.")
                .replaceAll("([^\\*])\\*", "$1[^\\.]*")
                .replace("**", ".*")
                + "$"));
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
