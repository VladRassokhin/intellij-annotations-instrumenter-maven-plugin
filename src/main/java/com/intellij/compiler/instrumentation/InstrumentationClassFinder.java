/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.compiler.instrumentation;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import se.eris.asm.ClassInfoVisitor;
import se.eris.notnull.instrumentation.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Eugene Zhuravlev
 *         Date: 2/16/12
 */
public class InstrumentationClassFinder {
    private static final String CLASS_RESOURCE_EXTENSION = ".class";

    @NotNull
    private final Map<String, PseudoClass> myLoaded = new HashMap<>(); // className -> class object
    @NotNull
    private final ClassFinderClasspath classpath;

    public InstrumentationClassFinder(final URL[] cp) {
        classpath = new ClassFinderClasspath(cp);
    }

    @NotNull
    PseudoClass loadClass(final String name) throws IOException, ClassNotFoundException {
        final String internalName = name.replace('.', '/'); // normalize
        final PseudoClass aClass = myLoaded.get(internalName);
        if (aClass != null) {
            return aClass;
        }

        final String resourceName = internalName + CLASS_RESOURCE_EXTENSION;
        // look into classpath
        final Resource resource = classpath.getResource(resourceName);
        final InputStream is;
        if (resource == null) {
            is = ClassLoader.getSystemResourceAsStream(resourceName);
        } else {
            is = resource.getInputStream();
        }
        if (is == null) {
            throw new ClassNotFoundException("Class not found: " + internalName);
        }

        try {
            final PseudoClass result = loadPseudoClass(is);
            myLoaded.put(internalName, result);
            return result;
        } finally {
            is.close();
        }
    }

    @NotNull
    private PseudoClass loadPseudoClass(final InputStream is) throws IOException {
        final ClassReader reader = new ClassReader(is);
        final ClassInfoVisitor visitor = new ClassInfoVisitor();

        reader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        return new PseudoClass(this, visitor.getClassInfo());
    }

}
