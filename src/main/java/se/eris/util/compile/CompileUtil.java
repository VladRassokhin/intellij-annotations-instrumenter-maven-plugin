package se.eris.util.compile;

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
import java.util.Arrays;

public class CompileUtil {

    public static boolean compile(final Path targetDir, @NotNull final File...filesToCompile) {
            createTargetDirectory(targetDir);
            final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            final Iterable<? extends JavaFileObject> javaFileObjects = getJavaFileObjects(compiler, filesToCompile);
            final JavaCompiler.CompilationTask task = compiler.getTask(null, null, null, Arrays.asList(getOutputPathParameter(targetDir)), null, javaFileObjects);
            return task.call();
        }

        @NotNull
        private static String[] getOutputPathParameter ( final Path targetDir){
            return new String[]{"-d", targetDir.toString()};
        }

        @NotNull
        private static Path createTargetDirectory ( final Path targetDir){
            try {
                return Files.createDirectories(targetDir);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        @NotNull
        private static Iterable<? extends JavaFileObject> getJavaFileObjects ( final JavaCompiler compiler,
        @NotNull final File...filesToCompile){
            final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
            return fileManager.getJavaFileObjects(filesToCompile);
        }

        @NotNull
        public static Class<?> getCompiledClass(final Path classesDirectory, @NotNull final String className) throws
        MalformedURLException, ClassNotFoundException {
            final URL[] classpath = {classesDirectory.toUri().toURL()};
            final URLClassLoader classLoader = new URLClassLoader(classpath);
            return classLoader.loadClass(className);
        }

    }
