package se.eris.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@ParameterizedTest(name = "Java version: {arguments}")
@ValueSource(strings = { "1.7", "1.8", "1.9", "10", "11" })
public @interface TestSupportedJavaVersions {

    String[] SUPPORTED_VERSIONS = {"1.7", "1.8", "1.9", "10", "11"};;

}
