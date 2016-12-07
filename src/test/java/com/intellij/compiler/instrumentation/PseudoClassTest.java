package com.intellij.compiler.instrumentation;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PseudoClassTest {

    @Test
    public void isAssignableFrom_isSubClass() throws IOException, ClassNotFoundException {
        final InstrumentationClassFinder instrumentationClassFinder = getInstrumentationClassFinder();

        final PseudoClass object = instrumentationClassFinder.loadClass("java.lang.Object");
        final PseudoClass arrayList = instrumentationClassFinder.loadClass("java.util.ArrayList");

        assertThat(object.isAssignableFrom(arrayList), is(true));
        assertThat(arrayList.isAssignableFrom(object), is(false));
    }

    @Test
    public void isAssignableFrom_implementsInterface() throws IOException, ClassNotFoundException {
        final InstrumentationClassFinder instrumentationClassFinder = getInstrumentationClassFinder();

        final PseudoClass iterable = instrumentationClassFinder.loadClass("java.lang.Iterable");
        final PseudoClass arrayList = instrumentationClassFinder.loadClass("java.util.ArrayList");

        assertThat(iterable.isAssignableFrom(arrayList), is(true));
        assertThat(arrayList.isAssignableFrom(iterable), is(false));
    }

    @Test
    public void isAssignableFrom_isInterface() throws IOException, ClassNotFoundException {
        final InstrumentationClassFinder instrumentationClassFinder = getInstrumentationClassFinder();

        final PseudoClass iterable = instrumentationClassFinder.loadClass("java.lang.Iterable");
        final PseudoClass object = instrumentationClassFinder.loadClass("java.lang.Object");

        assertThat(object.isAssignableFrom(iterable), is(true));
        assertThat(iterable.isAssignableFrom(object), is(false));
    }

    @NotNull
    private InstrumentationClassFinder getInstrumentationClassFinder() {
        final ClassLoader cl = ClassLoader.getSystemClassLoader();
        final URL[] urls = ((URLClassLoader) cl).getURLs();
        return new InstrumentationClassFinder(urls);
    }

}