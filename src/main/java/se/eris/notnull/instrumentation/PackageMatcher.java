package se.eris.notnull.instrumentation;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class PackageMatcher {

    @NotNull
    private final Pattern pattern;

    @NotNull
    public static PackageMatcher fromPackage(@NotNull final String aPackage) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < aPackage.length(); i++) {
            final char charAtI = aPackage.charAt(i);
            if (charAtI == '.') {
                sb.append("\\.");
            } else if (charAtI == '*') {
                if (isNextCharWildcard(aPackage, i)) {
                    sb.append(".*");
                    i++;
                } else {
                    sb.append("[^\\.]*");
                }
            } else {
                sb.append(charAtI);
            }
        }
        return new PackageMatcher(Pattern.compile("^" + sb.toString() + "$"));
    }

    private static boolean isNextCharWildcard(@NotNull final String s, final int i) {
        return hasCharsAfter(s, i) && s.charAt(i + 1) == '*';
    }

    private static boolean hasCharsAfter(@NotNull final String s, final int i) {
        return i + 1 < s.length();
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
