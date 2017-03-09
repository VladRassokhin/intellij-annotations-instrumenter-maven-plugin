package se.eris.notnull.instrumentation;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ClassMatcherTest {

    @Test
    public void fromPlainMarcher() {
        final ClassMatcher matcher = ClassMatcher.namePattern("se.eris.A");
        assertThat(matcher.matches("se.A"), is(false));
        assertThat(matcher.matches("se.eris.A"), is(true));
        assertThat(matcher.matches("se.eris.B"), is(false));
        assertThat(matcher.matches("se.eris.test.A"), is(false));
    }

    @Test
    public void fromSingleWildcard() {
        final ClassMatcher matcher = ClassMatcher.namePattern("se.*");
        assertThat(matcher.matches("se.A"), is(true));
        assertThat(matcher.matches("se.eris.A"), is(false));
        assertThat(matcher.matches("se.eris.Test"), is(false));
        assertThat(matcher.matches("sea"), is(false));
    }

    @Test
    public void fromDoubleWildcardAtEnd() {
        final ClassMatcher matcher = ClassMatcher.namePattern("se.**");
        assertThat(matcher.matches("se"), is(true));
        assertThat(matcher.matches("se.eris"), is(true));
        assertThat(matcher.matches("se.eris.Test"), is(true));
        assertThat(matcher.matches("sea"), is(false));
        assertThat(matcher.matches("sea.Test"), is(false));
    }

    @Test
    public void fromDoubleWildcard() {
        final ClassMatcher matcher = ClassMatcher.namePattern("se.**.Test");
        assertThat(matcher.matches("se"), is(false));
        assertThat(matcher.matches("se.eris"), is(false));
        assertThat(matcher.matches("se.Test"), is(true));
        assertThat(matcher.matches("se.eris.Test"), is(true));
        assertThat(matcher.matches("se.eris.other.Test"), is(true));
        assertThat(matcher.matches("se.eris.Test.more"), is(false));
        assertThat(matcher.matches("sea.eris.Test"), is(false));
    }

    @Test
    public void fromWildcardFirst() {
        final ClassMatcher matcher = ClassMatcher.namePattern("*.Test");
        assertThat(matcher.matches("se"), is(false));
        assertThat(matcher.matches("se.Test"), is(true));
        assertThat(matcher.matches("se.eris.Test"), is(false));
    }

    @Test
    public void fromDoubleWildcardFirst() {
        final ClassMatcher matcher = ClassMatcher.namePattern("**.Test");
        assertThat(matcher.matches("se"), is(false));
        assertThat(matcher.matches("se.Test"), is(true));
        assertThat(matcher.matches("se.eris.Test"), is(true));
    }

    @Test
    public void currencyDollar_shouldWork() {
        final ClassMatcher matcher = ClassMatcher.namePattern("**.Test$1");
        assertThat(matcher.matches("se.Test"), is(false));
        assertThat(matcher.matches("se.Test$1"), is(true));
        assertThat(matcher.matches("se.Test$12"), is(false));
    }

    @Test
    public void regexp_shouldWork() {
        final ClassMatcher matcher = ClassMatcher.namePattern("**.Test($[0-9]+)?");
        assertThat(matcher.matches("se.Test"), is(true));
        assertThat(matcher.matches("se.Test$1"), is(true));
        assertThat(matcher.matches("se.Test$12"), is(true));
    }

}