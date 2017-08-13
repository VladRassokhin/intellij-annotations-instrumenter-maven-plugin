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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import se.eris.asm.AsmUtils;
import se.eris.lang.LangUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class ThrowOnNullMethodVisitor extends MethodVisitor {

    static final String LJAVA_LANG_SYNTHETIC_ANNO = "Ljava/lang/Synthetic;";
    private static final String IAE_CLASS_NAME = "java/lang/IllegalArgumentException";
    private static final String ISE_CLASS_NAME = "java/lang/IllegalStateException";
    private static final String CONSTRUCTOR_NAME = "<init>";

    protected ArrayList<String> parameterNames = null;
    final Type[] argumentTypes;
    private final Type returnType;
    boolean isReturnNotNull;
    private final boolean isAnonymousClass;
    private boolean instrumented = false;
    private int syntheticCount;
    final int access;
    final String methodName;
    final String className;
    final List<Integer> notNullParams;
    Label startGeneratedCodeLabel;

    ThrowOnNullMethodVisitor(final int api, @Nullable final MethodVisitor mv, @NotNull final Type[] argumentTypes, final Type returnType, final int access, final String methodName, final String className, final boolean isReturnNotNull, boolean isAnonymousClass) {
        super(api, mv);
        this.argumentTypes = argumentTypes;
        this.returnType = returnType;
        this.access = access;
        this.methodName = methodName;
        this.className = className;
        this.isReturnNotNull = isReturnNotNull;
        this.isAnonymousClass = isAnonymousClass;
        syntheticCount = 0;
        notNullParams = new ArrayList<>();
    }

    private void setInstrumented() {
        instrumented = true;
    }

    /** This will be invoked only when visiting bytecode produced by java 8+ compiler with '-parameters' option. */
    @Override
    public void visitParameter(String name, int access) {
        if (parameterNames == null) {
            parameterNames = new ArrayList<>(argumentTypes.length);
        }
        parameterNames.add(name);
        if (mv != null) mv.visitParameter(name, access);
    }

    /**
     * Visits a zero operand instruction (ie return).
     * <p>
     * {@inheritDoc}
     */
    public void visitInsn(final int opcode) {
        if (shouldInclude() && opcode == Opcodes.ARETURN) {
            if (isReturnNotNull) {
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
        if (shouldInclude()) {
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
        mv.visitCode();
    }

    protected boolean shouldInclude() {
        return !shouldSkip();
    }

    private boolean shouldSkip() {
        return isSynthetic() || isAnonymousClassConstructor();
    }

    private boolean isAnonymousClassConstructor() {
        return isAnonymousClass && isConstructor();
    }

    private boolean isConstructor() {
        return "<init>".equals(this.methodName);
    }

    @Contract(pure = true)
    private boolean isSynthetic() {
        return (this.access & Opcodes.ACC_SYNTHETIC) == Opcodes.ACC_SYNTHETIC;
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

    boolean isReturnReferenceType() {
        return AsmUtils.isReferenceType(this.returnType);
    }

    boolean isParameterReferenceType(int parameter) {
        return AsmUtils.isReferenceType(argumentTypes[parameter]);
    }

    @NotNull
    private String getThrowMessage(int parameterNumber) {
        int pnum = getSourceCodeParameterNumber(parameterNumber);
        String pname = parameterNames == null || parameterNames.size() <= pnum
            ? "" : String.format(" '%s'", parameterNames.get(pnum));
        return String.format(
            "Argument %d for %s parameter%s of %s.%s must not be null",
            pnum, notNullCause(), pname, className, methodName
        );
    }

    /** Returns the reason for the parameter to be instrumented as non-null one. */
    @NotNull
    protected abstract String notNullCause();

    int increaseSyntheticCount() {
        return syntheticCount++;
    }

    int getSourceCodeParameterNumber(int parameterNumber) {
        return parameterNumber - syntheticCount;
    }

}
