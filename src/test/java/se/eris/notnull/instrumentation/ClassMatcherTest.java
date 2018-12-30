package se.eris.notnull.instrumentation;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassMatcherTest {

    @Test
    void fromPlainMarcher() {
        final ClassMatcher matcher = ClassMatcher.namePattern("se.eris.A");
        assertFalse(matcher.matches("se.A"));
        assertTrue(matcher.matches("se.eris.A"));
        assertFalse(matcher.matches("se.eris.B"));
        assertFalse(matcher.matches("se.eris.test.A"));
    }

    @Test
    void fromSingleWildcard() {
        final ClassMatcher matcher = ClassMatcher.namePattern("se.*");
        assertTrue(matcher.matches("se.A"));
        assertFalse(matcher.matches("se.eris.A"));
        assertFalse(matcher.matches("se.eris.Test"));
        assertFalse(matcher.matches("sea"));
    }

    @Test
    void fromDoubleWildcardAtEnd() {
        final ClassMatcher matcher = ClassMatcher.namePattern("se.**");
        assertTrue(matcher.matches("se"));
        assertTrue(matcher.matches("se.eris"));
        assertTrue(matcher.matches("se.eris.Test"));
        assertFalse(matcher.matches("sea"));
        assertFalse(matcher.matches("sea.Test"));
    }

    @Test
    void fromDoubleWildcard() {
        final ClassMatcher matcher = ClassMatcher.namePattern("se.**.Test");
        assertFalse(matcher.matches("se"));
        assertFalse(matcher.matches("se.eris"));
        assertTrue(matcher.matches("se.Test"));
        assertTrue(matcher.matches("se.eris.Test"));
        assertTrue(matcher.matches("se.eris.other.Test"));
        assertFalse(matcher.matches("se.eris.Test.more"));
        assertFalse(matcher.matches("sea.eris.Test"));
    }

    @Test
    void fromWildcardFirst() {
        final ClassMatcher matcher = ClassMatcher.namePattern("*.Test");
        assertFalse(matcher.matches("se"));
        assertTrue(matcher.matches("se.Test"));
        assertFalse(matcher.matches("se.eris.Test"));
    }

    @Test
    void fromDoubleWildcardFirst() {
        final ClassMatcher matcher = ClassMatcher.namePattern("**.Test");
        assertFalse(matcher.matches("se"));
        assertTrue(matcher.matches("se.Test"));
        assertTrue(matcher.matches("se.eris.Test"));
    }

    @Test
    void currencyDollar_shouldWork() {
        final ClassMatcher matcher = ClassMatcher.namePattern("**.Test$1");
        assertFalse(matcher.matches("se.Test"));
        assertTrue(matcher.matches("se.Test$1"));
        assertFalse(matcher.matches("se.Test$12"));
    }

    @Test
    void regexp_shouldWork() {
        final ClassMatcher matcher = ClassMatcher.namePattern("**.Test($[0-9]+)?");
        assertTrue(matcher.matches("se.Test"));
        assertTrue(matcher.matches("se.Test$1"));
        assertTrue(matcher.matches("se.Test$12"));
    }

}