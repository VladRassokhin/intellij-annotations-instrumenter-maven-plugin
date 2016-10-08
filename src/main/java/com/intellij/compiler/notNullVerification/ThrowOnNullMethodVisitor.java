/*
 * Copyright 2013-2016 Eris IT AB
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
package com.intellij.compiler.notNullVerification;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import se.eris.lang.LangUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class ThrowOnNullMethodVisitor extends MethodVisitor {

    static final String LJAVA_LANG_SYNTHETIC_ANNO = "Ljava/lang/Synthetic;";
    private static final String IAE_CLASS_NAME = "java/lang/IllegalArgumentException";
    private static final String ISE_CLASS_NAME = "java/lang/IllegalStateException";
    private static final String CONSTRUCTOR_NAME = "<init>";

    final Type[] argumentTypes;
    final Type returnType;
    boolean returnIsNotNull;
    private boolean instrumented = false;
    int syntheticCount;
    final int access;
    final String methodName;
    final String className;
    final List<Integer> notNullParams;
    Label startGeneratedCodeLabel;

    ThrowOnNullMethodVisitor(final int api, final MethodVisitor mv, @NotNull final Type[] argumentTypes, final Type returnType, final int access, final String methodName, final String className, final boolean returnIsNotNull) {
        super(api, mv);
        this.argumentTypes = argumentTypes;
        this.returnType = returnType;
        this.access = access;
        this.methodName = methodName;
        this.className = className;
        this.returnIsNotNull = returnIsNotNull;
        syntheticCount = 0;
        notNullParams = new ArrayList<>();
    }

    private void setInstrumented() {
        instrumented = true;
    }

    /**
     * Visits a zero operand instruction (ie return).
     * <p>
     * {@inheritDoc}
     */
    public void visitInsn(final int opcode) {
        if (opcode == Opcodes.ARETURN) {
            if (returnIsNotNull) {
                mv.visitInsn(Opcodes.DUP);
                final Label skipLabel = new Label();
                mv.visitJumpInsn(Opcodes.IFNONNULL, skipLabel);
                generateThrow(ISE_CLASS_NAME, "NotNull method " + className + "." + methodName + " must not return null", skipLabel);
            }
        }

        mv.visitInsn(opcode);
    }

    boolean hasInstrumented() {
        return instrumented;
    }

    private boolean isStatic() {
        return (access & Opcodes.ACC_STATIC) != 0;
    }

    boolean isParameter(int index) {
        return isStatic() ? index < argumentTypes.length : index <= argumentTypes.length;
    }

    /**
     * Starts the visit of the method's code, if any (ie non abstract method).
     */
    @Override
    public void visitCode() {
        if (!notNullParams.isEmpty()) {
            startGeneratedCodeLabel = new Label();
            mv.visitLabel(startGeneratedCodeLabel);
        }
        for (final Integer notNullParam : notNullParams) {
            int var = ((access & Opcodes.ACC_STATIC) == 0) ? 1 : 0;
            for (int i = 0; i < notNullParam; ++i) {
                var += argumentTypes[i].getSize();
            }
            mv.visitVarInsn(Opcodes.ALOAD, var);

            final Label end = new Label();
            mv.visitJumpInsn(Opcodes.IFNONNULL, end);

            generateThrow(IAE_CLASS_NAME, getThrowMessage(notNullParam), end);
        }
    }

    private void generateThrow(@NotNull final String exceptionClass, @NotNull final String description, @NotNull final Label end) {
        final String exceptionParamClass = "(" + LangUtils.convertToJavaClassName(String.class.getName()) + ")V";
        mv.visitTypeInsn(Opcodes.NEW, exceptionClass);
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn(description);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, exceptionClass, CONSTRUCTOR_NAME, exceptionParamClass, false);
        mv.visitInsn(Opcodes.ATHROW);
        mv.visitLabel(end);

        setInstrumented();
    }

    @NotNull
    protected abstract String getThrowMessage(int parameterNumber);

}
