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
import org.jetbrains.annotations.Nullable;
import se.eris.asm.ClassInfo;

import java.io.IOException;

/**
 * @author Eugene Zhuravlev
 * @author Olle Sundblad
 */
final class PseudoClass {

    static final String JAVA_LANG_OBJECT = "java/lang/Object";
    private static final PseudoClass[] EMPTY_PSEUDOCLASS_ARRAY = new PseudoClass[0];

    @NotNull
    private final InstrumentationClassFinder instrumentationClassFinder;
    @NotNull
    private final ClassInfo classInfo;

    PseudoClass(@NotNull final InstrumentationClassFinder instrumentationClassFinder, @NotNull final ClassInfo classInfo) {
        this.instrumentationClassFinder = instrumentationClassFinder;
        this.classInfo = classInfo;
    }

    boolean isInterface() {
        return classInfo.isInterface();
    }

    @NotNull
    String getName() {
        return classInfo.getName();
    }

    @Nullable
    PseudoClass getSuperClass() throws IOException, ClassNotFoundException {
        final String superClass = classInfo.getSuperName();
        return (superClass == null) ? null : instrumentationClassFinder.loadClass(superClass);
    }

    private PseudoClass[] getImplementedInterfaces() throws IOException, ClassNotFoundException {
        final String[] interfaces = classInfo.getInterfaces();
        if (interfaces.length == 0) {
            return EMPTY_PSEUDOCLASS_ARRAY;
        }

        final PseudoClass[] result = new PseudoClass[interfaces.length];

        for (int i = 0; i < result.length; i++) {
            result[i] = instrumentationClassFinder.loadClass(interfaces[i]);
        }

        return result;
    }

    private boolean isSubclassOf(@NotNull final PseudoClass pseudoClass) throws IOException, ClassNotFoundException {
        for (PseudoClass c = this; c != null; c = c.getSuperClass()) {
            final PseudoClass superClass = c.getSuperClass();

            if ((superClass != null) && superClass.equals(pseudoClass)) {
                return true;
            }
        }
        return false;
    }

    private boolean isImplementedBy(final PseudoClass pseudoClass) throws IOException, ClassNotFoundException {
        for (PseudoClass thisClassChain = this; thisClassChain != null; thisClassChain = thisClassChain.getSuperClass()) {
            for (final PseudoClass implementedInterface : thisClassChain.getImplementedInterfaces()) {
                if (implementedInterface.equals(pseudoClass) || implementedInterface.isImplementedBy(pseudoClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean isAssignableFrom(final PseudoClass pseudoClass) throws IOException, ClassNotFoundException {
        if (this.equals(pseudoClass)) {
            return true;
        }
        if (pseudoClass.isSubclassOf(this)) {
            return true;
        }
        if (pseudoClass.isImplementedBy(this)) {
            return true;
        }
        return this.isObject() && pseudoClass.isInterface();
    }

    private boolean isObject() {
        return JAVA_LANG_OBJECT.equals(getName());
    }

    @SuppressWarnings("ControlFlowStatementWithoutBraces")
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;

        final PseudoClass that = (PseudoClass) o;

        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return "PseudoClass{classInfo=" + classInfo + '}';
    }
}
