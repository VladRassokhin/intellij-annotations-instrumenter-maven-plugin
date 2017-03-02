package se.eris.notnull;

import se.eris.notnull.instrumentation.ClassMatcher;

import java.util.HashSet;
import java.util.Set;

public class ExcludeConfiguration {

    private final Set<ClassMatcher> exclude;

    public ExcludeConfiguration(final Set<ClassMatcher> exclude) {
        this.exclude = new HashSet<>(exclude);
    }

    public boolean isClassImplicitInstrumentation(final CharSequence className) {
        for (final ClassMatcher classMatcher : exclude) {
            if (classMatcher.matches(className)) {
                return false;
            }
        }
        return true;
    }

}
