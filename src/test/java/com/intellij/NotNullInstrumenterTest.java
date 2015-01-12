package com.intellij;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.eris.maven.NopLogWrapper;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class NotNullInstrumenterTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void addNotNullAnnotations() throws Exception {
        final File classesDir = new File("src/test/data");
        final String fileToCompile = new File(classesDir, "se/eris/test/TestNotNull.java".replace("/", java.io.File.separator)).toString();
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final int compilationResult = compiler.run(null, null, null, fileToCompile);

        assertThat(compilationResult, is(0));

        final NotNullInstrumenter instrumenter = new NotNullInstrumenter(new NopLogWrapper());
        final int numberOfInstrumentedFiles = instrumenter.addNotNullAnnotations("src/test/data/se/eris/test", Collections.singleton("org.jetbrains.annotations.NotNull"), Collections.<URL>emptyList());

        assertThat(numberOfInstrumentedFiles, is(1));

        final URL[] classpath = {classesDir.toURI().toURL()};
        final URLClassLoader classLoader = new URLClassLoader(classpath);
        final Object o = classLoader.loadClass("se.eris.test.TestNotNull").newInstance();
        final Method notNullParameterMethod = o.getClass().getMethod("notNullParameter", String.class);

        notNullParameterMethod.invoke(null, "should work");
        exception.expect(IllegalArgumentException.class);
        //noinspection NullArgumentToVariableArgMethod
        notNullParameterMethod.invoke(null, null);
    }

}