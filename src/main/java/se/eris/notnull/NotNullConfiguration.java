/*
 * Copyright 2013-2016 Eris IT AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.eris.notnull;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class NotNullConfiguration {

    private final boolean implicit;
    @NotNull
    private final Set<String> notNull;
    @NotNull
    private final Set<String> nullable;

    @SuppressWarnings("BooleanParameter")
    public NotNullConfiguration(
            final boolean implicit,
            @NotNull final Set<String> notNull,
            @NotNull final Set<String> nullable) {
        this.implicit = implicit;
        if (isAnnotationsConfigured(notNull, nullable)) {
            this.nullable = nullable;
            this.notNull = notNull;
        } else {
            this.nullable = getDefaultNullable();
            this.notNull = getDefaultNotNull();
        }
    }

    @NotNull
    private Set<String> getDefaultNotNull() {
        return new HashSet<>(Arrays.asList(
                org.jetbrains.annotations.NotNull.class.getName(),
                se.eris.notnull.NotNull.class.getName())
        );
    }

    @NotNull
    private Set<String> getDefaultNullable() {
        return new HashSet<>(Arrays.asList(
                org.jetbrains.annotations.Nullable.class.getName(),
                se.eris.notnull.Nullable.class.getName())
        );
    }

    private boolean isAnnotationsConfigured(@NotNull final Collection<String> notNull, @NotNull final Collection<String> nullable) {
        return !notNull.isEmpty() || !nullable.isEmpty();
    }

    public boolean isImplicit() {
        return implicit;
    }

    @NotNull
    public Set<String> getNotNullAnnotations() {
        return notNull;
    }

    @NotNull
    public Set<String> getNullableAnnotations() {
        return nullable;
    }

}
