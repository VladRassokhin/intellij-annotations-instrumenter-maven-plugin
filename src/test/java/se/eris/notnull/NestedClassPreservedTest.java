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
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import se.eris.asm.AsmUtils;
import se.eris.maven.NopLogWrapper;
import se.eris.notnull.instrumentation.ClassMatcher;
import se.eris.util.ReflectionUtil;
import se.eris.util.TestClass;
import se.eris.util.TestCompiler;
import se.eris.util.TestCompilerOptions;
import se.eris.util.compiler.JavaSystemCompilerUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertTrue;

public class NestedClassPreservedTest {

    private static final File SRC_DIR = new File("src/test/data");
    private static final File DESTINATION_DIR = new File("target/test/data/classes");

    private static final TestClass TEST_CLASS = new TestClass("se.eris.test.TestNotNull");

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static TestCompiler compiler;

    @BeforeClass
    public static void beforeClass() {
        compiler = TestCompiler.create(TestCompilerOptions.from(DESTINATION_DIR.toPath(), "1.8"));
        compiler.compile(TEST_CLASS.getJavaFile(SRC_DIR));

        final Configuration configuration = new Configuration(false, new AnnotationConfiguration(), new ExcludeConfiguration(Collections.<ClassMatcher>emptySet()));
        final NotNullInstrumenter instrumenter = new NotNullInstrumenter(new NopLogWrapper());
        final int numberOfInstrumentedFiles = instrumenter.addNotNullAnnotations(DESTINATION_DIR.toPath(), configuration, Collections.<URL>emptyList());

        assertThat(numberOfInstrumentedFiles, greaterThan(0));
    }

    /**
     * @return single-quoted parameter name if compiler supports `-parameters` option, empty string otherwise.
     */
    @NotNull
    private static String maybeName(@NotNull final String parameterName) {
        return JavaSystemCompilerUtil.supportParametersOption() ? String.format(" (parameter '%s')", parameterName) : "";
    }

    @Test
    public void syntheticMethod_dispatchesToSpecializedMethod() throws Exception {
        final Class<?> superargClass = compiler.getCompiledClass(TEST_CLASS.nested("Superarg").getName());
        final TestClass sub = TEST_CLASS.nested("Sub");
        final Class<?> subClass = compiler.getCompiledClass(sub.getName());
        final Method generalMethod = subClass.getMethod("overload", superargClass);
        assertTrue(generalMethod.isSynthetic());
        assertTrue(generalMethod.isBridge());
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("NotNull annotated argument 0" + maybeName("s") + " of " + sub.getAsmName() + ".overload must not be null");
        ReflectionUtil.simulateMethodCall(subClass.newInstance(), generalMethod, new Object[]{null});
    }

    @Test
    public void onlySpecificMethod_isInstrumented() throws Exception {
        // Check that only the specific method has a string annotation indicating instrumentation
        final TestClass sub = TEST_CLASS.nested("Sub");
        final ClassReader classReader = sub.getClassReader(DESTINATION_DIR);
        final List<String> strings = getStringConstants(classReader, "overload");
        final String onlyExpectedString = "(L" + TEST_CLASS.nested("Subarg").getAsmName() + ";)V:" +
                "NotNull annotated argument 0" + maybeName("s") + " of " +
                sub.getAsmName() + ".overload must not be null";
        assertEquals(Collections.singletonList(onlyExpectedString), strings);
    }

    @Test
    public void nestedClassesSegmentIsPreserved() throws Exception {
        // Check that only the specific method has a string annotation indicating instrumentation
        final TestClass preserved = TEST_CLASS.nested("NestedClassesSegmentIsPreserved");
        final ClassReader classReader = preserved.getClassReader(DESTINATION_DIR);
        final List<InnerClass> innerClasses = getInnerClasses(classReader);
        assertEquals(2, innerClasses.size());
        //self-entry
        assertEquals( preserved.getAsmName(), innerClasses.get(0).name);
        //inner entry
        final InnerClass expected = new InnerClass(preserved.nested("ASub").getAsmName(),
                preserved.getAsmName(), "ASub", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
        assertEquals(expected, innerClasses.get(1));
    }

    private List<InnerClass> getInnerClasses(final ClassReader cr) {
        final List<InnerClass> innerClasses = new ArrayList<>();
        cr.accept(new ClassVisitor(AsmUtils.ASM_OPCODES_VERSION) {
            @Override
            public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
                innerClasses.add(new InnerClass(name, outerName, innerName, access));
            }
        }, 0);
        return innerClasses;
    }

    @NotNull
    private List<String> getStringConstants(final ClassReader cr, final String methodName) {
        final List<String> strings = new ArrayList<>();
        cr.accept(new ClassVisitor(AsmUtils.ASM_OPCODES_VERSION) {
            @Override
            public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
                                             final String[] exceptions) {
                if (name.equals(methodName)) {
                    return new MethodVisitor(AsmUtils.ASM_OPCODES_VERSION) {
                        @Override
                        public void visitLdcInsn(final Object cst) {
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

}