package se.eris.util;

import org.junit.Test;

import java.io.File;
import java.util.Set;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class ClassFileUtilsTest {

    @Test
    public void collectClassFiles() {
        final Set<File> classFiles = ClassFileUtils.getClassFiles(new File("target/classes").toPath());

        assertThat(classFiles, hasSize(greaterThan(0)));
    }
}