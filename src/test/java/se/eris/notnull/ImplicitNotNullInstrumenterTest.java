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

import com.intellij.NotNullInstrumenter;
import org.jetbrains.annotations.NotNull;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.eris.maven.NopLogWrapper;
import se.eris.notnull.instrumentation.ClassMatcher;
import se.eris.util.ReflectionUtil;
import se.eris.util.TestCompiler;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

public class ImplicitNotNullInstrumenterTest {

    private static final File SRC_DIR = new File("src/test/data");
    private static final Path CLASSES_DIRECTORY = new File("target/test/data/classes").toPath();

    private static final String CLASS_NAME = "TestNotNull";

    private static final File TEST_FILE = new File(SRC_DIR, "se/eris/test/" + CLASS_NAME + ".java");
    private static final String TEST_CLASS = "se.eris.test." + CLASS_NAME;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static TestCompiler compiler;

    /** Returns single-quoted parameter name if compiler supports `-parameters` option, empty string otherwise. */
    @NotNull
    private static String maybeName(@NotNull String parameterName) {
        return compiler.parametersOptionSupported() ? String.format(" '%s'", parameterName) : "";
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        compiler = TestCompiler.create(CLASSES_DIRECTORY);
        compiler.compile(TEST_FILE);

        final Configuration configuration = new Configuration(true, new AnnotationConfiguration(Collections.<String>emptySet(), nullable()), new ExcludeConfiguration(Collections.<ClassMatcher>emptySet()));
        final NotNullInstrumenter instrumenter = new NotNullInstrumenter(new NopLogWrapper());
        final int numberOfInstrumentedFiles = instrumenter.addNotNullAnnotations(CLASSES_DIRECTORY, configuration, Collections.<URL>emptyList());

        assertThat(numberOfInstrumentedFiles, greaterThan(0));
    }

    @NotNull
    private static Set<String> nullable() {
        final Set<String> annotations = new HashSet<>();
        annotations.add("org.jetbrains.annotations.Nullable");
        annotations.add("java.lang.Deprecated");
        return annotations;
    }

    @Test
    public void notNullAnnotatedParameter_shouldValidate() throws Exception {
        final Class<?> c = compiler.getCompiledClass(TEST_CLASS);
        final Method notNullParameterMethod = c.getMethod("notNullParameter", String.class);
        ReflectionUtil.simulateMethodCall(notNullParameterMethod, "should work");
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(is(
            "Argument 0 for implicit NotNull parameter" + maybeName("s") + " of se/eris/test/" + CLASS_NAME + ".notNullParameter must not be null"
        ));
        ReflectionUtil.simulateMethodCall(notNullParameterMethod, new Object[]{null});
    }

    @Test
    public void implicitParameter_shouldValidate() throws Exception {
        final Class<?> c = compiler.getCompiledClass(TEST_CLASS);
        final Method implicitParameterMethod = c.getMethod("implicitParameter", String.class);
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(is(
            "Argument 0 for implicit NotNull parameter" + maybeName("s") + " of se/eris/test/" + CLASS_NAME + ".implicitParameter must not be null"
        ));
        ReflectionUtil.simulateMethodCall(implicitParameterMethod, new Object[]{null});
    }

    @Test
    public void nullableAnnotatedParameter_shouldNotValidate() throws Exception {
        final Class<?> c = compiler.getCompiledClass(TEST_CLASS);
        final Method implicitParameterMethod = c.getMethod("nullableParameter", String.class);
        ReflectionUtil.simulateMethodCall(implicitParameterMethod, new Object[]{null});
    }

    @Test
    public void implicitReturn_shouldValidate() throws Exception {
        final Class<?> c = compiler.getCompiledClass(TEST_CLASS);
        final Method notNullReturnMethod = c.getMethod("implicitReturn", String.class);
        ReflectionUtil.simulateMethodCall(notNullReturnMethod, "should work");
        exception.expect(IllegalStateException.class);
        exception.expectMessage("NotNull method se/eris/test/" + CLASS_NAME + ".implicitReturn must not return null");
        ReflectionUtil.simulateMethodCall(notNullReturnMethod, new Object[]{null});
    }

    @Test
    public void annotatedReturn_shouldNotValidate() throws Exception {
        final Class<?> c = compiler.getCompiledClass(TEST_CLASS);
        final Method notNullReturnMethod = c.getMethod("annotatedReturn", String.class);
        ReflectionUtil.simulateMethodCall(notNullReturnMethod, (String) null);
    }

    @Test
    public void innerClassWithoutConstructor_shouldWork() throws Exception {
        boolean syntheticConstructorFound = false;
        final Class<?> c = compiler.getCompiledClass(TEST_CLASS + "$Inner");
        for (final Constructor<?> constructor : c.getDeclaredConstructors()) {
            final boolean isSynthetic = constructor.isSynthetic();
            if (isSynthetic) {
                syntheticConstructorFound = true;
                constructor.setAccessible(true);
                constructor.newInstance(constructor.getGenericParameterTypes()[0].getClass().cast(null));
            }
        }
        assertThat(syntheticConstructorFound, is(true));
    }

}