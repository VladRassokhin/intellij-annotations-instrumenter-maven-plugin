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
import java.util.Arrays;
import java.util.List;

public class TestCompiler {

    public static TestCompiler create(final Path targetDir) throws MalformedURLException {
        return new TestCompiler(targetDir);
    }

    private final Path targetDir;
    private final URLClassLoader classLoader;

    private TestCompiler(final Path targetDir) throws MalformedURLException {
        createTargetDirectory(targetDir);
        this.targetDir = targetDir;
        final URL[] classpath = {targetDir.toUri().toURL()};
        classLoader = new URLClassLoader(classpath);
    }

    public boolean compile(@NotNull final File...filesToCompile) {
            final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            List<String> options = new ArrayList<>();
            options.addAll(Arrays.asList(getOutputPathParameter(targetDir)));
            // http://docs.oracle.com/javase/8/docs/technotes/tools/windows/javac.html#options
            if (compiler.isSupportedOption("-parameters") != -1) options.add("-parameters");
            final Iterable<? extends JavaFileObject> javaFileObjects = getJavaFileObjects(compiler, filesToCompile);
            final JavaCompiler.CompilationTask task = compiler.getTask(null, null, null, options, null, javaFileObjects);
            return task.call();
        }

    @NotNull
    private static String[] getOutputPathParameter(final Path targetDir) {
        return new String[]{"-d", targetDir.toString()};
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
