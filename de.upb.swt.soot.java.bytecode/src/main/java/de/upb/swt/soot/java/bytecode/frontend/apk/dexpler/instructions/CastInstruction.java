package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.instructions;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2012 Michael Markert, Frank Hartmann
 *
 * (c) 2012 University of Luxembourg - Interdisciplinary Centre for
 * Security Reliability and Trust (SnT) - All rights reserved
 * Alexandre Bartel
 *
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

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.common.expr.JCastExpr;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexBody;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.IDalvikTyper;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.tags.DoubleOpTag;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.tags.FloatOpTag;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.tags.IntOpTag;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.tags.LongOpTag;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.typing.DalvikTyper;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction;


public class CastInstruction extends TaggedInstruction {

  public CastInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  @Override
  public void jimplify(DexBody body) {
    TwoRegisterInstruction i = (TwoRegisterInstruction) instruction;
    int dest = i.getRegisterA();
    int source = i.getRegisterB();
    Type targetType = getTargetType();
    JCastExpr cast = Jimple.newCastExpr(body.getRegisterLocal(source), targetType);
    JAssignStmt assign = Jimple.newAssignStmt(body.getRegisterLocal(dest), cast, StmtPositionInfo.createNoStmtPositionInfo());
    assign.addTag(getTag());
    setStmt(assign);
    addTags(assign);
    body.add(assign);

    if (IDalvikTyper.ENABLE_DVKTYPER) {
      DalvikTyper.v().setType(assign.getLeftOpBox(), cast.getType(), false);
      // DalvikTyper.v().captureAssign((JAssignStmt)assign, op);
    }
  }

  /**
   * Return the appropriate target type for the covered opcodes.
   *
   * Note: the tag represents the original type before the cast. The cast type is not lost in Jimple and can be retrieved by
   * calling the getCastType() method.
   */
  private Type getTargetType() {
    Opcode opcode = instruction.getOpcode();
    switch (opcode) {
      case INT_TO_BYTE:
        setTag(new IntOpTag());
        return PrimitiveType.ByteType.getInstance();
      case INT_TO_CHAR:
        setTag(new IntOpTag());
        return PrimitiveType.CharType.getInstance();
      case INT_TO_SHORT:
        setTag(new IntOpTag());
        return PrimitiveType.ShortType.getInstance();

      case LONG_TO_INT:
        setTag(new LongOpTag());
        return PrimitiveType.IntType.getInstance();
      case DOUBLE_TO_INT:
        setTag(new DoubleOpTag());
        return PrimitiveType.IntType.getInstance();
      case FLOAT_TO_INT:
        setTag(new FloatOpTag());
        return PrimitiveType.IntType.getInstance();

      case INT_TO_LONG:
        setTag(new IntOpTag());
        return PrimitiveType.LongType.getInstance();
      case DOUBLE_TO_LONG:
        setTag(new DoubleOpTag());
        return PrimitiveType.LongType.getInstance();
      case FLOAT_TO_LONG:
        setTag(new FloatOpTag());
        return PrimitiveType.LongType.getInstance();

      case LONG_TO_FLOAT:
        setTag(new LongOpTag());
        return PrimitiveType.FloatType.getInstance();
      case DOUBLE_TO_FLOAT:
        setTag(new DoubleOpTag());
        return PrimitiveType.FloatType.getInstance();
      case INT_TO_FLOAT:
        setTag(new IntOpTag());
        return PrimitiveType.FloatType.getInstance();

      case INT_TO_DOUBLE:
        setTag(new IntOpTag());
        return PrimitiveType.DoubleType.getInstance();
      case FLOAT_TO_DOUBLE:
        setTag(new FloatOpTag());
        return PrimitiveType.DoubleType.getInstance();
      case LONG_TO_DOUBLE:
        setTag(new LongOpTag());
        return PrimitiveType.DoubleType.getInstance();

      default:
        throw new RuntimeException("Invalid Opcode: " + opcode);
    }
  }

  @Override
  boolean overridesRegister(int register) {
    TwoRegisterInstruction i = (TwoRegisterInstruction) instruction;
    int dest = i.getRegisterA();
    return register == dest;
  }

}
