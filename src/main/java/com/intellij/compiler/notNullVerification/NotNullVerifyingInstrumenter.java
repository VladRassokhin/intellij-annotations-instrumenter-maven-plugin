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
package com.intellij.compiler.notNullVerification;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;
import se.eris.asm.AsmUtils;
import se.eris.lang.LangUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ven
 * @author Vladislav.Rassokhin
 * noinspection HardCodedStringLiteral
 */
public class NotNullVerifyingInstrumenter extends ClassVisitor implements Opcodes {
    public static final String LJAVA_LANG_SYNTHETIC_ANNO = "Ljava/lang/Synthetic;";
    public static final String IAE_CLASS_NAME = "java/lang/IllegalArgumentException";
    public static final String ISE_CLASS_NAME = "java/lang/IllegalStateException";
    private static final String CONSTRUCTOR_NAME = "<init>";

    private String myClassName;
    private boolean myIsModification = false;
    private final Set<String> notNullAnnotations;

    public NotNullVerifyingInstrumenter(@NotNull final ClassVisitor classVisitor, @NotNull final Set<String> notNullAnnotations) {
        super(Opcodes.ASM5, classVisitor);
        this.notNullAnnotations = new HashSet<String>();
        for (@NotNull final String annotation : notNullAnnotations) {
            this.notNullAnnotations.add(LangUtils.convertToJavaClassName(annotation));
        }
    }

    public boolean isModification() {
        return myIsModification;
    }

    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        myClassName = name;
    }

    public MethodVisitor visitMethod(final int access, @NotNull final String name, final String desc, final String signature, final String[] exceptions) {
        final Type[] argumentTypes = Type.getArgumentTypes(desc);
        final Type returnType = Type.getReturnType(desc);
        final MethodVisitor v = cv.visitMethod(access, name, desc, signature, exceptions);
        return new InstrumenterMethodVisitorThrow(v, argumentTypes, returnType, access, name);
    }

    private class InstrumenterMethodVisitorThrow extends MethodVisitor {

        private final Type[] argumentTypes;

        private final Type returnType;
        private final int access;
        private final String name;
        private final ArrayList<Integer> myNotNullParams;
        private int mySyntheticCount;
        private boolean myIsNotNull;
        private Label myStartGeneratedCodeLabel;

        public InstrumenterMethodVisitorThrow(@Nullable final MethodVisitor v, @NotNull final Type[] argumentTypes, @NotNull final Type returnType, final int access, @NotNull final String name) {
            super(Opcodes.ASM5, v);
            this.argumentTypes = argumentTypes;
            this.returnType = returnType;
            this.access = access;
            this.name = name;
            myNotNullParams = new ArrayList<Integer>();
            mySyntheticCount = 0;
            myIsNotNull = false;
        }

        /**
         * Visits an annotation of a parameter this method.
         *
         * {@inheritDoc}
         */
        public AnnotationVisitor visitParameterAnnotation(final int parameter, final String annotation, final boolean visible) {
            final AnnotationVisitor av = mv.visitParameterAnnotation(parameter, annotation, visible);
            if (AsmUtils.isReferenceType(argumentTypes[parameter]) && isNotNullAnnotation(annotation)) {
                myNotNullParams.add(parameter);
            } else if (annotation.equals(LJAVA_LANG_SYNTHETIC_ANNO)) {
                // See asm r1278 for what we do this,
                // http://forge.objectweb.org/tracker/index.php?func=detail&aid=307392&group_id=23&atid=100023
                mySyntheticCount++;
            }
            return av;
        }

        /**
         * Visits an annotation of this method.
         *
         * {@inheritDoc}
         */
        public AnnotationVisitor visitAnnotation(final String annotation, final boolean visible) {
            final AnnotationVisitor av = mv.visitAnnotation(annotation, visible);
            if (AsmUtils.isReferenceType(returnType) && isNotNullAnnotation(annotation)) {
                myIsNotNull = true;
            }

            return av;
        }

        /**
         * Starts the visit of the method's code, if any (ie non abstract method).
         */
        public void visitCode() {
            if (!myNotNullParams.isEmpty()) {
                myStartGeneratedCodeLabel = new Label();
                mv.visitLabel(myStartGeneratedCodeLabel);
            }
            for (final Integer myNotNullParam : myNotNullParams) {
                int var = ((access & ACC_STATIC) == 0) ? 1 : 0;
                for (int i = 0; i < myNotNullParam; ++i) {
                    var += argumentTypes[i].getSize();
                }
                mv.visitVarInsn(ALOAD, var);

                final Label end = new Label();
                mv.visitJumpInsn(IFNONNULL, end);

                generateThrow(IAE_CLASS_NAME, "Argument " + (myNotNullParam - mySyntheticCount) + " for @NotNull parameter of " + myClassName + "." + name + " must not be null", end);
            }
        }

        /**
         * Visits a local variable declaration.
         *
         * {@inheritDoc}
         */
        public void visitLocalVariable(final String name, final String description, final String signature, final Label start, final Label end, final int index) {
            final boolean isStatic = (access & ACC_STATIC) != 0;
            final boolean isParameter = isStatic ? index < argumentTypes.length : index <= argumentTypes.length;
            mv.visitLocalVariable(name, description, signature, (isParameter && myStartGeneratedCodeLabel != null) ? myStartGeneratedCodeLabel : start, end, index);
        }

        /**
         * Visits a zero operand instruction (ie return).
         *
         * {@inheritDoc}
         */
        public void visitInsn(final int opcode) {
            if (opcode == ARETURN) {
                if (myIsNotNull) {
                    mv.visitInsn(DUP);
                    final Label skipLabel = new Label();
                    mv.visitJumpInsn(IFNONNULL, skipLabel);
                    generateThrow(ISE_CLASS_NAME, "@NotNull method " + myClassName + "." + name + " must not return null", skipLabel);
                }
            }

            mv.visitInsn(opcode);
        }

        private void generateThrow(final String exceptionClass, final String description, final Label end) {
            final String exceptionParamClass = "(" + LangUtils.convertToJavaClassName(java.lang.String.class.getName()) + ")V";
            mv.visitTypeInsn(NEW, exceptionClass);
            mv.visitInsn(DUP);
            mv.visitLdcInsn(description);
            mv.visitMethodInsn(INVOKESPECIAL, exceptionClass, CONSTRUCTOR_NAME, exceptionParamClass, false);
            mv.visitInsn(ATHROW);
            mv.visitLabel(end);

            myIsModification = true;
        }

        public void visitMaxs(final int maxStack, final int maxLocals) {
            try {
                super.visitMaxs(maxStack, maxLocals);
            }
            catch (final ArrayIndexOutOfBoundsException e) {
                throw new ArrayIndexOutOfBoundsException("visitMaxs processing failed for method " + name + ": " + e.getMessage());
            }
        }

    }

    private boolean isNotNullAnnotation(@NotNull final String annotation) {
        return notNullAnnotations.contains(annotation);
    }

}
