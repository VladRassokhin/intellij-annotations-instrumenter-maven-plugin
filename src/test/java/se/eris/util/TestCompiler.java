package se.eris.util;

import org.jetbrains.annotations.NotNull;
import se.eris.util.compiler.JavaSystemCompilerUtil;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TestCompiler {

    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private final TestCompilerOptions options;
    private final URLClassLoader classLoader;

    public static TestCompiler create(final TestCompilerOptions options) {
        return new TestCompiler(options);
    }
    public static TestCompiler create(final Path destinationDirectory) {
        return create(TestCompilerOptions.from(destinationDirectory, "1.8"));
    }

    private TestCompiler(final TestCompilerOptions options) {
        createDestinationDirectory(options);
        this.options = options;
        classLoader = new URLClassLoader(options.getClasspathURLs());
    }

    private void createDestinationDirectory(final TestCompilerOptions options) {
        try {
            Files.createDirectories(options.getDestination());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean compile(@NotNull final File... filesToCompile) {
        final Iterable<? extends JavaFileObject> javaFileObjects = getJavaFileObjects(compiler, filesToCompile);
        final JavaCompiler.CompilationTask task = compiler.getTask(null, null, null, buildCompilerOptions(), null, javaFileObjects);
        return task.call();
    }

    /**
     * Builds options to be passed to the compiler.
     * See http://docs.oracle.com/javase/8/docs/technotes/tools/windows/javac.html#options
     */
    @NotNull
    private List<String> buildCompilerOptions() {
        final List<String> options = new ArrayList<>(this.options.getOptions());
        if (JavaSystemCompilerUtil.supportParametersOption()) {
            options.add("-parameters");
        }
        return options;
    }

    @NotNull
    private static Iterable<? extends JavaFileObject> getJavaFileObjects(final JavaCompiler compiler,
                                                                         @NotNull final File... filesToCompile) {
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        return fileManager.getJavaFileObjects(filesToCompile);
    }

    @NotNull
    public Class<?> getCompiledClass(@NotNull final TestClass testClass) throws ClassNotFoundException {
        return getCompiledClass(testClass.getName());
    }

    @NotNull
    public Class<?> getCompiledClass(@NotNull final String className) throws ClassNotFoundException {
        try {
            return classLoader.loadClass(className);
        } catch (final ClassNotFoundException e) {
            System.out.println("className = " + className);
            throw e;
        }
    }

    public boolean hasParametersSupport() {
        return options.hasParametersSupport();
    }
}
