package se.eris.notnull.instrumentation;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ClassMatcherTest {

    @Test
    public void fromPlainPackage() {
        final ClassMatcher matcher = ClassMatcher.namePattern("se.eris.A");
        assertThat(matcher.matches("se.A"), is(false));
        assertThat(matcher.matches("se.eris.A"), is(true));
        assertThat(matcher.matches("se.eris.B"), is(false));
        assertThat(matcher.matches("se.eris.test.A"), is(false));
    }

    @Test
    public void fromSingleWildcardPackage() {
        final ClassMatcher matcher = ClassMatcher.namePattern("se.*");
        assertThat(matcher.matches("se.A"), is(true));
        assertThat(matcher.matches("se.eris.A"), is(false));
        assertThat(matcher.matches("se.eris.Test"), is(false));
        assertThat(matcher.matches("sea"), is(false));
    }

    @Test
    public void fromDoubleWildcardAtEndPackage() {
        final ClassMatcher matcher = ClassMatcher.namePattern("se.**"); // should this include the se package
        assertThat(matcher.matches("se"), is(true));
        assertThat(matcher.matches("se.eris"), is(true));
        assertThat(matcher.matches("se.eris.Test"), is(true));
        assertThat(matcher.matches("sea"), is(false));
        assertThat(matcher.matches("sea.Test"), is(false));
    }

    @Test
    public void fromDoubleWildcardPackage() {
        final ClassMatcher matcher = ClassMatcher.namePattern("se.**.Test"); // should this include the se package
        assertThat(matcher.matches("se"), is(false));
        assertThat(matcher.matches("se.eris"), is(false));
        assertThat(matcher.matches("se.Test"), is(true));
        assertThat(matcher.matches("se.eris.Test"), is(true));
        assertThat(matcher.matches("se.eris.other.Test"), is(true));
        assertThat(matcher.matches("se.eris.Test.more"), is(false));
        assertThat(matcher.matches("sea.eris.Test"), is(false));
    }

    @Test
    public void fromWildcardFirstPackage() {
        final ClassMatcher matcher = ClassMatcher.namePattern("*.Test");
        assertThat(matcher.matches("se"), is(false));
        assertThat(matcher.matches("se.Test"), is(true));
        assertThat(matcher.matches("se.eris.Test"), is(false));
    }

    @Test
    public void fromDoubleWildcardFirstPackage() {
        final ClassMatcher matcher = ClassMatcher.namePattern("**.Test");
        assertThat(matcher.matches("se"), is(false));
        assertThat(matcher.matches("se.Test"), is(true));
        assertThat(matcher.matches("se.eris.Test"), is(true));
    }

}