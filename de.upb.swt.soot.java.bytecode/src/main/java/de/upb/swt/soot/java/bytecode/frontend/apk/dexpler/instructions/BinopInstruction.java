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
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexBody;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.tags.DoubleOpTag;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.tags.FloatOpTag;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.tags.IntOpTag;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.tags.LongOpTag;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ThreeRegisterInstruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction23x;

public class BinopInstruction extends TaggedInstruction {

  public BinopInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction23x)) {
      throw new IllegalArgumentException("Expected Instruction23x but got: " + instruction.getClass());
    }

    Instruction23x binOpInstr = (Instruction23x) instruction;
    int dest = binOpInstr.getRegisterA();

    Local source1 = body.getRegisterLocal(binOpInstr.getRegisterB());
    Local source2 = body.getRegisterLocal(binOpInstr.getRegisterC());

    Value expr = getExpression(source1, source2);

    JAssignStmt assign = Jimple.newAssignStmt(body.getRegisterLocal(dest), expr, StmtPositionInfo.createNoStmtPositionInfo());
    assign.addTag(getTag());

    setStmt(assign);
    addTags(assign);
    body.add(assign);
  }

  private Value getExpression(Local source1, Local source2) {
    Opcode opcode = instruction.getOpcode();
    switch (opcode) {
      case ADD_LONG:
        setTag(new LongOpTag());
        return Jimple.newAddExpr(source1, source2);
      case ADD_FLOAT:
        setTag(new FloatOpTag());
        return Jimple.newAddExpr(source1, source2);
      case ADD_DOUBLE:
        setTag(new DoubleOpTag());
        return Jimple.newAddExpr(source1, source2);
      case ADD_INT:
        setTag(new IntOpTag());
        return Jimple.newAddExpr(source1, source2);

      case SUB_LONG:
        setTag(new LongOpTag());
        return Jimple.newSubExpr(source1, source2);
      case SUB_FLOAT:
        setTag(new FloatOpTag());
        return Jimple.newSubExpr(source1, source2);
      case SUB_DOUBLE:
        setTag(new DoubleOpTag());
        return Jimple.newSubExpr(source1, source2);
      case SUB_INT:
        setTag(new IntOpTag());
        return Jimple.newSubExpr(source1, source2);

      case MUL_LONG:
        setTag(new LongOpTag());
        return Jimple.newMulExpr(source1, source2);
      case MUL_FLOAT:
        setTag(new FloatOpTag());
        return Jimple.newMulExpr(source1, source2);
      case MUL_DOUBLE:
        setTag(new DoubleOpTag());
        return Jimple.newMulExpr(source1, source2);
      case MUL_INT:
        setTag(new IntOpTag());
        return Jimple.newMulExpr(source1, source2);

      case DIV_LONG:
        setTag(new LongOpTag());
        return Jimple.newDivExpr(source1, source2);
      case DIV_FLOAT:
        setTag(new FloatOpTag());
        return Jimple.newDivExpr(source1, source2);
      case DIV_DOUBLE:
        setTag(new DoubleOpTag());
        return Jimple.newDivExpr(source1, source2);
      case DIV_INT:
        setTag(new IntOpTag());
        return Jimple.newDivExpr(source1, source2);

      case REM_LONG:
        setTag(new LongOpTag());
        return Jimple.newRemExpr(source1, source2);
      case REM_FLOAT:
        setTag(new FloatOpTag());
        return Jimple.newRemExpr(source1, source2);
      case REM_DOUBLE:
        setTag(new DoubleOpTag());
        return Jimple.newRemExpr(source1, source2);
      case REM_INT:
        setTag(new IntOpTag());
        return Jimple.newRemExpr(source1, source2);

      case AND_LONG:
        setTag(new LongOpTag());
        return Jimple.newAndExpr(source1, source2);
      case AND_INT:
        setTag(new IntOpTag());
        return Jimple.newAndExpr(source1, source2);

      case OR_LONG:
        setTag(new LongOpTag());
        return Jimple.newOrExpr(source1, source2);
      case OR_INT:
        setTag(new IntOpTag());
        return Jimple.newOrExpr(source1, source2);

      case XOR_LONG:
        setTag(new LongOpTag());
        return Jimple.newXorExpr(source1, source2);
      case XOR_INT:
        setTag(new IntOpTag());
        return Jimple.newXorExpr(source1, source2);

      case SHL_LONG:
        setTag(new LongOpTag());
        return Jimple.newShlExpr(source1, source2);
      case SHL_INT:
        setTag(new IntOpTag());
        return Jimple.newShlExpr(source1, source2);

      case SHR_LONG:
        setTag(new LongOpTag());
        return Jimple.newShrExpr(source1, source2);
      case SHR_INT:
        setTag(new IntOpTag());
        return Jimple.newShrExpr(source1, source2);

      case USHR_LONG:
        setTag(new LongOpTag());
        return Jimple.newUshrExpr(source1, source2);
      case USHR_INT:
        setTag(new IntOpTag());
        return Jimple.newUshrExpr(source1, source2);

      default:
        throw new RuntimeException("Invalid Opcode: " + opcode);
    }
  }

  @Override
  boolean overridesRegister(int register) {
    ThreeRegisterInstruction i = (ThreeRegisterInstruction) instruction;
    int dest = i.getRegisterA();
    return register == dest;
  }

}
