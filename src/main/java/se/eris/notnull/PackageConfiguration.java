package se.eris.notnull;

import se.eris.notnull.instrumentation.PackageMatcher;

import java.util.HashSet;
import java.util.Set;

public class PackageConfiguration {

    private final Set<PackageMatcher> exclude;

    public PackageConfiguration(final Set<PackageMatcher> exclude) {
        this.exclude = new HashSet<>(exclude);
    }

    public boolean isPackageImplicitInstrumentation(final String packageName) {
        for (final PackageMatcher packageMatcher : exclude) {
            if (packageMatcher.matches(packageName)) {
                return false;
            }
        }
        return true;
    }

}
