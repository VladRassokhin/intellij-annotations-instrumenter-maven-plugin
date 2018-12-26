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
package se.eris.version;

import com.intellij.NotNullInstrumenter;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import se.eris.maven.NopLogWrapper;
import se.eris.notnull.AnnotationConfiguration;
import se.eris.notnull.Configuration;
import se.eris.notnull.ExcludeConfiguration;
import se.eris.notnull.instrumentation.ClassMatcher;
import se.eris.util.ReflectionUtil;
import se.eris.util.TestClass;
import se.eris.util.TestCompiler;
import se.eris.util.TestCompilerOptions;
import se.eris.util.TestSupportedJavaVersions;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImplicitInstrumenterTest {

    private static final String[] VERSIONS = TestSupportedJavaVersions.SUPPORTED_VERSIONS;
    public static final String JAVA_7 = "1.7";

    private static final File SRC_DIR = new File("src/test/data");
    private static final Path DESTINATION_BASEDIR = new File("target/test/data/classes").toPath();

    private static final TestClass testClass = new TestClass("se.eris.version.Implicit");

    private static final Map<String, TestCompiler> compilers = new HashMap<>();

    /**
     * @return single-quoted parameter name if compilers supports `-parameters` option, empty string otherwise.
     */
    @NotNull
    private static String maybeName(final TestCompiler testCompiler, @NotNull final String parameterName) {
        return testCompiler.hasParametersSupport() ? String.format(" (parameter '%s')", parameterName): "";
    }

    @BeforeAll
    static void beforeClass() {
        for (final String version : VERSIONS) {
            final Path destination = DESTINATION_BASEDIR.resolve(version);
            final TestCompiler compiler = TestCompiler.create(TestCompilerOptions.from(destination, version));
            compiler.compile(testClass.getJavaFile(SRC_DIR));
            compilers.put(version, compiler);

            final Configuration configuration = new Configuration(false,
                    new AnnotationConfiguration(notnull(), nullable()),
                    new ExcludeConfiguration(Collections.<ClassMatcher>emptySet()));
            final NotNullInstrumenter instrumenter = new NotNullInstrumenter(new NopLogWrapper());
            final int numberOfInstrumentedFiles = instrumenter.addNotNullAnnotations(destination, configuration, Collections.emptyList());

            assertTrue(numberOfInstrumentedFiles > 0);
        }
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

    @TestSupportedJavaVersions
    void notNullAnnotatedParameter_shouldValidate(final String javaVersion) throws Exception {
        final Class<?> c = compilers.get(javaVersion).getCompiledClass(testClass.getName());
        final Method implicitReturn = c.getMethod("implicitReturn", String.class);
        ReflectionUtil.simulateMethodCall(implicitReturn, "should work");

        final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> ReflectionUtil.simulateMethodCall(implicitReturn, new Object[]{null}));
        assertEquals(exception.getMessage(), String.format("NotNull method se/eris/version/%s.implicitReturn must not return null", testClass.getSimpleName()));
    }

    @TestSupportedJavaVersions
    void implicitParameter_shouldValidate(final String javaVersion) throws Exception {
        final Class<?> c = compilers.get(javaVersion).getCompiledClass(testClass.getName());
        final Method implicitParameterMethod = c.getMethod("implicitParameter", String.class);

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> ReflectionUtil.simulateMethodCall(implicitParameterMethod, new Object[]{null}));
        final String expected = String.format("Implicit NotNull argument 0%s of se/eris/version/%s.implicitParameter must not be null", maybeName(compilers.get(javaVersion), "s"), testClass.getSimpleName());
        assertEquals(expected, exception.getMessage());
    }

    @TestSupportedJavaVersions
    void implicitConstructorParameter_shouldValidate(final String javaVersion) throws Exception {
        final Class<?> c = compilers.get(javaVersion).getCompiledClass(testClass);
        final Constructor<?> implicitParameterConstructor = c.getConstructor(String.class);

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> ReflectionUtil.simulateConstructorCall(implicitParameterConstructor, new Object[]{null}));
        assertEquals("Implicit NotNull argument 0" + maybeName(compilers.get(javaVersion), "s") + " of se/eris/version/" + testClass.getSimpleName() + ".<init> must not be null", exception.getMessage());
    }

    @TestSupportedJavaVersions
    void anonymousClassConstructor_shouldNotBeInstrumented(final String javaVersion) throws Exception {
        final Class<?> c = compilers.get(javaVersion).getCompiledClass(testClass.getName());
        final Method anonymousClassNullable = c.getMethod("anonymousClassNullable");
        ReflectionUtil.simulateMethodCall(anonymousClassNullable);

        final Method anonymousClassNotNull = c.getMethod("anonymousClassNotNull");
        ReflectionUtil.simulateMethodCall(anonymousClassNotNull);
    }

}