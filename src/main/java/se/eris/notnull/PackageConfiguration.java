package se.eris.notnull;

import se.eris.notnull.instrumentation.ClassMatcher;

import java.util.HashSet;
import java.util.Set;

public class PackageConfiguration {

    private final Set<ClassMatcher> exclude;

    public PackageConfiguration(final Set<ClassMatcher> exclude) {
        this.exclude = new HashSet<>(exclude);
    }

    public boolean isPackageImplicitInstrumentation(final CharSequence packageName) {
        for (final ClassMatcher classMatcher : exclude) {
            if (classMatcher.matches(packageName)) {
                return false;
            }
        }
        return true;
    }

}
