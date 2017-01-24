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
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NotNullConfiguration {

    private final boolean implicit;
    @NotNull
    private final Set<String> annotations;
    @NotNull
    private final Set<String> nullable;

    @SuppressWarnings("BooleanParameter")
    public NotNullConfiguration(
            final boolean implicit,
            @NotNull final Set<String> annotations,
            @NotNull final Set<String> nullable)
    {
        this.implicit = implicit;
        if (implicit) {
            if (annotations.isEmpty() && nullable.isEmpty()) {
                this.nullable = Collections.singleton(Nullable.class.getName());
            } else {
                this.nullable = Collections.unmodifiableSet(join(annotations, nullable));
            }
            this.annotations = Collections.emptySet();
        } else {
            if (annotations.isEmpty()) {
                this.annotations = Collections.singleton(NotNull.class.getName());
            } else {
                this.annotations = Collections.unmodifiableSet(new HashSet<>(annotations));
            }
            this.nullable = Collections.unmodifiableSet(new HashSet<>(nullable));
        }
    }

    @NotNull
    private Set<String> join(@NotNull final Collection<String> c1, @NotNull final Collection<String> c2) {
        final Set<String> all = new HashSet<>(c1.size() + c2.size());
        all.addAll(c1);
        all.addAll(c2);
        return all;
    }

    public boolean isImplicit() {
        return implicit;
    }

    @NotNull
    public Set<String> getAnnotations() {
        return annotations;
    }

    @NotNull
    public Set<String> getNotNullAnnotations() {
        return annotations;
    }

    @NotNull
    public Set<String> getNullableAnnotations() {
        return nullable;
    }

}
