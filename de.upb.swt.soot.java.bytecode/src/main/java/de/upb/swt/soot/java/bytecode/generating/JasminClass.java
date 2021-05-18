package de.upb.swt.soot.java.bytecode.generating;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999 Patrick Lam, Patrick Pominville and Raja Vallee-Rai
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.expr.*;
import de.upb.swt.soot.core.jimple.common.ref.IdentityRef;
import de.upb.swt.soot.core.jimple.common.ref.JCaughtExceptionRef;
import de.upb.swt.soot.core.jimple.common.ref.JParameterRef;
import de.upb.swt.soot.core.jimple.common.ref.JThisRef;
import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JEnterMonitorStmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JExitMonitorStmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JSwitchStmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JasminClass extends AbstractJasminClass {
  private static final Logger logger = LoggerFactory.getLogger(JasminClass.class);

  public JasminClass(SootClass sootClass) {
    super(sootClass);
  }

  @Override
  protected void emitMethodBody(SootMethod method) {

    Body body = method.getBody();
    // hint: [ms] if its a jimple this was done: body =
    // PackManager.v().convertJimpleBodyToBaf(method);

    if (body == null) {
      throw new RuntimeException("method: " + method.getName() + " has no active body!");
    }

    List<Stmt> instList = body.getStmts();

    int stackLimitIndex = -1;

    subroutineToReturnAddressSlot = new HashMap<>(10, 0.7f);

    // Determine the unitToLabel map
    {
      unitToLabel = new HashMap<>(instList.size() * 2 + 1, 0.7f);
      labelCount = 0;

      for (UnitBox uBox : body.getUnitBoxes(true)) {
        // Assign a label for each statement reference
        {
          InstBox box = (InstBox) uBox;

          if (!unitToLabel.containsKey(box.getUnit())) {
            unitToLabel.put(box.getUnit(), "label" + labelCount++);
          }
        }
      }
    }

    // Emit the exceptions, recording the Units at the beginning
    // of handlers so that later on we can recognize blocks that
    // begin with an exception on the stack.
    Set<Stmt> handlerUnits = new HashSet<>(body.getTraps().size());
    {
      for (Trap trap : body.getTraps()) {
        handlerUnits.add(trap.getHandlerStmt());
        if (trap.getBeginStmt()
            != trap.getEndStmt()) { // TODO: [ms] refactor this check as a basic validator
          emit(
              ".catch "
                  + slashify(trap.getExceptionType().getFullyQualifiedName())
                  + " from "
                  + unitToLabel.get(trap.getBeginStmt())
                  + " to "
                  + unitToLabel.get(trap.getEndStmt())
                  + " using "
                  + unitToLabel.get(trap.getHandlerStmt()));
        }
      }
    }

    // Determine where the locals go
    {
      int localCount = 0;
      int[] paramSlots = new int[method.getParameterCount()];
      int thisSlot = 0;
      Set<Local> assignedLocals = new HashSet<Local>();

      localToSlot = new HashMap<Local, Integer>(body.getLocalCount() * 2 + 1, 0.7f);

      // assignColorsToLocals(body);

      // Determine slots for 'this' and parameters
      {
        if (!method.isStatic()) {
          thisSlot = 0;
          localCount++;
        }

        for (int i = 0; i < method.getParameterCount(); i++) {
          paramSlots[i] = localCount;
          localCount += sizeOfType(method.getParameterType(i));
        }
      }

      // Handle identity statements
      {
        for (Stmt s : instList) {
          if (s instanceof JIdentityStmt && ((JIdentityStmt) s).getLeftOp() instanceof Local) {
            Local l = (Local) ((JIdentityStmt) s).getLeftOp();
            IdentityRef identity = (IdentityRef) ((JIdentityStmt) s).getRightOp();

            int slot;
            if (identity instanceof JThisRef) {
              // TODO: [ms] refactor this check into general validators!
              if (method.isStatic()) {
                throw new RuntimeException("Attempting to use 'this' in static method");
              }

              slot = thisSlot;
            } else if (identity instanceof JParameterRef) {
              slot = paramSlots[((JParameterRef) identity).getIndex()];
            } else {
              // Exception ref. Skip over this
              continue;
            }

            localToSlot.put(l, slot);
            assignedLocals.add(l);
          }
        }
      }

      // Assign the rest of the locals
      {
        for (Local local : body.getLocals()) {
          if (assignedLocals.add(local)) {
            localToSlot.put(local, localCount);
            localCount += sizeOfType(local.getType());
          }
        }

        if (!Modifier.isNative(method.getModifiers())
            && !Modifier.isAbstract(method.getModifiers())) {
          emit("    .limit stack ?");
          stackLimitIndex = code.size() - 1;

          emit("    .limit locals " + localCount);
        }
      }
    }

    // Emit code in one pass
    {
      isEmittingMethodCode = true;
      maxStackHeight = 0;
      isNextGotoAJsr = false;

      for (Stmt s : instList) {

        if (unitToLabel.containsKey(s)) {
          emit(unitToLabel.get(s) + ":");
        }

        // emit this statement
        {
          emitInst(s);
        }
      }

      isEmittingMethodCode = false;

      // calculate max stack height
      {
        maxStackHeight = 0;
        if (body.getStmtGraph().nodes().size() != 0) {
          StmtGraph blockGraph = new BriefBlockGraph(activeBody);
          List<Block> blocks = blockGraph.getBlocks();

          if (blocks.size() != 0) {
            // set the stack height of the entry points
            List<Block> entryPoints = ((DirectedGraph<Block>) blockGraph).getHeads();
            for (Block entryBlock : entryPoints) {
              int initialHeight;
              if (handlerUnits.contains(entryBlock.getHead())) {
                initialHeight = 1;
              } else {
                initialHeight = 0;
              }
              if (blockToStackHeight == null) {
                blockToStackHeight = new HashMap<Block, Integer>();
              }
              blockToStackHeight.put(entryBlock, initialHeight);
              if (blockToLogicalStackHeight == null) {
                blockToLogicalStackHeight = new HashMap<Block, Integer>();
              }
              blockToLogicalStackHeight.put(entryBlock, initialHeight);
            }

            // dfs the block graph using the blocks in the
            // entryPoints list as roots
            for (Block nextBlock : entryPoints) {
              calculateStackHeight(nextBlock);
              calculateLogicalStackHeightCheck(nextBlock);
            }
          }
        }
      }

      if (!Modifier.isNative(method.getModifiers())
          && !Modifier.isAbstract(method.getModifiers())) {
        code.set(stackLimitIndex, "    .limit stack " + maxStackHeight);
      }
    }

    /* FIXME emit code attributes
    for (Tag t : body.getTags()) {
        if (t instanceof JasminAttribute) {
            emit(".code_attribute " + t.getName() + " \"" + ((JasminAttribute) t).getJasminValue(unitToLabel) + "\"");
        }
    }
    */

  }

  void emitInst(Stmt inst) {
    StmtPositionInfo positionInfo = inst.getPositionInfo();
    if (positionInfo != null) {
      emit(".line " + positionInfo.getStmtPosition().getFirstLine());
    }
    inst.apply(
        new StmtSwitch() {
          @Override
          public void caseReturnVoidInst(JReturnVoidStmt i) {
            emit("return");
          }

          @Override
          public void caseReturnInst(JReturnStmt i) {
            i.getOpType()
                .apply(
                    new TypeSwitch() {
                      @Override
                      public void defaultCase(PrimitiveType t) {
                        throw new RuntimeException("invalid return type " + t.toString());
                      }

                      @Override
                      public void caseDoubleType(PrimitiveType t) {
                        emit("dreturn");
                      }

                      @Override
                      public void caseFloatType(PrimitiveType t) {
                        emit("freturn");
                      }

                      @Override
                      public void caseIntType(PrimitiveType t) {
                        emit("ireturn");
                      }

                      @Override
                      public void caseByteType(PrimitiveType t) {
                        emit("ireturn");
                      }

                      @Override
                      public void caseShortType(PrimitiveType t) {
                        emit("ireturn");
                      }

                      @Override
                      public void caseCharType(PrimitiveType t) {
                        emit("ireturn");
                      }

                      @Override
                      public void caseBooleanType(PrimitiveType t) {
                        emit("ireturn");
                      }

                      @Override
                      public void caseLongType(PrimitiveType t) {
                        emit("lreturn");
                      }

                      @Override
                      public void caseArrayType(PrimitiveType t) {
                        emit("areturn");
                      }

                      @Override
                      public void caseRefType(PrimitiveType t) {
                        emit("areturn");
                      }

                      @Override
                      public void caseNullType(PrimitiveType t) {
                        emit("areturn");
                      }
                    });
          }

          @Override
          public void caseNopInst(JNopStmt i) {
            emit("nop");
          }

          @Override
          public void caseEnterMonitorInst(JEnterMonitorStmt i) {
            emit("monitorenter");
          }

          @Override
          public void casePopInst(PopInst i) {
            if (i.getWordCount() == 2) {
              emit("pop2");
            } else {
              emit("pop");
            }
          }

          @Override
          public void caseExitMonitorInst(JExitMonitorStmt i) {
            emit("monitorexit");
          }

          @Override
          public void caseGotoInst(JGotoStmt i) {
            emit("goto " + unitToLabel.get(i.getTarget()));
          }

          @Override
          public void caseJSRInst(JSRInst i) {
            emit("jsr " + unitToLabel.get(i.getTarget()));
          }

          @Override
          public void casePushInst(PushInst i) {
            if (i.getConstant() instanceof IntConstant) {
              IntConstant v = (IntConstant) (i.getConstant());
              if (v.value == -1) {
                emit("iconst_m1");
              } else if (v.value >= 0 && v.value <= 5) {
                emit("iconst_" + v.value);
              } else if (v.value >= Byte.MIN_VALUE && v.value <= Byte.MAX_VALUE) {
                emit("bipush " + v.value);
              } else if (v.value >= Short.MIN_VALUE && v.value <= Short.MAX_VALUE) {
                emit("sipush " + v.value);
              } else {
                emit("ldc " + v.toString());
              }
            } else if (i.getConstant() instanceof StringConstant) {
              emit("ldc " + i.getConstant().toString());
            } else if (i.getConstant() instanceof ClassConstant) {
              emit("ldc " + ((ClassConstant) i.getConstant()).toInternalString());
            } else if (i.getConstant() instanceof DoubleConstant) {
              DoubleConstant v = (DoubleConstant) (i.getConstant());

              if ((v.value == 0) && ((1.0 / v.value) > 0.0)) {
                emit("dconst_0");
              } else if (v.value == 1) {
                emit("dconst_1");
              } else {
                String s = doubleToString(v);
                emit("ldc2_w " + s);
              }
            } else if (i.getConstant() instanceof FloatConstant) {
              FloatConstant v = (FloatConstant) (i.getConstant());
              if ((v.value == 0) && ((1.0f / v.value) > 1.0f)) {
                emit("fconst_0");
              } else if (v.value == 1) {
                emit("fconst_1");
              } else if (v.value == 2) {
                emit("fconst_2");
              } else {
                String s = floatToString(v);
                emit("ldc " + s);
              }
            } else if (i.getConstant() instanceof LongConstant) {
              LongConstant v = (LongConstant) (i.getConstant());
              if (v.value == 0) {
                emit("lconst_0");
              } else if (v.value == 1) {
                emit("lconst_1");
              } else {
                emit("ldc2_w " + v.toString());
              }
            } else if (i.getConstant() instanceof NullConstant) {
              emit("aconst_null");
            } else if (i.getConstant() instanceof MethodHandle) {
              throw new RuntimeException(
                  "MethodHandle constants not supported by Jasmin. Please use -asm-backend.");
            } else {
              throw new RuntimeException("unsupported opcode");
            }
          }

          @Override
          public void caseIdentityInst(JIdentityStmt i) {
            if (i.getRightOp() instanceof JCaughtExceptionRef && i.getLeftOp() instanceof Local) {
              int slot = localToSlot.get(i.getLeftOp()).intValue();

              if (slot >= 0 && slot <= 3) {
                emit("astore_" + slot);
              } else {
                emit("astore " + slot);
              }
            }
          }

          @Override
          public void caseStoreInst(StoreInst i) {
            final int slot = localToSlot.get(i.getLocal()).intValue();

            i.getOpType()
                .apply(
                    new TypeSwitch() {
                      @Override
                      public void caseArrayType(ArrayType t) {
                        if (slot >= 0 && slot <= 3) {
                          emit("astore_" + slot);
                        } else {
                          emit("astore " + slot);
                        }
                      }

                      @Override
                      public void caseDoubleType(PrimitiveType t) {
                        if (slot >= 0 && slot <= 3) {
                          emit("dstore_" + slot);
                        } else {
                          emit("dstore " + slot);
                        }
                      }

                      @Override
                      public void caseFloatType(PrimitiveType t) {
                        if (slot >= 0 && slot <= 3) {
                          emit("fstore_" + slot);
                        } else {
                          emit("fstore " + slot);
                        }
                      }

                      @Override
                      public void caseIntType(PrimitiveType t) {
                        if (slot >= 0 && slot <= 3) {
                          emit("istore_" + slot);
                        } else {
                          emit("istore " + slot);
                        }
                      }

                      @Override
                      public void caseByteType(PrimitiveType t) {
                        if (slot >= 0 && slot <= 3) {
                          emit("istore_" + slot);
                        } else {
                          emit("istore " + slot);
                        }
                      }

                      @Override
                      public void caseShortType(PrimitiveType t) {
                        if (slot >= 0 && slot <= 3) {
                          emit("istore_" + slot);
                        } else {
                          emit("istore " + slot);
                        }
                      }

                      @Override
                      public void caseCharType(PrimitiveType t) {
                        if (slot >= 0 && slot <= 3) {
                          emit("istore_" + slot);
                        } else {
                          emit("istore " + slot);
                        }
                      }

                      @Override
                      public void caseBooleanType(PrimitiveType t) {
                        if (slot >= 0 && slot <= 3) {
                          emit("istore_" + slot);
                        } else {
                          emit("istore " + slot);
                        }
                      }

                      @Override
                      public void caseLongType(PrimitiveType t) {
                        if (slot >= 0 && slot <= 3) {
                          emit("lstore_" + slot);
                        } else {
                          emit("lstore " + slot);
                        }
                      }

                      @Override
                      public void caseRefType(PrimitiveType t) {
                        if (slot >= 0 && slot <= 3) {
                          emit("astore_" + slot);
                        } else {
                          emit("astore " + slot);
                        }
                      }

                      @Override
                      public void caseStmtAddressType(StmtAddressType t) {
                        isNextGotoAJsr = true;
                        returnAddressSlot = slot;

                        /*
                         * if ( slot >= 0 && slot <= 3) emit("astore_" + slot, ); else emit("astore " + slot, );
                         */
                      }

                      @Override
                      public void caseNullType(NullType t) {
                        if (slot >= 0 && slot <= 3) {
                          emit("astore_" + slot);
                        } else {
                          emit("astore " + slot);
                        }
                      }

                      @Override
                      public void defaultCase(Type t) {
                        throw new RuntimeException("Invalid local type:" + t);
                      }
                    });
          }

          @Override
          public void caseLoadInst(LoadInst i) {
            final int slot = localToSlot.get(i.getLocal()).intValue();

            i.getOpType()
                .apply(
                    new TypeSwitch() {
                      @Override
                      public void caseArrayType(ArrayType t) {
                        if (slot >= 0 && slot <= 3) {
                          emit("aload_" + slot);
                        } else {
                          emit("aload " + slot);
                        }
                      }

                      @Override
                      public void defaultCase(Type t) {
                        throw new RuntimeException("invalid local type to load" + t);
                      }

                      @Override
                      public void caseDoubleType(PrimitiveType t) {
                        if (slot >= 0 && slot <= 3) {
                          emit("dload_" + slot);
                        } else {
                          emit("dload " + slot);
                        }
                      }

                      @Override
                      public void caseFloatType(PrimitiveType t) {
                        if (slot >= 0 && slot <= 3) {
                          emit("fload_" + slot);
                        } else {
                          emit("fload " + slot);
                        }
                      }

                      @Override
                      public void caseIntType(PrimitiveType t) {
                        if (slot >= 0 && slot <= 3) {
                          emit("iload_" + slot);
                        } else {
                          emit("iload " + slot);
                        }
                      }

                      @Override
                      public void caseByteType(PrimitiveType t) {
                        if (slot >= 0 && slot <= 3) {
                          emit("iload_" + slot);
                        } else {
                          emit("iload " + slot);
                        }
                      }

                      @Override
                      public void caseShortType(PrimitiveType t) {
                        if (slot >= 0 && slot <= 3) {
                          emit("iload_" + slot);
                        } else {
                          emit("iload " + slot);
                        }
                      }

                      @Override
                      public void caseCharType(PrimitiveType t) {
                        if (slot >= 0 && slot <= 3) {
                          emit("iload_" + slot);
                        } else {
                          emit("iload " + slot);
                        }
                      }

                      @Override
                      public void caseBooleanType(PrimitiveType t) {
                        if (slot >= 0 && slot <= 3) {
                          emit("iload_" + slot);
                        } else {
                          emit("iload " + slot);
                        }
                      }

                      @Override
                      public void caseLongType(PrimitiveType t) {
                        if (slot >= 0 && slot <= 3) {
                          emit("lload_" + slot);
                        } else {
                          emit("lload " + slot);
                        }
                      }

                      @Override
                      public void caseRefType(ClassType t) {
                        if (slot >= 0 && slot <= 3) {
                          emit("aload_" + slot);
                        } else {
                          emit("aload " + slot);
                        }
                      }

                      @Override
                      public void caseNullType(NullType t) {
                        if (slot >= 0 && slot <= 3) {
                          emit("aload_" + slot);
                        } else {
                          emit("aload " + slot);
                        }
                      }
                    });
          }

          @Override
          public void caseArrayWriteInst(ArrayWriteInst i) {
            i.getOpType()
                .apply(
                    new TypeSwitch() {
                      @Override
                      public void caseArrayType(ArrayType t) {
                        emit("aastore");
                      }

                      @Override
                      public void caseDoubleType(PrimitiveType t) {
                        emit("dastore");
                      }

                      @Override
                      public void caseFloatType(PrimitiveType t) {
                        emit("fastore");
                      }

                      @Override
                      public void caseIntType(PrimitiveType t) {
                        emit("iastore");
                      }

                      @Override
                      public void caseLongType(PrimitiveType t) {
                        emit("lastore");
                      }

                      @Override
                      public void caseRefType(ReferenceType t) {
                        emit("aastore");
                      }

                      @Override
                      public void caseByteType(PrimitiveType t) {
                        emit("bastore");
                      }

                      @Override
                      public void caseBooleanType(PrimitiveType t) {
                        emit("bastore");
                      }

                      @Override
                      public void caseCharType(PrimitiveType t) {
                        emit("castore");
                      }

                      @Override
                      public void caseShortType(PrimitiveType t) {
                        emit("sastore");
                      }

                      @Override
                      public void defaultCase(Type t) {
                        throw new RuntimeException("Invalid type: " + t);
                      }
                    });
          }

          @Override
          public void caseArrayReadInst(ArrayReadInst i) {
            i.getOpType()
                .apply(
                    new TypeSwitch() {
                      @Override
                      public void caseArrayType(ArrayType ty) {
                        emit("aaload");
                      }

                      @Override
                      public void caseBooleanType(PrimitiveType ty) {
                        emit("baload");
                      }

                      @Override
                      public void caseByteType(PrimitiveType ty) {
                        emit("baload");
                      }

                      @Override
                      public void caseCharType(PrimitiveType ty) {
                        emit("caload");
                      }

                      @Override
                      public void defaultCase(Type ty) {
                        throw new RuntimeException("invalid base type");
                      }

                      @Override
                      public void caseDoubleType(PrimitiveType ty) {
                        emit("daload");
                      }

                      @Override
                      public void caseFloatType(PrimitiveType ty) {
                        emit("faload");
                      }

                      @Override
                      public void caseIntType(PrimitiveType ty) {
                        emit("iaload");
                      }

                      @Override
                      public void caseLongType(PrimitiveType ty) {
                        emit("laload");
                      }

                      @Override
                      public void caseNullType(NullType ty) {
                        emit("aaload");
                      }

                      @Override
                      public void caseRefType(ReferenceType ty) {
                        emit("aaload");
                      }

                      @Override
                      public void caseShortType(PrimitiveType ty) {
                        emit("saload");
                      }
                    });
          }

          @Override
          public void caseIfNullInst(IfNullInst i) {
            emit("ifnull " + unitToLabel.get(i.getTarget()));
          }

          @Override
          public void caseIfNonNullInst(IfNonNullInst i) {
            emit("ifnonnull " + unitToLabel.get(i.getTarget()));
          }

          @Override
          public void caseIfEqInst(IfEqInst i) {
            emit("ifeq " + unitToLabel.get(i.getTarget()));
          }

          @Override
          public void caseIfNeInst(IfNeInst i) {
            emit("ifne " + unitToLabel.get(i.getTarget()));
          }

          @Override
          public void caseIfGtInst(IfGtInst i) {
            emit("ifgt " + unitToLabel.get(i.getTarget()));
          }

          @Override
          public void caseIfGeInst(IfGeInst i) {
            emit("ifge " + unitToLabel.get(i.getTarget()));
          }

          @Override
          public void caseIfLtInst(IfLtInst i) {
            emit("iflt " + unitToLabel.get(i.getTarget()));
          }

          @Override
          public void caseIfLeInst(IfLeInst i) {
            emit("ifle " + unitToLabel.get(i.getTarget()));
          }

          @Override
          public void caseIfCmpEqInst(final IfCmpEqInst i) {
            i.getOpType()
                .apply(
                    new TypeSwitch() {
                      @Override
                      public void caseIntType(PrimitiveType t) {
                        emit("if_icmpeq " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseBooleanType(PrimitiveType t) {
                        emit("if_icmpeq " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseShortType(PrimitiveType t) {
                        emit("if_icmpeq " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseCharType(PrimitiveType t) {
                        emit("if_icmpeq " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseByteType(PrimitiveType t) {
                        emit("if_icmpeq " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseDoubleType(PrimitiveType t) {
                        emit("dcmpg");
                        emit("ifeq " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseLongType(PrimitiveType t) {
                        emit("lcmp");
                        emit("ifeq " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseFloatType(PrimitiveType t) {
                        emit("fcmpg");
                        emit("ifeq " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseArrayType(ArrayType t) {
                        emit("if_acmpeq " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseRefType(ReferenceType t) {
                        emit("if_acmpeq " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseNullType(NullType t) {
                        emit("if_acmpeq " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void defaultCase(Type t) {
                        throw new RuntimeException("invalid type");
                      }
                    });
          }

          @Override
          public void caseIfCmpNeInst(final IfCmpNeInst i) {
            i.getOpType()
                .apply(
                    new TypeSwitch() {
                      @Override
                      public void caseIntType(PrimitiveType t) {
                        emit("if_icmpne " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseBooleanType(PrimitiveType t) {
                        emit("if_icmpne " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseShortType(PrimitiveType t) {
                        emit("if_icmpne " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseCharType(PrimitiveType t) {
                        emit("if_icmpne " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseByteType(PrimitiveType t) {
                        emit("if_icmpne " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseDoubleType(PrimitiveType t) {
                        emit("dcmpg");
                        emit("ifne " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseLongType(PrimitiveType t) {
                        emit("lcmp");
                        emit("ifne " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseFloatType(PrimitiveType t) {
                        emit("fcmpg");
                        emit("ifne " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseArrayType(ArrayType t) {
                        emit("if_acmpne " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseRefType(ReferenceType t) {
                        emit("if_acmpne " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseNullType(NullType t) {
                        emit("if_acmpne " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void defaultCase(Type t) {
                        throw new RuntimeException("invalid type");
                      }
                    });
          }

          @Override
          public void caseIfCmpGtInst(final IfCmpGtInst i) {
            i.getOpType()
                .apply(
                    new TypeSwitch() {
                      @Override
                      public void caseIntType(PrimitiveType t) {
                        emit("if_icmpgt " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseBooleanType(PrimitiveType t) {
                        emit("if_icmpgt " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseShortType(PrimitiveType t) {
                        emit("if_icmpgt " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseCharType(PrimitiveType t) {
                        emit("if_icmpgt " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseByteType(PrimitiveType t) {
                        emit("if_icmpgt " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseDoubleType(PrimitiveType t) {
                        emit("dcmpg");
                        emit("ifgt " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseLongType(PrimitiveType t) {
                        emit("lcmp");
                        emit("ifgt " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseFloatType(PrimitiveType t) {
                        emit("fcmpg");
                        emit("ifgt " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseArrayType(ArrayType t) {
                        emit("if_acmpgt " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseRefType(ReferenceType t) {
                        emit("if_acmpgt " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseNullType(NullType t) {
                        emit("if_acmpgt " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void defaultCase(Type t) {
                        throw new RuntimeException("invalid type");
                      }
                    });
          }

          @Override
          public void caseIfCmpGeInst(final IfCmpGeInst i) {
            i.getOpType()
                .apply(
                    new TypeSwitch() {
                      @Override
                      public void caseIntType(PrimitiveType t) {
                        emit("if_icmpge " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseBooleanType(PrimitiveType t) {
                        emit("if_icmpge " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseShortType(PrimitiveType t) {
                        emit("if_icmpge " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseCharType(PrimitiveType t) {
                        emit("if_icmpge " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseByteType(PrimitiveType t) {
                        emit("if_icmpge " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseDoubleType(PrimitiveType t) {
                        emit("dcmpg");
                        emit("ifge " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseLongType(PrimitiveType t) {
                        emit("lcmp");
                        emit("ifge " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseFloatType(PrimitiveType t) {
                        emit("fcmpg");
                        emit("ifge " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseArrayType(ArrayType t) {
                        emit("if_acmpge " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseRefType(ReferenceType t) {
                        emit("if_acmpge " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseNullType(NullType t) {
                        emit("if_acmpge " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void defaultCase(Type t) {
                        throw new RuntimeException("invalid type");
                      }
                    });
          }

          @Override
          public void caseIfCmpLtInst(final IfCmpLtInst i) {
            i.getOpType()
                .apply(
                    new TypeSwitch() {
                      @Override
                      public void caseIntType(PrimitiveType t) {
                        emit("if_icmplt " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseBooleanType(PrimitiveType t) {
                        emit("if_icmplt " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseShortType(PrimitiveType t) {
                        emit("if_icmplt " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseCharType(PrimitiveType t) {
                        emit("if_icmplt " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseByteType(PrimitiveType t) {
                        emit("if_icmplt " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseDoubleType(PrimitiveType t) {
                        emit("dcmpg");
                        emit("iflt " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseLongType(PrimitiveType t) {
                        emit("lcmp");
                        emit("iflt " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseFloatType(PrimitiveType t) {
                        emit("fcmpg");
                        emit("iflt " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseArrayType(ArrayType t) {
                        emit("if_acmplt " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseRefType(ReferenceType t) {
                        emit("if_acmplt " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseNullType(NullType t) {
                        emit("if_acmplt " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void defaultCase(Type t) {
                        throw new RuntimeException("invalid type");
                      }
                    });
          }

          @Override
          public void caseIfCmpLeInst(final IfCmpLeInst i) {
            i.getOpType()
                .apply(
                    new TypeSwitch() {
                      @Override
                      public void caseIntType(PrimitiveType t) {
                        emit("if_icmple " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseBooleanType(PrimitiveType t) {
                        emit("if_icmple " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseShortType(PrimitiveType t) {
                        emit("if_icmple " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseCharType(PrimitiveType t) {
                        emit("if_icmple " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseByteType(PrimitiveType t) {
                        emit("if_icmple " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseDoubleType(PrimitiveType t) {
                        emit("dcmpg");
                        emit("ifle " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseLongType(PrimitiveType t) {
                        emit("lcmp");
                        emit("ifle " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseFloatType(PrimitiveType t) {
                        emit("fcmpg");
                        emit("ifle " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseArrayType(ArrayType t) {
                        emit("if_acmple " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseRefType(ReferenceType t) {
                        emit("if_acmple " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void caseNullType(NullType t) {
                        emit("if_acmple " + unitToLabel.get(i.getTarget()));
                      }

                      @Override
                      public void defaultCase(Type t) {
                        throw new RuntimeException("invalid type");
                      }
                    });
          }

          @Override
          public void caseStaticGetInst(StaticGetInst i) {
            SootFieldRef field = i.getFieldRef();
            emit(
                "getstatic "
                    + slashify(field.getDeclClassType().getFullyQualifiedName())
                    + "/"
                    + field.name()
                    + " "
                    + jasminDescriptorOf(field.type()));
          }

          @Override
          public void caseStaticPutInst(StaticPutInst i) {
            emit(
                "putstatic "
                    + slashify(i.getFieldRef().getDeclClassType().getFullyQualifiedName())
                    + "/"
                    + i.getFieldRef().name()
                    + " "
                    + jasminDescriptorOf(i.getFieldRef().type()));
          }

          @Override
          public void caseFieldGetInst(FieldGetInst i) {
            emit(
                "getfield "
                    + slashify(i.getFieldRef().getDeclClassType().getFullyQualifiedName())
                    + "/"
                    + i.getFieldRef().name()
                    + " "
                    + jasminDescriptorOf(i.getFieldRef().type()));
          }

          @Override
          public void caseFieldPutInst(FieldPutInst i) {
            emit(
                "putfield "
                    + slashify(i.getFieldRef().getDeclClassType().getFullyQualifiedName())
                    + "/"
                    + i.getFieldRef().name()
                    + " "
                    + jasminDescriptorOf(i.getFieldRef().type()));
          }

          @Override
          public void caseInstanceCastInst(InstanceCastInst i) {
            Type castType = i.getCastType();

            if (castType instanceof RefType) {
              emit("checkcast " + slashify(((ReferenceType) castType).getClassName()));
            } else if (castType instanceof ArrayType) {
              emit("checkcast " + jasminDescriptorOf(castType));
            }
          }

          @Override
          public void caseInstanceOfInst(InstanceOfInst i) {
            Type checkType = i.getCheckType();

            if (checkType instanceof RefType) {
              emit("instanceof " + slashify(checkType.toString()));
            } else if (checkType instanceof ArrayType) {
              emit("instanceof " + jasminDescriptorOf(checkType));
            }
          }

          @Override
          public void caseNewInst(NewInst i) {
            emit("new " + slashify(i.getBaseType().getClassName()));
          }

          @Override
          public void casePrimitiveCastInst(PrimitiveCastInst i) {
            emit(i.toString());
          }

          @Override
          public void caseDynamicInvokeInst(DynamicInvokeInst i) {
            MethodSignature m = i.getMethodSignature();
            MethodSignature bsm = i.getBootstrapMethodRef();
            String bsmArgString = "";
            for (Iterator<Value> iterator = i.getBootstrapArgs().iterator(); iterator.hasNext(); ) {
              Value val = iterator.next();
              bsmArgString += "(" + jasminDescriptorOf(val.getType()) + ")";
              bsmArgString += escape(val.toString());

              if (iterator.hasNext()) {
                bsmArgString += ",";
              }
            }
            emit(
                "invokedynamic \""
                    + m.name()
                    + "\" "
                    + jasminDescriptorOf(m)
                    + " "
                    + slashify(bsm.getDeclClassType().getFullyQualifiedName())
                    + "/"
                    + bsm.name()
                    + jasminDescriptorOf(bsm)
                    + "("
                    + bsmArgString
                    + ")");
          }

          private String escape(String bsmArgString) {
            return bsmArgString
                .replace(",", "\\comma")
                .replace(" ", "\\blank")
                .replace("\t", "\\tab")
                .replace("\n", "\\newline");
          }

          @Override
          public void caseStaticInvokeInst(JStaticInvokeExpr i) {
            MethodSignature m = i.getMethodSignature();

            emit(
                "invokestatic "
                    + slashify(m.getDeclClassType().getFullyQualifiedName())
                    + "/"
                    + m.getName()
                    + jasminDescriptorOf(m));
          }

          @Override
          public void caseVirtualInvokeInst(JVirtualInvokeExpr i) {
            MethodSignature m = i.getMethodSignature();

            emit(
                "invokevirtual "
                    + slashify(m.getDeclClassType().getFullyQualifiedName())
                    + "/"
                    + m.getName()
                    + jasminDescriptorOf(m));
          }

          @Override
          public void caseInterfaceInvokeInst(JInterfaceInvokeExpr i) {
            MethodSignature m = i.getMethodSignature();

            emit(
                "invokeinterface "
                    + slashify(m.getDeclClassType().getFullyQualifiedName())
                    + "/"
                    + m.getName()
                    + jasminDescriptorOf(m)
                    + " "
                    + (argCountOf(m) + 1));
          }

          @Override
          public void caseSpecialInvokeInst(JSpecialInvokeExpr i) {
            MethodSignature m = i.getMethodSignature();

            emit(
                "invokespecial "
                    + slashify(m.getDeclClassType().getFullyQualifiedName())
                    + "/"
                    + m.getName()
                    + jasminDescriptorOf(m));
          }

          @Override
          public void caseThrowInst(JThrowStmt i) {
            emit("athrow");
          }

          @Override
          public void caseCmpInst(JCmpExpr i) {
            emit("lcmp");
          }

          @Override
          public void caseCmplInst(JCmplExpr i) {
            // TODO: [ms] thats BAAAD!! getType() returns currently always PrimitiveType.getInt()
            // see others too
            if (i.getType() == PrimitiveType.getFloat()) {
              emit("fcmpl");
            } else {
              emit("dcmpl");
            }
          }

          @Override
          public void caseCmpgInst(JCmpgExpr i) {
            // TODO: [ms] thats BAAAD!! getType() returns currently always PrimitiveType.getInt()
            // see others too
            if (i.getType() == PrimitiveType.getFloat()) {
              emit("fcmpg");
            } else {
              emit("dcmpg");
            }
          }

          private void emitOpTypeInst(final String s, final OpTypeArgInst i) {
            i.getOpType()
                .apply(
                    new TypeSwitch() {
                      private void handleIntCase() {
                        emit("i" + s);
                      }

                      @Override
                      public void caseIntType(PrimitiveType t) {
                        handleIntCase();
                      }

                      @Override
                      public void caseBooleanType(PrimitiveType t) {
                        handleIntCase();
                      }

                      @Override
                      public void caseShortType(PrimitiveType t) {
                        handleIntCase();
                      }

                      @Override
                      public void caseCharType(PrimitiveType t) {
                        handleIntCase();
                      }

                      @Override
                      public void caseByteType(PrimitiveType t) {
                        handleIntCase();
                      }

                      @Override
                      public void caseLongType(PrimitiveType t) {
                        emit("l" + s);
                      }

                      @Override
                      public void caseDoubleType(PrimitiveType t) {
                        emit("d" + s);
                      }

                      @Override
                      public void caseFloatType(PrimitiveType t) {
                        emit("f" + s);
                      }

                      @Override
                      public void defaultCase(Type t) {
                        throw new RuntimeException("Invalid argument type for div");
                      }
                    });
          }

          @Override
          public void caseAddInst(JAddExpr i) {
            emitOpTypeInst("add", i);
          }

          @Override
          public void caseDivInst(JDivExpr i) {
            emitOpTypeInst("div", i);
          }

          @Override
          public void caseSubInst(JSubExpr i) {
            emitOpTypeInst("sub", i);
          }

          @Override
          public void caseMulInst(JMulExpr i) {
            emitOpTypeInst("mul", i);
          }

          @Override
          public void caseRemInst(JRemExpr i) {
            emitOpTypeInst("rem", i);
          }

          @Override
          public void caseShlInst(JShlExpr i) {
            emitOpTypeInst("shl", i);
          }

          @Override
          public void caseAndInst(JAndExpr i) {
            emitOpTypeInst("and", i);
          }

          @Override
          public void caseOrInst(JOrExpr i) {
            emitOpTypeInst("or", i);
          }

          @Override
          public void caseXorInst(JXorExpr i) {
            emitOpTypeInst("xor", i);
          }

          @Override
          public void caseShrInst(JShrExpr i) {
            emitOpTypeInst("shr", i);
          }

          @Override
          public void caseUshrInst(JUshrExpr i) {
            emitOpTypeInst("ushr", i);
          }

          @Override
          public void caseIncInst(IncInst i) {
            if (i.getUseBoxes().get(0).getValue() != i.getDefBoxes().get(0).getValue()) {
              throw new RuntimeException("iinc def and use boxes don't match");
            }

            emit("iinc " + localToSlot.get(i.getLocal()) + " " + i.getConstant());
          }

          @Override
          public void caseArrayLengthInst(JLengthExpr i) {
            emit("arraylength");
          }

          @Override
          public void caseNegInst(JNegExpr i) {
            emitOpTypeInst("neg", i);
          }

          @Override
          public void caseNewArrayInst(JNewArrayExpr i) {
            Type baseType = i.getBaseType();
            if (baseType instanceof ClassType) {
              emit("anewarray " + slashify(((ClassType) baseType).getFullyQualifiedName()));
            } else if (baseType instanceof ArrayType) {
              emit("anewarray " + jasminDescriptorOf(baseType));
            } else {
              emit("newarray " + baseType.toString());
            }
          }

          @Override
          public void caseNewMultiArrayInst(JNewMultiArrayExpr i) {
            emit(
                "multianewarray "
                    + jasminDescriptorOf(i.getBaseType())
                    + " "
                    + i.getSizeCount()); // was: i.getDimensions()
          }

          @Override
          public void caseLookupSwitchInst(JSwitchStmt i) {
            emit("lookupswitch");

            List<IntConstant> lookupValues = i.getLookupValues();
            List<Stmt> targets = i.getTargets();

            for (int j = 0; j < lookupValues.size(); j++) {
              emit("  " + lookupValues.get(j) + " : " + unitToLabel.get(targets.get(j)));
            }

            emit("  default : " + unitToLabel.get(i.getDefaultTarget()));
          }

          @Override
          public void caseTableSwitchInst(JSwitchStmt i) {
            emit("tableswitch " + i.getLowIndex() + " ; high = " + i.getHighIndex());

            List<Unit> targets = i.getTargets();

            for (int j = 0; j < targets.size(); j++) {
              emit("  " + unitToLabel.get(targets.get(j)));
            }

            emit("default : " + unitToLabel.get(i.getDefaultTarget()));
          }

          private boolean isDwordType(Type t) {
            // TODO: [ms] refactor into Primitivetype
            return t == PrimitiveType.getLong() || t == PrimitiveType.getDouble();
          }

          @Override
          public void caseDup1Inst(Dup1Inst i) {
            Type firstOpType = i.getOp1Type();
            if (isDwordType(firstOpType)) {
              emit("dup2"); // (form 2)
            } else {
              emit("dup");
            }
          }

          @Override
          public void caseDup2Inst(Dup2Inst i) {
            Type firstOpType = i.getOp1Type();
            Type secondOpType = i.getOp2Type();
            // The first two cases have no real bytecode equivalents.
            // Use a pair of insts to simulate them.
            if (isDwordType(firstOpType)) {
              emit("dup2"); // (form 2)
              if (isDwordType(secondOpType)) {
                emit("dup2"); // (form 2 -- by simulation)
              } else {
                emit("dup"); // also a simulation
              }
            } else if (isDwordType(secondOpType)) {
              if (isDwordType(firstOpType)) {
                emit("dup2"); // (form 2)
              } else {
                emit("dup");
              }
              emit("dup2"); // (form 2 -- complete the simulation)
            } else {
              emit("dup2"); // form 1
            }
          }

          @Override
          public void caseDup1_x1Inst(Dup1_x1Inst i) {
            Type opType = i.getOp1Type();
            Type underType = i.getUnder1Type();

            if (isDwordType(opType)) {
              if (isDwordType(underType)) {
                emit("dup2_x2"); // (form 4)
              } else {
                emit("dup2_x1"); // (form 2)
              }
            } else {
              if (isDwordType(underType)) {
                emit("dup_x2"); // (form 2)
              } else {
                emit("dup_x1"); // (only one form)
              }
            }
          }

          @Override
          public void caseDup1_x2Inst(Dup1_x2Inst i) {
            Type opType = i.getOp1Type();
            Type under1Type = i.getUnder1Type();
            Type under2Type = i.getUnder2Type();

            // 07-20-2006 Michael Batchelder
            // NOW handling all types of dup1_x2
            /*
             * From VM Spec: cat1 = category 1 (word type) cat2 = category 2 (doubleword)
             *
             * Form 1: [..., cat1_value3, cat1_value2, cat1_value1]->[..., cat1_value2, cat1_value1, cat1_value3, cat1_value2,
             * cat1_value1] Form 2: [..., cat1_value2, cat2_value1]->[..., cat2_value1, cat1_value2, cat2_value1]
             */

            if (isDwordType(opType)) {
              if (!isDwordType(under1Type) && !isDwordType(under2Type)) {
                emit("dup2_x2"); // (form 2)
              } else {
                throw new RuntimeException("magic not implemented yet");
              }
            } else {
              if (isDwordType(under1Type) || isDwordType(under2Type)) {
                throw new RuntimeException("magic not implemented yet");
              }
            }

            emit("dup_x2"); // (form 1)
          }

          @Override
          public void caseDup2_x1Inst(Dup2_x1Inst i) {
            Type op1Type = i.getOp1Type();
            Type op2Type = i.getOp2Type();
            Type under1Type = i.getUnder1Type();

            // 07-20-2006 Michael Batchelder
            // NOW handling all types of dup2_x1
            /*
             * From VM Spec: cat1 = category 1 (word type) cat2 = category 2 (doubleword)
             *
             * Form 1: [..., cat1_value3, cat1_value2, cat1_value1]->[..., cat1_value2, cat1_value1, cat1_value3, cat1_value2,
             * cat1_value1] Form 2: [..., cat1_value2, cat2_value1]->[..., cat2_value1, cat1_value2, cat2_value1]
             */
            if (isDwordType(under1Type)) {
              if (!isDwordType(op1Type) && !isDwordType(op2Type)) {
                throw new RuntimeException("magic not implemented yet");
              } else {
                emit("dup2_x2"); // (form 3)
              }
            } else {
              if ((isDwordType(op1Type) && op2Type != null) || isDwordType(op2Type)) {
                throw new RuntimeException("magic not implemented yet");
              }
            }

            emit("dup2_x1"); // (form 1)
          }

          @Override
          public void caseDup2_x2Inst(Dup2_x2Inst i) {
            Type op1Type = i.getOp1Type();
            Type op2Type = i.getOp2Type();
            Type under1Type = i.getUnder1Type();
            Type under2Type = i.getUnder2Type();

            // 07-20-2006 Michael Batchelder
            // NOW handling all types of dup2_x2

            /*
             * From VM Spec: cat1 = category 1 (word type) cat2 = category 2 (doubleword) Form 1: [..., cat1_value4, cat1_value3,
             * cat1_value2, cat1_value1]->[..., cat1_value2, cat1_value1, cat1_value4, cat1_value3, cat1_value2, cat1_value1]
             * Form 2: [..., cat1_value3, cat1_value2, cat2_value1]->[ ..., cat2_value1, cat1_value3, cat1_value2, cat2_value1]
             * Form 3: [..., cat2_value3, cat1_value2, cat1_value1]->[..., cat1_value2, cat1_value1, cat2_value3, cat1_value2,
             * cat1_value1] Form 4: [..., cat2_value2, cat2_value1]->[..., cat2_value1, cat2_value2, cat2_value1]
             */
            boolean malformed = true;
            if (isDwordType(op1Type)) {
              if (op2Type == null && under1Type != null) {
                if ((under2Type == null && isDwordType(under1Type))
                    || (!isDwordType(under1Type)
                        && under2Type != null
                        && !isDwordType(under2Type))) {
                  malformed = false;
                }
              }
            } else if (op1Type != null && op2Type != null && !isDwordType(op2Type)) {
              if ((under2Type == null && isDwordType(under1Type))
                  || (under1Type != null
                      && !isDwordType(under1Type)
                      && under2Type != null
                      && !isDwordType(under2Type))) {
                malformed = false;
              }
            }
            if (malformed) {
              throw new RuntimeException("magic not implemented yet");
            }

            emit("dup2_x2"); // (form 1)
          }

          @Override
          public void caseSwapInst(SwapInst i) {
            emit("swap");
          }
        });
  }

  private void calculateStackHeight(Block aBlock) {
    int blockHeight = blockToStackHeight.get(aBlock).intValue();
    if (blockHeight > maxStackHeight) {
      maxStackHeight = blockHeight;
    }

    for (Unit u : aBlock) {
      Inst nInst = (Inst) u;

      blockHeight -= nInst.getInMachineCount();

      if (blockHeight < 0) {
        throw new RuntimeException(
            "Negative Stack height has been attained in :"
                + aBlock.getBody().getMethod().getSignature()
                + " \n"
                + "StackHeight: "
                + blockHeight
                + "\n"
                + "At instruction:"
                + nInst
                + "\n"
                + "Block:\n"
                + aBlock
                + "\n\nMethod: "
                + aBlock.getBody().getMethod().getName()
                + "\n"
                + aBlock.getBody().getMethod());
      }

      blockHeight += nInst.getOutMachineCount();
      if (blockHeight > maxStackHeight) {
        maxStackHeight = blockHeight;
      }
      // logger.debug(">>> " + nInst + " " + blockHeight);
    }

    for (Block b : aBlock.getSuccs()) {
      Integer i = blockToStackHeight.get(b);
      if (i != null) {
        if (i.intValue() != blockHeight) {
          throw new RuntimeException(
              aBlock.getBody().getMethod().getSignature()
                  + ": incoherent stack height at block merge point "
                  + b
                  + aBlock
                  + "\ncomputed blockHeight == "
                  + blockHeight
                  + " recorded blockHeight = "
                  + i.intValue());
        }

      } else {
        blockToStackHeight.put(b, new Integer(blockHeight));
        calculateStackHeight(b);
      }
    }
  }

  private void calculateLogicalStackHeightCheck(Block aBlock) {
    int blockHeight = blockToLogicalStackHeight.get(aBlock).intValue();
    for (Unit u : aBlock) {
      Inst nInst = (Inst) u;

      blockHeight -= nInst.getInCount();

      if (blockHeight < 0) {
        throw new RuntimeException(
            "Negative Stack Logical height has been attained: \n"
                + "StackHeight: "
                + blockHeight
                + "\nAt instruction:"
                + nInst
                + "\nBlock:\n"
                + aBlock
                + "\n\nMethod: "
                + aBlock.getBody().getMethod().getName()
                + "\n"
                + aBlock.getBody().getMethod());
      }

      blockHeight += nInst.getOutCount();

      // logger.debug(">>> " + nInst + " " + blockHeight);
    }

    for (Block b : aBlock.getSuccs()) {
      Integer i = blockToLogicalStackHeight.get(b);
      if (i != null) {
        if (i.intValue() != blockHeight) {
          throw new RuntimeException(
              "incoherent logical stack height at block merge point " + b + aBlock);
        }

      } else {
        blockToLogicalStackHeight.put(b, new Integer(blockHeight));
        calculateLogicalStackHeightCheck(b);
      }
    }
  }
}

class GroupIntPair {
  Object group;
  int x;

  GroupIntPair(Object group, int x) {
    this.group = group;
    this.x = x;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof GroupIntPair) {
      return ((GroupIntPair) other).group.equals(this.group) && ((GroupIntPair) other).x == this.x;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return group.hashCode() + 1013 * x;
  }
}
