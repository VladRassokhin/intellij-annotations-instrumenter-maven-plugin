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
import se.eris.util.ReflectionUtil;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ImplicitNotNullInstrumenterTest {

    private static final File SRC_DIR = new File("src/test/data");
    private static final File TARGET_DIR = new File("src/test/data");

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void beforeClass() throws Exception {
        final String fileToCompile = getSrcFile(SRC_DIR, "se/eris/test/TestNotNull.java");
        compile(fileToCompile);

        final NotNullConfiguration configuration = new NotNullConfiguration(true, annotations());
        final NotNullInstrumenter instrumenter = new NotNullInstrumenter(new NopLogWrapper());
        final int numberOfInstrumentedFiles = instrumenter.addNotNullAnnotations("src/test/data/se/eris/test", configuration, Collections.<URL>emptyList());

        assertThat(numberOfInstrumentedFiles, is(2));
    }

    @NotNull
    private static Set<String> annotations() {
        final Set<String> annotations = new HashSet<>();
        annotations.add("org.jetbrains.annotations.Nullable");
        annotations.add("java.lang.Deprecated");
        return annotations;
    }

    @Test
    public void notNullAnnotatedParameter_shouldValidate() throws Exception {
        final Class<?> c = getCompiledClass(TARGET_DIR, "se.eris.test.TestNotNull");
        final Method notNullParameterMethod = c.getMethod("notNullParameter", String.class);
        ReflectionUtil.simulateMethodCall(notNullParameterMethod, "should work");
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Argument 0 for implicit 'NotNull' parameter of se/eris/test/TestNotNull.notNullParameter must not be null");
        ReflectionUtil.simulateMethodCall(notNullParameterMethod, new Object[]{null});
    }

    @Test
    public void implicitParameter_shouldValidate() throws Exception {
        final Class<?> c = getCompiledClass(TARGET_DIR, "se.eris.test.TestNotNull");
        final Method implicitParameterMethod = c.getMethod("implicitParameter", String.class);
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Argument 0 for implicit 'NotNull' parameter of se/eris/test/TestNotNull.implicitParameter must not be null");
        ReflectionUtil.simulateMethodCall(implicitParameterMethod, new Object[]{null});
    }

    @Test
    public void nullableAnnotatedParameter_shouldNotValidate() throws Exception {
        final Class<?> c = getCompiledClass(TARGET_DIR, "se.eris.test.TestNotNull");
        final Method implicitParameterMethod = c.getMethod("nullableParameter", String.class);
        ReflectionUtil.simulateMethodCall(implicitParameterMethod, new Object[]{null});
    }

    @Test
    public void implicitReturn_shouldValidate() throws Exception {
        final Class<?> c = getCompiledClass(TARGET_DIR, "se.eris.test.TestNotNull");
        final Method notNullReturnMethod = c.getMethod("implicitReturn", String.class);
        ReflectionUtil.simulateMethodCall(notNullReturnMethod, "should work");
        exception.expect(IllegalStateException.class);
        exception.expectMessage("NotNull method se/eris/test/TestNotNull.implicitReturn must not return null");
        ReflectionUtil.simulateMethodCall(notNullReturnMethod, new Object[]{null});
    }

    @Test
    public void annotatedReturn_shouldNotValidate() throws Exception {
        final Class<?> c = getCompiledClass(TARGET_DIR, "se.eris.test.TestNotNull");
        final Method notNullReturnMethod = c.getMethod("annotatedReturn", String.class);
        ReflectionUtil.simulateMethodCall(notNullReturnMethod, (String) null);
    }

    @Test
    public void innerClassWithoutConstructor_shouldWork() throws Exception {
        boolean syntheticConstructorFound = false;
        final Class<?> c = getCompiledClass(TARGET_DIR, "se.eris.test.TestNotNull$Inner");
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

    @NotNull
    private Class<?> getCompiledClass(@NotNull final File targetDir, @NotNull final String className) throws MalformedURLException, ClassNotFoundException {
        final URL[] classpath = {targetDir.toURI().toURL()};
        final URLClassLoader classLoader = new URLClassLoader(classpath);
        return classLoader.loadClass(className);
    }

    @NotNull
    private static String getSrcFile(@NotNull final File srcDir, @NotNull final String file) {
        return new File(srcDir, file).toString().replace("/", File.separator);
    }

    private static void compile(@NotNull final String... filesToCompile) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final int compilationResult = compiler.run(null, null, null, filesToCompile);
        assertThat(compilationResult, is(0));
    }

}