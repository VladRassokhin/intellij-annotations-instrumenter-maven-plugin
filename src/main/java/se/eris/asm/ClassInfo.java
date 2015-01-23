/*
 * Copyright 2013-2015 Eris IT AB
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
package se.eris.asm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;

/**
* Collects class info. That is name, modifiers, super class, and interfaces.
*/
public class ClassInfo {

    private final int version;
    private final int access;
    @NotNull
    private final String name;
    @NotNull
    private final String signature;
    @Nullable
    private final String superName;
    @Nullable
    private final String[] interfaces;

    public ClassInfo(final int version, final int access, @NotNull final String name, @NotNull final String signature, @Nullable final String superName, @Nullable final String[] interfaces) {
        this.version = version;
        this.access = access;
        this.name = name;
        this.signature = signature;
        this.superName = superName;
        this.interfaces = interfaces;
    }

    public int getVersion() {
        return version;
    }

    public int getAccess() {
        return access;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getSignature() {
        return signature;
    }

    @Nullable
    public String getSuperName() {
        return superName;
    }

    @Nullable
    public String[] getInterfaces() {
        return interfaces;
    }

    public boolean isInterface() {
        return (access & Opcodes.ACC_INTERFACE) > 0;
    }
}
