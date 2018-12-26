/*
 * Copyright 2013-2015 Eris IT AB
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
import se.eris.util.TestClass;
import se.eris.util.TestCompiler;
import se.eris.util.TestCompilerOptions;
import se.eris.util.compiler.JavaSystemCompilerUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class AnnotationNotNullInstrumenterTest {

    private static final File SRC_DIR = new File("src/test/data");
    private static final File TARGET_DIR = new File("target/test/data/classes");

    private static final TestClass TEST_CLASS = new TestClass("se.eris.test.TestNotNull");

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static TestCompiler compiler;

    @BeforeClass
    public static void beforeClass() throws MalformedURLException {
        compiler = TestCompiler.create(TestCompilerOptions.from(TARGET_DIR.toPath(), "1.8"));
        compiler.compile(TEST_CLASS.getJavaFile(SRC_DIR));

        final Configuration configuration = new Configuration(false, new AnnotationConfiguration(notNull(), Collections.<String>emptySet()), new ExcludeConfiguration(Collections.<ClassMatcher>emptySet()));
        final NotNullInstrumenter instrumenter = new NotNullInstrumenter(new NopLogWrapper());
        final int numberOfInstrumentedFiles = instrumenter.addNotNullAnnotations(TARGET_DIR.toPath(), configuration, Collections.<URL>emptyList());

        assertThat(numberOfInstrumentedFiles, greaterThan(0));
    }

    @NotNull
    private static Set<String> notNull() {
        final Set<String> annotations = new HashSet<>();
        annotations.add("org.jetbrains.annotations.NotNull");
        annotations.add("java.lang.Deprecated");
        return annotations;
    }

    /**
     * @return single-quoted parameter name if compiler supports `-parameters` option, empty string otherwise.
     */
    @NotNull
    private static String maybeName(@NotNull final String parameterName) {
        return JavaSystemCompilerUtil.supportParametersOption() ? String.format(" (parameter '%s')", parameterName) : "";
    }

    @Test
    public void annotatedParameter_shouldValidate() throws Exception {
        final Class<?> c = compiler.getCompiledClass(TEST_CLASS.getName());
        final Method notNullParameterMethod = c.getMethod("notNullParameter", String.class);
        ReflectionUtil.simulateMethodCall(notNullParameterMethod, "should work");

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(is(
                "NotNull annotated argument 0" + maybeName("s") +
                " of " + TEST_CLASS.getAsmName() + ".notNullParameter must not be null"));
        ReflectionUtil.simulateMethodCall(notNullParameterMethod, new Object[]{null});
    }

    @Test
    public void notnullReturn_shouldValidate() throws Exception {
        final Class<?> c = compiler.getCompiledClass(TEST_CLASS.getName());
        final Method notNullReturnMethod = c.getMethod("notNullReturn", String.class);
        ReflectionUtil.simulateMethodCall(notNullReturnMethod, "should work");

        exception.expect(IllegalStateException.class);
        exception.expectMessage("NotNull method " + TEST_CLASS.getAsmName() + ".notNullReturn must not return null");
        ReflectionUtil.simulateMethodCall(notNullReturnMethod, new Object[]{null});
    }

    @Test
    public void annotatedReturn_shouldValidate() throws Exception {
        final Class<?> c = compiler.getCompiledClass(TEST_CLASS.getName());
        final Method notNullReturnMethod = c.getMethod("annotatedReturn", String.class);
        ReflectionUtil.simulateMethodCall(notNullReturnMethod, "should work");

        exception.expect(IllegalStateException.class);
        exception.expectMessage("NotNull method " + TEST_CLASS.getAsmName() + ".annotatedReturn must not return null");
        ReflectionUtil.simulateMethodCall(notNullReturnMethod, new Object[]{null});
    }

    @Test
    public void overridingMethod_isInstrumented() throws Exception {
        final Class<?> subargClass = compiler.getCompiledClass(TEST_CLASS.getName() +"$Subarg");
        final Class<?> subClass = compiler.getCompiledClass(TEST_CLASS.getName() +"$Sub");
        final Method specializedMethod = subClass.getMethod("overload", subargClass);
        assertFalse(specializedMethod.isSynthetic());
        assertFalse(specializedMethod.isBridge());
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(is(
                "NotNull annotated argument 0" +  maybeName("s") +
                        " of " + TEST_CLASS.getAsmName() + "$Sub.overload must not be null"
        ));
        ReflectionUtil.simulateMethodCall(subClass.newInstance(), specializedMethod, new Object[]{null});
    }

}