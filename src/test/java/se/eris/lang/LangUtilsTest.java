package se.eris.lang;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LangUtilsTest {

    @Test
    public void convertToJavaClassName() {
        assertThat(LangUtils.convertToJavaClassName("org.jetbrains.annotations.NotNull"), is("Lorg/jetbrains/annotations/NotNull;"));
    }

}