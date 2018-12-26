package se.eris.util;

import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TestClass {

    private final String fullClassName;

    public TestClass(final String fullClassName) {
        this.fullClassName = fullClassName;
    }

    public String getSimpleName() {
        return fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
    }

    public String getName() {
        return fullClassName;
    }

    public String getAsmName() {
        return fullClassName.replace(".", "/");
    }

    public File getJavaFile(final File path) {
        return new File(path, fullClassName.replace(".", "/") + ".java");
    }

    private File getClassFile(final File path) {
        return new File(path, fullClassName.replace(".", "/") + ".class");
    }

    public ClassReader getClassReader(final File path) throws IOException {
        return new ClassReader(new FileInputStream(getClassFile(path)));
    }

    public TestClass nested(final String nestedName) {
        return new TestClass(fullClassName + "$" + nestedName);
    }
}
