package se.eris.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestCompilerOptionsTest {

    private static final Path PATH = new File("a").toPath();

    @Test
    void hasParametersSupport() {
        assertFalse(TestCompilerOptions.from(PATH, "1.2").hasParametersSupport());
        assertFalse(TestCompilerOptions.from(PATH, "1.7").hasParametersSupport());
        assertTrue(TestCompilerOptions.from(PATH, "1.8").hasParametersSupport());
        assertTrue(TestCompilerOptions.from(PATH, "1.9").hasParametersSupport());
        assertTrue(TestCompilerOptions.from(PATH, "10").hasParametersSupport());
        assertTrue(TestCompilerOptions.from(PATH, "11").hasParametersSupport());
    }

    @Test
    void javaVersion_unsupported() {
        assertThrows(IllegalArgumentException.class, () -> TestCompilerOptions.from(PATH, "1.1").hasParametersSupport());
        assertThrows(IllegalArgumentException.class, () -> TestCompilerOptions.from(PATH, "12").hasParametersSupport());
    }

}