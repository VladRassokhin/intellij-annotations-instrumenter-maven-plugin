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

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AsmUtilsTest {

    @Test
    void asmOpcodeToJavaVersion() {
        assertEquals(1, AsmUtils.asmOpcodeToJavaVersion(Opcodes.V1_1));
        assertEquals(5, AsmUtils.asmOpcodeToJavaVersion(Opcodes.V1_5));
        assertEquals(6, AsmUtils.asmOpcodeToJavaVersion(Opcodes.V1_6));
        assertEquals(7, AsmUtils.asmOpcodeToJavaVersion(Opcodes.V1_7));
        assertEquals(8, AsmUtils.asmOpcodeToJavaVersion(Opcodes.V1_8));

        assertEquals(9, AsmUtils.asmOpcodeToJavaVersion(Opcodes.V9));
        assertEquals(10, AsmUtils.asmOpcodeToJavaVersion(Opcodes.V10));
        assertEquals(11, AsmUtils.asmOpcodeToJavaVersion(Opcodes.V11));
    }

}