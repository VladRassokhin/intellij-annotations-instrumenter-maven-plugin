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
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.eris.maven.NopLogWrapper;
import se.eris.notnull.instrumentation.ClassMatcher;
import se.eris.util.ReflectionUtil;
import se.eris.util.TestCompiler;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

public class ImplicitInnerClassNullableInstrumenterTest {

    private static final String CLASS_NAME = "TestClassImplicit$1";

    private static final String TEST_CLASS = "se.eris.implicit." + CLASS_NAME;
    private static final String TEST_FILE = "se/eris/implicit/" + CLASS_NAME + ".java";

    private static final File SRC_DIR = new File("src/test/data/");
    private static final Path CLASSES_DIRECTORY = new File("target/test/data/classes").toPath();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static TestCompiler compiler;

    @BeforeClass
    public static void beforeClass() throws MalformedURLException {
        final File fileToCompile = new File(SRC_DIR, TEST_FILE);
        compiler = TestCompiler.create(CLASSES_DIRECTORY);
        compiler.compile(fileToCompile);

        final Configuration configuration = new Configuration(true,
                new AnnotationConfiguration(),
                new ExcludeConfiguration(Collections.<ClassMatcher>emptySet()));
        final NotNullInstrumenter instrumenter = new NotNullInstrumenter(new NopLogWrapper());
        final int numberOfInstrumentedFiles = instrumenter.addNotNullAnnotations(CLASSES_DIRECTORY, configuration, Collections.<URL>emptyList());

        assertThat(numberOfInstrumentedFiles, greaterThan(0));
    }

    @Test
    public void anonymousClassConstructor_shouldFollowParentAnnotation() throws Exception {
        final Class<?> c = compiler.getCompiledClass(TEST_CLASS);
        final Method anonymousClassNullable = c.getMethod("anonymousClassNullable");
        ReflectionUtil.simulateMethodCall(anonymousClassNullable);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Argument 0 for implicit 'NotNull' parameter of se/eris/implicit/" + CLASS_NAME + "$Foo.<init> must not be null");
        final Method anonymousClassNotNull = c.getMethod("anonymousClassNotNull");
        ReflectionUtil.simulateMethodCall(anonymousClassNotNull);
    }

}