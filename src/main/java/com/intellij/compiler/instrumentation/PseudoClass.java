/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
import se.eris.asm.ClassInfo;

import java.io.IOException;

/**
 * @author Eugene Zhuravlev
 * @author Olle Sundblad
 */
final class PseudoClass {
    private static final PseudoClass[] EMPTY_PSEUDOCLASS_ARRAY = new PseudoClass[0];

    private final InstrumentationClassFinder instrumentationClassFinder;
    private final ClassInfo classInfo;

    PseudoClass(@NotNull final InstrumentationClassFinder instrumentationClassFinder, @NotNull final ClassInfo classInfo) {
        this.instrumentationClassFinder = instrumentationClassFinder;
        this.classInfo = classInfo;
    }

    boolean isInterface() {
        return classInfo.isInterface();
    }

    String getName() {
        return classInfo.getName();
    }

    PseudoClass getSuperClass() throws IOException, ClassNotFoundException {
        final String superClass = classInfo.getSuperName();
        return superClass != null ? instrumentationClassFinder.loadClass(superClass) : null;
    }

    private PseudoClass[] getInterfaces() throws IOException, ClassNotFoundException {
        if (classInfo.getInterfaces() == null) {
            return EMPTY_PSEUDOCLASS_ARRAY;
        }

        final PseudoClass[] result = new PseudoClass[classInfo.getInterfaces().length];

        for (int i = 0; i < result.length; i++) {
            result[i] = instrumentationClassFinder.loadClass(classInfo.getInterfaces()[i]);
        }

        return result;
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        //noinspection SimplifiableIfStatement
        if (o == null || getClass() != o.getClass()) return false;

        return getName().equals(((PseudoClass) o).getName());
    }

    private boolean isSubclassOf(final PseudoClass x) throws IOException, ClassNotFoundException {
        for (PseudoClass c = this; c != null; c = c.getSuperClass()) {
            final PseudoClass superClass = c.getSuperClass();

            if (superClass != null && superClass.equals(x)) {
                return true;
            }
        }

        return false;
    }

    private boolean implementsInterface(final PseudoClass x) throws IOException, ClassNotFoundException {
        for (PseudoClass c = this; c != null; c = c.getSuperClass()) {
            final PseudoClass[] tis = c.getInterfaces();
            for (final PseudoClass ti : tis) {
                if (ti.equals(x) || ti.implementsInterface(x)) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean isAssignableFrom(final PseudoClass x) throws IOException, ClassNotFoundException {
        if (this.equals(x)) {
            return true;
        }
        if (x.isSubclassOf(this)) {
            return true;
        }
        if (x.implementsInterface(this)) {
            return true;
        }
        //noinspection RedundantIfStatement
        if (x.isInterface() && "java/lang/Object".equals(getName())) {
            return true;
        }
        return false;
    }

}
