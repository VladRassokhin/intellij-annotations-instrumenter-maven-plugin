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

import java.util.Arrays;
import java.util.Objects;

/**
* Collects class info. That is name, modifiers, super class, and interfaces.
*/
public class ClassInfo {

    private static final String[] EMPTY_ARRAY = new String[0];

    private final int version;
    private final int access;
    @NotNull
    private final String name;
    @Nullable
    private final String signature;
    @Nullable
    private final String superName;
    @NotNull
    private final String[] interfaces;

    public ClassInfo(final int version, final int access, @NotNull final String name, @Nullable final String signature, @Nullable final String superName, @Nullable final String[] interfaces) {
        this.version = version;
        this.access = access;
        this.name = name;
        this.signature = signature;
        this.superName = superName;
        this.interfaces = (interfaces == null) ? EMPTY_ARRAY : interfaces;
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

    @Nullable
    public String getSignature() {
        return signature;
    }

    @Nullable
    public String getSuperName() {
        return superName;
    }

    @NotNull
    public String[] getInterfaces() {
        return interfaces;
    }

    public boolean isInterface() {
        return (access & Opcodes.ACC_INTERFACE) > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassInfo classInfo = (ClassInfo) o;

        if (version != classInfo.version) return false;
        if (access != classInfo.access) return false;
        if (!name.equals(classInfo.name)) return false;
        if (!Objects.equals(signature, classInfo.signature)) return false;
        if (superName != null ? !superName.equals(classInfo.superName) : classInfo.superName != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(interfaces, classInfo.interfaces);

    }

    @Override
    public int hashCode() {
        int result = version;
        result = 31 * result + access;
        result = 31 * result + name.hashCode();
        result = 31 * result + (signature == null ? 0 : signature.hashCode());
        result = 31 * result + (superName != null ? superName.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(interfaces);
        return result;
    }

    @Override
    public String toString() {
        return "ClassInfo{" +
                "version=" + version +
                ", access=" + access +
                ", name='" + name + '\'' +
                ", signature='" + signature + '\'' +
                ", superName='" + superName + '\'' +
                ", interfaces=" + Arrays.toString(interfaces) +
                '}';
    }

}
