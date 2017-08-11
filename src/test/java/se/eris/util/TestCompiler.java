package se.eris.util;

import org.jetbrains.annotations.NotNull;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TestCompiler {

    public static TestCompiler create(final Path targetDir) throws MalformedURLException {
        return new TestCompiler(targetDir);
    }

    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private final boolean parametersOptionSupported = compiler.isSupportedOption("-parameters") != -1;
    private final Iterable<String> options;
    private final URLClassLoader classLoader;

    private TestCompiler(final Path targetDir) throws MalformedURLException {
        createTargetDirectory(targetDir);
        options = buildCompilerOptions(targetDir, parametersOptionSupported);
        final URL[] classpath = {targetDir.toUri().toURL()};
        classLoader = new URLClassLoader(classpath);
    }

    public boolean compile(@NotNull final File...filesToCompile) {
        final Iterable<? extends JavaFileObject> javaFileObjects = getJavaFileObjects(compiler, filesToCompile);
        final JavaCompiler.CompilationTask task = compiler.getTask(null, null, null, options, null, javaFileObjects);
        return task.call();
    }

    /**
     * Builds options to be passed to the compiler.
     * See http://docs.oracle.com/javase/8/docs/technotes/tools/windows/javac.html#options
     */
    @NotNull
    private static Iterable<String> buildCompilerOptions(final Path targetDir, final boolean parametersOptionSupported) {
        List<String> options = new ArrayList<>();
        options.add("-d");
        options.add(targetDir.toString());
        if (parametersOptionSupported) options.add("-parameters");
        return options;
    }

    @NotNull
    private static Path createTargetDirectory(final Path targetDir) {
        try {
            return Files.createDirectories(targetDir);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static Iterable<? extends JavaFileObject> getJavaFileObjects(final JavaCompiler compiler,
                                                                         @NotNull final File... filesToCompile) {
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        return fileManager.getJavaFileObjects(filesToCompile);
    }

    /** Whether the compiler supports `-parameters` option. */
    public boolean parametersOptionSupported() {
        return parametersOptionSupported;
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

}
