package se.eris.notnull.instrumentation;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PackageMatcherTest {

    @Test
    public void fromPlainPackage() throws Exception {
        final PackageMatcher matcher = PackageMatcher.fromPackage("se.eris");
        assertThat(matcher.matches("se"), is(false));
        assertThat(matcher.matches("se.eris"), is(true));
        assertThat(matcher.matches("se.eris.test"), is(false));
    }

    @Test
    public void fromSingleWildcardPackage() throws Exception {
        final PackageMatcher matcher = PackageMatcher.fromPackage("se.*");
        assertThat(matcher.matches("se"), is(false));
        assertThat(matcher.matches("se.eris"), is(true));
        assertThat(matcher.matches("se.eris.test"), is(false));
        assertThat(matcher.matches("sea"), is(false));
    }

    @Test
    public void fromDoubleWildcardAtEndPackage() throws Exception {
        final PackageMatcher matcher = PackageMatcher.fromPackage("se.**"); // should this include the se package
        assertThat(matcher.matches("se"), is(true));
        assertThat(matcher.matches("se.eris"), is(true));
        assertThat(matcher.matches("se.eris.test"), is(true));
        assertThat(matcher.matches("sea"), is(false));
        assertThat(matcher.matches("sea.test"), is(false));
    }

    @Test
    public void fromDoubleWildcardPackage() {
        final PackageMatcher matcher = PackageMatcher.fromPackage("se.**.test"); // should this include the se package
        assertThat(matcher.matches("se"), is(false));
        assertThat(matcher.matches("se.eris"), is(false));
        assertThat(matcher.matches("se.test"), is(true));
        assertThat(matcher.matches("se.eris.test"), is(true));
        assertThat(matcher.matches("se.eris.other.test"), is(true));
        assertThat(matcher.matches("se.eris.test.more"), is(false));
        assertThat(matcher.matches("sea.eris.test"), is(false));
    }

    @Test
    public void fromWildcardFirstPackage() throws Exception {
        final PackageMatcher matcher = PackageMatcher.fromPackage("*.eris");
        assertThat(matcher.matches("se"), is(false));
        assertThat(matcher.matches("se.eris"), is(true));
        assertThat(matcher.matches("se.eris.test"), is(false));
    }

    @Test
    public void fromDoubleWildcardFirstPackage() throws Exception {
        final PackageMatcher matcher = PackageMatcher.fromPackage("**.test");
        assertThat(matcher.matches("se"), is(false));
        assertThat(matcher.matches("se.eris"), is(false));
        assertThat(matcher.matches("se.eris.test"), is(true));
    }

}