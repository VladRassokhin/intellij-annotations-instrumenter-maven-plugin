package se.eris.notnull.instrumentation;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PackageMatcherTest {

    @Test
    public void fromPlainPackage() throws Exception {
        final PackageMatcher matcher = PackageMatcher.fromPackage("se.eris");
        assertThat(matcher.matches("se.eris"), is(true));
        assertThat(matcher.matches("se"), is(false));
        assertThat(matcher.matches("se.eris.test"), is(false));
    }

    @Test
    public void fromSingleWildcardPackage() throws Exception {
        final PackageMatcher matcher = PackageMatcher.fromPackage("se.*");
        assertThat(matcher.matches("se.eris"), is(true));
        assertThat(matcher.matches("se"), is(false));
        assertThat(matcher.matches("se.eris.test"), is(false));
    }

    @Test
    public void fromDoubleWildcardPackage() throws Exception {
        final PackageMatcher matcher = PackageMatcher.fromPackage("se.**"); // should this include the se package
        assertThat(matcher.matches("se.eris"), is(true));
        assertThat(matcher.matches("se.eris.test"), is(true));
        assertThat(matcher.matches("se"), is(false));
    }

}