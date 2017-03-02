package se.eris.notnull;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public class AnnotationConfiguration {

    @NotNull
    private final Set<String> notNull;
    @NotNull
    private final Set<String> nullable;

    public AnnotationConfiguration(@NotNull final Set<String> notNull, @NotNull final Set<String> nullable) {
        this.notNull = notNull;
        this.nullable = nullable;
    }

    public AnnotationConfiguration() {
        notNull = Collections.emptySet();
        nullable = Collections.emptySet();
    }

    public boolean isAnnotationsConfigured() {
        return !notNull.isEmpty() || !nullable.isEmpty();
    }

    @NotNull
    public Set<String> getNotNull() {
        return notNull;
    }

    @NotNull
    public Set<String> getNullable() {
        return nullable;
    }

}
