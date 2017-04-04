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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import se.eris.maven.NopLogWrapper;
import se.eris.notnull.instrumentation.ClassMatcher;
import se.eris.util.ReflectionUtil;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

public class AnnotationNotNullInstrumenterTest {

    private static final File SRC_DIR = new File("src/test/data");
    private static final File TARGET_DIR = new File("src/test/data");

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private static URLClassLoader classLoader;

    @BeforeClass
    public static void beforeClass() throws MalformedURLException {
        final URL[] classpath = {TARGET_DIR.toURI().toURL()};
        classLoader = new URLClassLoader(classpath);
        final String fileToCompile = getSrcFile(SRC_DIR, "se/eris/test/TestNotNull.java");
        compile(fileToCompile);

        final Configuration configuration = new Configuration(false, new AnnotationConfiguration(notNull(), Collections.<String>emptySet()), new ExcludeConfiguration(Collections.<ClassMatcher>emptySet()));
        final NotNullInstrumenter instrumenter = new NotNullInstrumenter(new NopLogWrapper());
        final int numberOfInstrumentedFiles = instrumenter.addNotNullAnnotations("src/test/data/se/eris/test", configuration, Collections.<URL>emptyList());

        assertThat(numberOfInstrumentedFiles, greaterThan(0));
    }

    @NotNull
    private static Set<String> notNull() {
        final Set<String> annotations = new HashSet<>();
        annotations.add("org.jetbrains.annotations.NotNull");
        annotations.add("java.lang.Deprecated");
        return annotations;
    }

    @Test
    public void annotatedParameter_shouldValidate() throws Exception {
        final Class<?> c = getCompiledClass("se.eris.test.TestNotNull");
        final Method notNullParameterMethod = c.getMethod("notNullParameter", String.class);
        ReflectionUtil.simulateMethodCall(notNullParameterMethod, "should work");

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Argument 0 for @NotNull parameter of se/eris/test/TestNotNull.notNullParameter must not be null");
        ReflectionUtil.simulateMethodCall(notNullParameterMethod, new Object[]{null});
    }

    @Test
    public void notnullReturn_shouldValidate() throws Exception {
        final Class<?> c = getCompiledClass("se.eris.test.TestNotNull");
        final Method notNullReturnMethod = c.getMethod("notNullReturn", String.class);
        ReflectionUtil.simulateMethodCall(notNullReturnMethod, "should work");

        exception.expect(IllegalStateException.class);
        exception.expectMessage("NotNull method se/eris/test/TestNotNull.notNullReturn must not return null");
        ReflectionUtil.simulateMethodCall(notNullReturnMethod, new Object[]{null});
    }

    @Test
    public void annotatedReturn_shouldValidate() throws Exception {
        final Class<?> c = getCompiledClass("se.eris.test.TestNotNull");
        final Method notNullReturnMethod = c.getMethod("annotatedReturn", String.class);
        ReflectionUtil.simulateMethodCall(notNullReturnMethod, "should work");

        exception.expect(IllegalStateException.class);
        exception.expectMessage("NotNull method se/eris/test/TestNotNull.annotatedReturn must not return null");
        ReflectionUtil.simulateMethodCall(notNullReturnMethod, new Object[]{null});
    }

    @Test
    public void overridingMethod_isInstrumented() throws Exception {
        final Class<?> subargClass = getCompiledClass("se.eris.test.TestNotNull$Subarg");
        final Class<?> subClass = getCompiledClass("se.eris.test.TestNotNull$Sub");
        final Method specializedMethod = subClass.getMethod("overload", subargClass);
        Assert.assertFalse(specializedMethod.isSynthetic());
        Assert.assertFalse(specializedMethod.isBridge());
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Argument 0 for @NotNull parameter of se/eris/test/TestNotNull$Sub.overload must not be null");
        ReflectionUtil.simulateMethodCall(subClass.newInstance(), specializedMethod, new Object[]{null});
    }

    @Test
    public void syntheticMethod_dispatchesToSpecializedMethod() throws Exception {
        final Class<?> superargClass = getCompiledClass("se.eris.test.TestNotNull$Superarg");
        final Class<?> subClass = getCompiledClass("se.eris.test.TestNotNull$Sub");
        final Method generalMethod = subClass.getMethod("overload", superargClass);
        Assert.assertTrue(generalMethod.isSynthetic());
        Assert.assertTrue(generalMethod.isBridge());
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Argument 0 for @NotNull parameter of se/eris/test/TestNotNull$Sub.overload must not be null");
        ReflectionUtil.simulateMethodCall(subClass.newInstance(), generalMethod, new Object[]{null});
    }

    @Test
    public void onlySpecificMethod_isInstrumented() throws Exception {
        // Check that only the specific method has a string annotation indicating instrumentation
        final File f = new File(TARGET_DIR, "se/eris/test/TestNotNull$Sub.class");
        Assert.assertTrue(f.isFile());
        final ClassReader cr = new ClassReader(new FileInputStream(f));
        final ArrayList<String> strings = getStringConstants(cr, "overload");
        final String onlyExpectedString = "(Lse/eris/test/TestNotNull$Subarg;)V:" +
                "Argument 0 for @NotNull parameter of " +
                "se/eris/test/TestNotNull$Sub.overload must not be null";
        Assert.assertEquals(Collections.singletonList(
                onlyExpectedString), strings);
    }

    @NotNull
    private ArrayList<String> getStringConstants(ClassReader cr, final String methodName) {
        final ArrayList<String> strings = new ArrayList<>();
        cr.accept(new ClassVisitor(Opcodes.ASM5) {
            @Override
            public MethodVisitor visitMethod(int access, String name, final String desc, String signature,
                                             String[] exceptions) {
                if (name.equals(methodName)) {
                    return new MethodVisitor(Opcodes.ASM5) {
                        @Override
                        public void visitLdcInsn(Object cst) {
                            if (cst instanceof String) {
                                strings.add(desc + ":" + cst);
                            }
                        }
                    };
                }
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        }, 0);
        return strings;
    }

    @NotNull
    private Class<?> getCompiledClass(@NotNull final String className) throws MalformedURLException, ClassNotFoundException {
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