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
import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.expr.AbstractConditionExpr;
import de.upb.swt.soot.core.jimple.common.stmt.JIfStmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexBody;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;

import java.util.Collections;

public abstract class ConditionalJumpInstruction extends JumpInstruction implements DeferableInstruction {

  public ConditionalJumpInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  /**
   * Return an if statement depending on the instruction.
   */
  protected abstract JIfStmt ifStatement(DexBody body);

  public void jimplify(DexBody body) {
    // check if target instruction has been jimplified
    DexlibAbstractInstruction ins = getTargetInstruction(body);
    if (ins != null && ins.getStmt() != null) {
      JIfStmt s = ifStatement(body);
      body.add(s);
      setStmt(s);
    } else {
      // set marker unit to swap real gotostmt with otherwise
      body.addDeferredJimplification(this);
      markerStmt = Jimple.newNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
      stmt = markerStmt;
      // beginUnit = markerUnit;
      // endUnit = markerUnit;
      // beginUnit = markerUnit;
      body.add(markerStmt);
      // Unit end = Jimple.v().newNopStmt();
      // body.add(end);
      // endUnit = end;
    }
  }

  // DalvikTyper.v() here?

  public void deferredJimplify(DexBody body) {
    JIfStmt s = ifStatement(body);
    Body.BodyBuilder bb = Body.builder(body.getBody(), Collections.emptySet());
    bb = bb.replaceStmt(markerStmt, s);
    body.setBody(bb.build());
    setStmt(s);
  }

  /**
   * Get comparison expression depending on opcode against zero or null.
   *
   * If the register is used as an object this will create a comparison with null, not zero.
   *
   * @param body
   *          the containing DexBody
   * @param reg
   *          the register to compare against zero.
   */
  protected AbstractConditionExpr getComparisonExpr(DexBody body, int reg) {
    Local one = body.getRegisterLocal(reg);
    return getComparisonExpr(one, IntConstant.getInstance(0));
  }

  /**
   * Get comparison expression depending on opcode between two immediates
   *
   * @param one
   *          first immediate
   * @param other
   *          second immediate
   * @throws RuntimeException
   *           if this is not a IfTest or IfTestz instruction.
   */
  protected AbstractConditionExpr getComparisonExpr(Immediate one, Immediate other) {
    Opcode opcode = instruction.getOpcode();

    switch (opcode) {
      case IF_EQ:
      case IF_EQZ:
        return Jimple.newEqExpr(one, other);
      case IF_NE:
      case IF_NEZ:
        return Jimple.newNeExpr(one, other);
      case IF_LT:
      case IF_LTZ:
        return Jimple.newLtExpr(one, other);
      case IF_GE:
      case IF_GEZ:
        return Jimple.newGeExpr(one, other);
      case IF_GT:
      case IF_GTZ:
        return Jimple.newGtExpr(one, other);
      case IF_LE:
      case IF_LEZ:
        return Jimple.newLeExpr(one, other);
      default:
        throw new RuntimeException("Instruction is not an IfTest(z) instruction.");
    }
  }
}
