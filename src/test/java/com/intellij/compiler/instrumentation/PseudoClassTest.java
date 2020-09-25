package com.intellij.compiler.instrumentation;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PseudoClassTest {

    @Test
    void isAssignableFrom_isSubClass() throws IOException, ClassNotFoundException {
        final InstrumentationClassFinder instrumentationClassFinder = getInstrumentationClassFinder();

        final PseudoClass object = instrumentationClassFinder.loadClass("java.lang.Object");
        final PseudoClass arrayList = instrumentationClassFinder.loadClass("java.util.ArrayList");

        assertTrue(object.isAssignableFrom(arrayList));
        assertFalse(arrayList.isAssignableFrom(object));
    }

    @Test
    void isAssignableFrom_implementsInterface() throws IOException, ClassNotFoundException {
        final InstrumentationClassFinder instrumentationClassFinder = getInstrumentationClassFinder();

        final PseudoClass iterable = instrumentationClassFinder.loadClass("java.lang.Iterable");
        final PseudoClass arrayList = instrumentationClassFinder.loadClass("java.util.ArrayList");

        assertTrue(iterable.isAssignableFrom(arrayList));
        assertFalse(arrayList.isAssignableFrom(iterable));
    }

    @Test
    void isAssignableFrom_isInterface() throws IOException, ClassNotFoundException {
        final InstrumentationClassFinder instrumentationClassFinder = getInstrumentationClassFinder();

        final PseudoClass iterable = instrumentationClassFinder.loadClass("java.lang.Iterable");
        final PseudoClass object = instrumentationClassFinder.loadClass("java.lang.Object");

        assertTrue(object.isAssignableFrom(iterable));
        assertFalse(iterable.isAssignableFrom(object));
    }

    @NotNull
    private InstrumentationClassFinder getInstrumentationClassFinder() throws MalformedURLException {
        final String[] paths = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
        final URL[] urls = new URL[paths.length];
        for (int i = 0; i < paths.length; i++) {
            final String path = paths[i];
            urls[i] = new File(path).toURI().toURL();
        }
        return new InstrumentationClassFinder(urls);
    }

}