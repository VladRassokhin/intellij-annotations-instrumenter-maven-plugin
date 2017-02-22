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
import java.util.HashSet;
import java.util.Set;

public class Configuration {

    private final boolean implicit;
    @NotNull
    private final AnnotationConfiguration annotationConfiguration;
    @NotNull
    private final PackageConfiguration packageConfiguration;

    @SuppressWarnings("BooleanParameter")
    public Configuration(
            final boolean implicit,
            @NotNull final AnnotationConfiguration annotationConfiguration,
            @NotNull final PackageConfiguration packageConfiguration) {
        this.implicit = implicit;
        if (annotationConfiguration.isAnnotationsConfigured()) {
            this.annotationConfiguration = annotationConfiguration;
        } else {
            this.annotationConfiguration = new AnnotationConfiguration(getDefaultNotNull(), getDefaultNullable());
        }
        this.packageConfiguration = packageConfiguration;
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

    public boolean isImplicit() {
        return implicit;
    }

    @NotNull
    public Set<String> getNotNullAnnotations() {
        return annotationConfiguration.getNotNull();
    }

    @NotNull
    public Set<String> getNullableAnnotations() {
        return annotationConfiguration.getNullable();
    }

    public boolean isImplicitInstrumentation(final String packageName) {
        return implicit && packageConfiguration.isPackageImplicitInstrumentation(packageName);
    }
}
