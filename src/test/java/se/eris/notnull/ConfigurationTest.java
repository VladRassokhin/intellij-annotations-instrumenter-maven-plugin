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

import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class ConfigurationTest {

    private static final String ORG_JETBRAINS_ANNOTATIONS_NOT_NULL = org.jetbrains.annotations.NotNull.class.getName();
    private static final String ORG_JETBRAINS_ANNOTATIONS_NULLABLE = org.jetbrains.annotations.Nullable.class.getName();

    private static final String SE_ERIS_NOT_NULL = se.eris.notnull.NotNull.class.getName();
    private static final String SE_ERIS_NULLABLE = se.eris.notnull.Nullable.class.getName();

    @Test
    public void getAnnotations_defaultNotNull() {
        Configuration configuration = getDefaultNotNullConfiguration(false);
        assertThat(configuration.getNotNullAnnotations(), containsInAnyOrder(ORG_JETBRAINS_ANNOTATIONS_NOT_NULL, SE_ERIS_NOT_NULL));
        assertThat(configuration.isImplicit(), is(false));
    }

    @Test
    public void getAnnotations_defaultNullable() {
        Configuration configuration = getDefaultNotNullConfiguration(true);
        assertThat(configuration.getNullableAnnotations(), containsInAnyOrder(ORG_JETBRAINS_ANNOTATIONS_NULLABLE, SE_ERIS_NULLABLE));
        assertThat(configuration.isImplicit(), is(true));
    }

    private Configuration getDefaultNotNullConfiguration(boolean implicit) {
        return new Configuration(implicit, new AnnotationConfiguration(Collections.<String>emptySet(), Collections.<String>emptySet()));
    }

}