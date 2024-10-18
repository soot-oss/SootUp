package sootup.apk.frontend.instruction;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
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

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import sootup.apk.frontend.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.AbstractConditionExpr;
import sootup.core.jimple.common.stmt.*;

public abstract class ConditionalJumpInstruction extends JumpInstruction
    implements DeferableInstruction {
  /**
   * @param instruction the underlying dexlib instruction
   * @param codeAddress the bytecode address of this instruction
   */
  public ConditionalJumpInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  protected abstract JIfStmt ifStatement(DexBody dexBody);

  @Override
  public void jimplify(DexBody body) {
    // check if target instruction has been jimplified
    DexLibAbstractInstruction ins = getTargetInstruction(body);
    if (ins != null && ins.getStmt() != null) {
      JIfStmt s = ifStatement(body);
      body.add(s);
      setStmt(s);
    } else {
      // set marker unit to swap real gotostmt with otherwise
      body.addDeferredJimplification(this);
      markerUnit = Jimple.newNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
      setStmt(markerUnit);
      body.add(stmt);
    }
  }

  protected AbstractConditionExpr getComparisonExpr(DexBody body, int reg) {
    Local one = body.getRegisterLocal(reg);
    return getComparisonExpr(one, IntConstant.getInstance(0));
  }

  /**
   * Get comparison expression depending on opcode between two immediates
   *
   * @param one first immediate
   * @param other second immediate
   * @throws RuntimeException if this is not a IfTest or IfTestz instruction.
   * @return the conditional expression statement based on the opcode fromm the given instruction
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

  @Override
  public void deferredJimplify(DexBody body) {
    JIfStmt jIfStmt = ifStatement(body);
    body.replaceStmt(markerUnit, jIfStmt);
    setStmt(jIfStmt);
  }
}
