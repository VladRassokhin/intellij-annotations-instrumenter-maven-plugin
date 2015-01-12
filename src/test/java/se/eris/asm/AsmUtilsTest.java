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

package se.eris.asm;

import org.junit.Test;
import org.objectweb.asm.Opcodes;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class AsmUtilsTest {

    @Test
    public void asmOpcodeToJavaVersion() {
        assertThat(AsmUtils.asmOpcodeToJavaVersion(Opcodes.V1_1), is(1));
        assertThat(AsmUtils.asmOpcodeToJavaVersion(Opcodes.V1_5), is(5));
        assertThat(AsmUtils.asmOpcodeToJavaVersion(Opcodes.V1_6), is(6));
        assertThat(AsmUtils.asmOpcodeToJavaVersion(Opcodes.V1_7), is(7));
    }

}