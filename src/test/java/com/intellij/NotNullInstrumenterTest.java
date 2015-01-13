package com.intellij;

import org.hamcrest.*;
import org.jetbrains.annotations.NotNull;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.eris.maven.NopLogWrapper;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class NotNullInstrumenterTest {

    public static final File SRC_DIR = new File("src/test/data");
    public static final File TARGET_DIR = new File("src/test/data");

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void beforeClass() {
        final String fileToCompile = getSrcFile(SRC_DIR, "se/eris/test/TestNotNull.java");
        compile(fileToCompile);

        final NotNullInstrumenter instrumenter = new NotNullInstrumenter(new NopLogWrapper());
        final int numberOfInstrumentedFiles = instrumenter.addNotNullAnnotations("src/test/data/se/eris/test", Collections.singleton("org.jetbrains.annotations.NotNull"), Collections.<URL>emptyList());

        assertThat(numberOfInstrumentedFiles, is(1));


    }

    @Test
    public void parameterFT() throws Exception {
        final Class<?> c = getCompiledClass(TARGET_DIR, "se.eris.test.TestNotNull");
        final Method notNullParameterMethod = c.getMethod("notNullParameter", String.class);
        notNullParameterMethod.invoke(null, "should work");
        exception.expect(ExceptionCauseMatcher.causeMatcher(new IllegalArgumentException("Argument 0 for @NotNull parameter of se/eris/test/TestNotNull.notNullParameter must not be null")));
        notNullParameterMethod.invoke(null, (String) null);
    }

    @Test
    public void returnFT() throws Exception {
        final Class<?> c = getCompiledClass(TARGET_DIR, "se.eris.test.TestNotNull");
        final Method notNullReturnMethod = c.getMethod("notNullReturn", String.class);
        notNullReturnMethod.invoke(null, "should work");
        exception.expect(ExceptionCauseMatcher.causeMatcher(new IllegalStateException("@NotNull method se/eris/test/TestNotNull.notNullReturn must not return null")));
        notNullReturnMethod.invoke(null, (String) null);
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

    private static class ExceptionCauseMatcher extends TypeSafeMatcher<Exception> {

        @NotNull
        private RuntimeException expectedException;

        private ExceptionCauseMatcher(@NotNull final RuntimeException expectedException) {
            super(InvocationTargetException.class);
            this.expectedException = expectedException;
        }

        @Override
        public boolean matchesSafely(@NotNull final Exception exception) {
            final Throwable cause = exception.getCause();
            return classesMatch(cause) && messagesMatch(expectedException, cause);
        }

        private boolean messagesMatch(@NotNull final Exception exception, @NotNull final Throwable cause) {
            return cause.getMessage().equals(exception.getMessage());
        }

        private boolean classesMatch(@NotNull final Throwable cause) {
            return cause.getClass().equals(expectedException.getClass());
        }

        public void describeTo(@NotNull final Description description) {
          description.appendText(expectedException.toString());
        }

        protected void describeMismatchSafely(final Exception item, @NotNull final Description mismatchDescription) {
            super.describeMismatch(item.getCause(), mismatchDescription);
        }

        @Factory
        public static Matcher<Exception> causeMatcher(@NotNull final RuntimeException expectedException) {
          return new ExceptionCauseMatcher(expectedException);
        }
    }
}