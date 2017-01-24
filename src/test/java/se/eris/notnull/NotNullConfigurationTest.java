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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class NotNullConfigurationTest {

    private static final String ORG_JETBRAINS_ANNOTATIONS_NOT_NULL = "org.jetbrains.annotations.NotNull";
    private static final String ORG_JETBRAINS_ANNOTATIONS_NULLABLE = "org.jetbrains.annotations.Nullable";
    private static final String SE_ERIS_NULLABLE = "se.eris.Nullable";

    @Test
    public void getAnnotations_default() {
        assertThat(new NotNullConfiguration(false, Collections.<String>emptySet(), Collections.<String>emptySet()).getNotNullAnnotations().iterator().next(), is(ORG_JETBRAINS_ANNOTATIONS_NOT_NULL));
    }

    @Test
    public void getAnnotations_defaultImplicit() {
        assertThat(new NotNullConfiguration(true, Collections.<String>emptySet(), Collections.<String>emptySet()).getNullableAnnotations().iterator().next(), is(ORG_JETBRAINS_ANNOTATIONS_NULLABLE));
    }

    @Test
    public void getAnnotations_implicit_shouldMergeLists() {
        assertThat(new NotNullConfiguration(true, Collections.singleton(SE_ERIS_NULLABLE), Collections.singleton(ORG_JETBRAINS_ANNOTATIONS_NULLABLE)).getNullableAnnotations(), containsInAnyOrder(SE_ERIS_NULLABLE, ORG_JETBRAINS_ANNOTATIONS_NULLABLE));
    }

}