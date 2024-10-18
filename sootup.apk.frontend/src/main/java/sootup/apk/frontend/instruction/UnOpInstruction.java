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
import org.jf.dexlib2.iface.instruction.formats.Instruction12x;
import sootup.apk.frontend.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.jimple.common.stmt.JAssignStmt;

public class UnOpInstruction extends DexLibAbstractInstruction {
  public UnOpInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction12x)) {
      throw new IllegalArgumentException(
          "Expected Instruction12x but got: " + instruction.getClass());
    }

    Instruction12x cmpInstr = (Instruction12x) instruction;
    int dest = cmpInstr.getRegisterA();

    Local source = body.getRegisterLocal(cmpInstr.getRegisterB());
    Value expr = getExpression(source);
    JAssignStmt assign =
        Jimple.newAssignStmt(
            body.getRegisterLocal(dest), expr, StmtPositionInfo.getNoStmtPositionInfo());

    setStmt(assign);
    body.add(assign);
  }

  /** Return the appropriate Jimple Expression according to the OpCode */
  private Value getExpression(Local source) {
    Opcode opcode = instruction.getOpcode();
    switch (opcode) {
      case NEG_INT:
      case NEG_DOUBLE:
      case NEG_FLOAT:
      case NEG_LONG:
        return Jimple.newNegExpr(source);
      case NOT_LONG:
        return getNotLongExpr(source);
      case NOT_INT:
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
}
