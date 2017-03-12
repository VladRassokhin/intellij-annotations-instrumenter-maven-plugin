package se.eris.notnull;

import se.eris.notnull.instrumentation.ClassMatcher;

import java.util.HashSet;
import java.util.Set;

public class ExcludeConfiguration {

    private final Set<ClassMatcher> excludes;

    public ExcludeConfiguration(final Set<ClassMatcher> excludes) {
        this.excludes = new HashSet<>(excludes);
    }

    public boolean isClassImplicitInstrumentation(final CharSequence className) {
        for (final ClassMatcher classMatcher : excludes) {
            if (classMatcher.matches(className)) {
                return false;
            }
        }
        return true;
    }

}
