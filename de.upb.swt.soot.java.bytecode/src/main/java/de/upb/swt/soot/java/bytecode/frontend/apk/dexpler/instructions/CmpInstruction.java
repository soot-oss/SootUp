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
import de.upb.swt.soot.core.jimple.common.expr.AbstractBinopExpr;
import de.upb.swt.soot.core.jimple.common.expr.Expr;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexBody;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.IDalvikTyper;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.tags.DoubleOpTag;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.tags.FloatOpTag;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.tags.LongOpTag;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.typing.DalvikTyper;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ThreeRegisterInstruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction23x;

public class CmpInstruction extends TaggedInstruction {

  public CmpInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction23x)) {
      throw new IllegalArgumentException("Expected Instruction23x but got: " + instruction.getClass());
    }

    Instruction23x cmpInstr = (Instruction23x) instruction;
    int dest = cmpInstr.getRegisterA();

    Local first = body.getRegisterLocal(cmpInstr.getRegisterB());
    Local second = body.getRegisterLocal(cmpInstr.getRegisterC());

    // Expr cmpExpr;
    // Type type = null
    Opcode opcode = instruction.getOpcode();
    Expr cmpExpr = null;
    Type type = null;
    switch (opcode) {
      case CMPL_DOUBLE:
        setTag(new DoubleOpTag());
        type = PrimitiveType.DoubleType.getInstance();
        cmpExpr = Jimple.newCmplExpr(first, second);
        break;
      case CMPL_FLOAT:
        setTag(new FloatOpTag());
        type = PrimitiveType.FloatType.getInstance();
        cmpExpr = Jimple.newCmplExpr(first, second);
        break;
      case CMPG_DOUBLE:
        setTag(new DoubleOpTag());
        type = PrimitiveType.DoubleType.getInstance();
        cmpExpr = Jimple.newCmpgExpr(first, second);
        break;
      case CMPG_FLOAT:
        setTag(new FloatOpTag());
        type = PrimitiveType.FloatType.getInstance();
        cmpExpr = Jimple.newCmpgExpr(first, second);
        break;
      case CMP_LONG:
        setTag(new LongOpTag());
        type = PrimitiveType.LongType.getInstance();
        cmpExpr = Jimple.newCmpExpr(first, second);
        break;
      default:
        throw new RuntimeException("no opcode for CMP: " + opcode);
    }

    JAssignStmt assign = Jimple.newAssignStmt(body.getRegisterLocal(dest), cmpExpr, StmtPositionInfo.createNoStmtPositionInfo());
    assign.addTag(getTag());

    setStmt(assign);
    addTags(assign);
    body.add(assign);

    if (IDalvikTyper.ENABLE_DVKTYPER) {
      getTag().getName();
      AbstractBinopExpr bexpr = (AbstractBinopExpr) cmpExpr;
      DalvikTyper.v().setType(bexpr.getOp1Box(), type, true);
      DalvikTyper.v().setType(bexpr.getOp2Box(), type, true);
      DalvikTyper.v().setType(((JAssignStmt) assign).getLeftBox(), PrimitiveType.IntType.getInstance(), false);
    }
  }

  @Override
  boolean overridesRegister(int register) {
    ThreeRegisterInstruction i = (ThreeRegisterInstruction) instruction;
    int dest = i.getRegisterA();
    return register == dest;
  }

}
