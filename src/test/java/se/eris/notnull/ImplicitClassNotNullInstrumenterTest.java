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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class ImplicitClassNotNullInstrumenterTest {

    private static final File SRC_DIR = new File("src/test/data");
    private static final Path CLASSES_DIRECTORY = new File("target/test/data/classes").toPath();

    private static final String CLASS_NAME = "TestClassImplicit$1";
    private static final String TEST_CLASS = "se.eris.implicit." + CLASS_NAME;

    private static final File TEST_FILE = new File(SRC_DIR, "se/eris/implicit/" + CLASS_NAME + ".java");

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static TestCompiler compiler;

    /**
     * @return single-quoted parameter name if compiler supports `-parameters` option, empty string otherwise.
     */
    @NotNull
    private static String maybeName(@NotNull final String parameterName) {
        return compiler.parametersOptionSupported() ? String.format(" (parameter '%s')", parameterName) : "";
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        compiler = TestCompiler.create(CLASSES_DIRECTORY);
        compiler.compile(TEST_FILE);

        final Configuration configuration = new Configuration(false,
                new AnnotationConfiguration(notnull(), nullable()),
                new ExcludeConfiguration(Collections.<ClassMatcher>emptySet()));
        final NotNullInstrumenter instrumenter = new NotNullInstrumenter(new NopLogWrapper());
        final int numberOfInstrumentedFiles = instrumenter.addNotNullAnnotations(CLASSES_DIRECTORY, configuration, Collections.<URL>emptyList());

        assertThat(numberOfInstrumentedFiles, greaterThan(0));
    }

    @NotNull
    private static Set<String> nullable() {
        final Set<String> annotations = new HashSet<>();
        annotations.add("org.jetbrains.annotations.Nullable");
        return annotations;
    }

    @NotNull
    private static Set<String> notnull() {
        final Set<String> annotations = new HashSet<>();
        annotations.add("org.jetbrains.annotations.NotNull");
        return annotations;
    }

    @Test
    public void notNullAnnotatedParameter_shouldValidate() throws Exception {
        final Class<?> c = compiler.getCompiledClass(TEST_CLASS);
        final Method implicitReturn = c.getMethod("implicitReturn", String.class);
        ReflectionUtil.simulateMethodCall(implicitReturn, "should work");

        exception.expect(IllegalStateException.class);
        exception.expectMessage("NotNull method se/eris/implicit/" + CLASS_NAME + ".implicitReturn must not return null");
        ReflectionUtil.simulateMethodCall(implicitReturn, new Object[]{null});
    }

    @Test
    public void implicitParameter_shouldValidate() throws Exception {
        final Class<?> c = compiler.getCompiledClass(TEST_CLASS);
        final Method implicitParameterMethod = c.getMethod("implicitParameter", String.class);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(is(
            "Implicit NotNull argument 0" + maybeName("s") + " of se/eris/implicit/" + CLASS_NAME + ".implicitParameter must not be null"
        ));
        ReflectionUtil.simulateMethodCall(implicitParameterMethod, new Object[]{null});
    }

    @Test
    public void implicitConstructorParameter_shouldValidate() throws Exception {
        final Class<?> c = compiler.getCompiledClass(TEST_CLASS);
        final Constructor<?> implicitParameterConstructor = c.getConstructor(String.class);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(is(
            "Implicit NotNull argument 0" + maybeName("s") + " of se/eris/implicit/" + CLASS_NAME + ".<init> must not be null"
        ));
        ReflectionUtil.simulateConstructorCall(implicitParameterConstructor, new Object[]{null});
    }

    @Test
    public void anonymousClassConstructor_shouldNotBeInstrumented() throws Exception {
        final Class<?> c = compiler.getCompiledClass(TEST_CLASS);
        final Method anonymousClassNullable = c.getMethod("anonymousClassNullable");
        ReflectionUtil.simulateMethodCall(anonymousClassNullable);

        final Method anonymousClassNotNull = c.getMethod("anonymousClassNotNull");
        ReflectionUtil.simulateMethodCall(anonymousClassNotNull);
    }

}