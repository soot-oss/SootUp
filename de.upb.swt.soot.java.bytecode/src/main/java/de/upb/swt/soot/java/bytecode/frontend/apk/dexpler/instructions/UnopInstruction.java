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
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.constant.LongConstant;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexBody;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.IDalvikTyper;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.tags.DoubleOpTag;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.tags.FloatOpTag;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.tags.IntOpTag;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.tags.LongOpTag;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction12x;


public class UnopInstruction extends TaggedInstruction {

  public UnopInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction12x)) {
      throw new IllegalArgumentException("Expected Instruction12x but got: " + instruction.getClass());
    }

    Instruction12x cmpInstr = (Instruction12x) instruction;
    int dest = cmpInstr.getRegisterA();

    Local source = body.getRegisterLocal(cmpInstr.getRegisterB());
    Value expr = getExpression(source);

    JAssignStmt assign = Jimple.newAssignStmt(body.getRegisterLocal(dest), expr, StmtPositionInfo.createNoStmtPositionInfo());
    assign.addTag(getTag());

    setStmt(assign);
    addTags(assign);
    body.add(assign);

    if (IDalvikTyper.ENABLE_DVKTYPER) {
      /*
       * int op = (int)instruction.getOpcode().value; //DalvikTyper.v().captureAssign((JAssignStmt)assign, op); JAssignStmt
       * jass = (JAssignStmt)assign; DalvikTyper.v().setType((expr instanceof JCastExpr) ? ((JCastExpr) expr).getOpBox() :
       * ((UnopExpr) expr).getOpBox(), opUnType[op - 0x7b], true); DalvikTyper.v().setType(jass.leftBox, resUnType[op -
       * 0x7b], false);
       */
    }
  }

  /**
   * Return the appropriate Jimple Expression according to the OpCode
   */
  private Value getExpression(Local source) {
    Opcode opcode = instruction.getOpcode();
    switch (opcode) {
      case NEG_INT:
        setTag(new IntOpTag());
        return Jimple.newNegExpr(source);
      case NEG_LONG:
        setTag(new LongOpTag());
        return Jimple.newNegExpr(source);
      case NEG_FLOAT:
        setTag(new FloatOpTag());
        return Jimple.newNegExpr(source);
      case NEG_DOUBLE:
        setTag(new DoubleOpTag());
        return Jimple.newNegExpr(source);
      case NOT_LONG:
        setTag(new LongOpTag());
        return getNotLongExpr(source);
      case NOT_INT:
        setTag(new IntOpTag());
        return getNotIntExpr(source);
      default:
        throw new RuntimeException("Invalid Opcode: " + opcode);
    }

  }

  /**
   * returns bitwise negation of an integer
   *
   * @param source
   * @return
   */
  private Value getNotIntExpr(Local source) {
    return Jimple.newXorExpr(source, IntConstant.getInstance(-1));

  }

  /**
   * returns bitwise negation of a long
   *
   * @param source
   * @return
   */
  private Value getNotLongExpr(Local source) {
    return Jimple.newXorExpr(source, LongConstant.getInstance(-1l));

  }

  @Override
  boolean overridesRegister(int register) {
    TwoRegisterInstruction i = (TwoRegisterInstruction) instruction;
    int dest = i.getRegisterA();
    return register == dest;
  }

  @Override
  boolean isUsedAsFloatingPoint(DexBody body, int register) {
    int source = ((TwoRegisterInstruction) instruction).getRegisterB();
    Opcode opcode = instruction.getOpcode();
    switch (opcode) {
      case NEG_FLOAT:
      case NEG_DOUBLE:
        return source == register;
      default:
        return false;
    }
  }
}
