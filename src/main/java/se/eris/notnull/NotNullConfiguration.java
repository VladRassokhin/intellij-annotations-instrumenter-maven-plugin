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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NotNullConfiguration {

    private final boolean implicit;
    @NotNull
    private final Set<String> annotations;

    public NotNullConfiguration(final boolean implicit, @NotNull final Set<String> annotations) {
        this.implicit = implicit;
        if (annotations.isEmpty()) {
            this.annotations = Collections.singleton(implicit ? Nullable.class.getName() : NotNull.class.getName());
        } else {
            this.annotations = Collections.unmodifiableSet(new HashSet<>(annotations));
        }
    }

    public boolean isImplicit() {
        return implicit;
    }

    @NotNull
    public Set<String> getAnnotations() {
        return annotations;
    }

}
