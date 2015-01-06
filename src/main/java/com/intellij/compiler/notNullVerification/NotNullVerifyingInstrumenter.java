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

import org.objectweb.asm.*;

import java.util.ArrayList;

/**
 * @author ven
 * @author Vladislav.Rassokhin
 * @noinspection HardCodedStringLiteral
 */
public class NotNullVerifyingInstrumenter extends ClassVisitor implements Opcodes {
  public static final String LJAVA_LANG_SYNTHETIC_ANNO = "Ljava/lang/Synthetic;";
  private boolean myIsModification = false;
  private String myClassName;
  public static final String NOT_NULL = "org/jetbrains/annotations/NotNull";
  public static final String NOT_NULL_ANNO = "L" + NOT_NULL + ";";
  public static final String IAE_CLASS_NAME = "java/lang/IllegalArgumentException";
  public static final String ISE_CLASS_NAME = "java/lang/IllegalStateException";
  private static final String CONSTRUCTOR_NAME = "<init>";

  public NotNullVerifyingInstrumenter(final ClassVisitor classVisitor) {
    super(Opcodes.ASM5, classVisitor);
  }

  public boolean isModification() {
    return myIsModification;
  }

  public void visit(final int version,
                    final int access,
                    final String name,
                    final String signature,
                    final String superName,
                    final String[] interfaces) {
    super.visit(version, access, name, signature, superName, interfaces);
    myClassName = name;
  }

  public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
    final Type[] args = Type.getArgumentTypes(desc);
    final Type returnType = Type.getReturnType(desc);
    final MethodVisitor v = cv.visitMethod(access, name, desc, signature, exceptions);
    return new MethodVisitor(Opcodes.ASM5, v) {

      private final ArrayList<Integer> myNotNullParams = new ArrayList<Integer>();
      private int mySyntheticCount = 0;
      private boolean myIsNotNull = false;
      private Label myStartGeneratedCodeLabel;

      public AnnotationVisitor visitParameterAnnotation(final int parameter, final String anno, final boolean visible) {
        final AnnotationVisitor av = mv.visitParameterAnnotation(parameter, anno, visible);
        if (isReferenceType(args[parameter]) && anno.equals(NOT_NULL_ANNO)) {
          myNotNullParams.add(parameter);
        } else if (anno.equals(LJAVA_LANG_SYNTHETIC_ANNO)) {
          // See asm r1278 for what we do this,
          // http://forge.objectweb.org/tracker/index.php?func=detail&aid=307392&group_id=23&atid=100023
          mySyntheticCount++;
        }
        return av;
      }

      public AnnotationVisitor visitAnnotation(String anno, boolean isRuntime) {
        final AnnotationVisitor av = mv.visitAnnotation(anno, isRuntime);
        if (isReferenceType(returnType) && anno.equals(NOT_NULL_ANNO)) {
          myIsNotNull = true;
        }

        return av;
      }

      public void visitCode() {
        if (myNotNullParams.size() > 0) {
          myStartGeneratedCodeLabel = new Label();
          mv.visitLabel(myStartGeneratedCodeLabel);
        }
        for (Integer myNotNullParam : myNotNullParams) {
          int var = ((access & ACC_STATIC) == 0) ? 1 : 0;
          int param = myNotNullParam;
          for (int i = 0; i < param; ++i) {
            var += args[i].getSize();
          }
          mv.visitVarInsn(ALOAD, var);

          Label end = new Label();
          mv.visitJumpInsn(IFNONNULL, end);

          generateThrow(IAE_CLASS_NAME, "Argument " + (param - mySyntheticCount) + " for @NotNull parameter of " + myClassName + "." + name + " must not be null", end);
        }
      }

      public void visitLocalVariable(final String name, final String desc, final String signature, final Label start, final Label end,
                                     final int index) {
        final boolean isStatic = (access & ACC_STATIC) != 0;
        final boolean isParameter = isStatic ? index < args.length : index <= args.length;
        mv.visitLocalVariable(name, desc, signature, (isParameter && myStartGeneratedCodeLabel != null) ? myStartGeneratedCodeLabel : start, end, index);
      }

      public void visitInsn(int opcode) {
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

      private void generateThrow(final String exceptionClass, final String descr, final Label end) {
        String exceptionParamClass = "(Ljava/lang/String;)V";
        mv.visitTypeInsn(NEW, exceptionClass);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(descr);
        mv.visitMethodInsn(INVOKESPECIAL, exceptionClass, CONSTRUCTOR_NAME, exceptionParamClass, false);
        mv.visitInsn(ATHROW);
        mv.visitLabel(end);

        myIsModification = true;
      }

      public void visitMaxs(final int maxStack, final int maxLocals) {
        try {
          super.visitMaxs(maxStack, maxLocals);
        } catch (ArrayIndexOutOfBoundsException e) {
          throw new ArrayIndexOutOfBoundsException("visitMaxs processing failed for method " + name + ": " + e.getMessage());
        }
      }
    };
  }

  private static boolean isReferenceType(final Type type) {
    return type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY;
  }
}
