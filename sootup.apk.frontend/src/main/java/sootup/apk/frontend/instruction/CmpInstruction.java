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
import org.jf.dexlib2.iface.instruction.formats.Instruction23x;
import sootup.apk.frontend.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.expr.Expr;
import sootup.core.jimple.common.stmt.JAssignStmt;

public class CmpInstruction extends DexLibAbstractInstruction {
  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction23x)) {
      throw new IllegalArgumentException(
          "Expected Instruction23x but got: " + instruction.getClass());
    }

    Instruction23x cmpInstr = (Instruction23x) instruction;
    int dest = cmpInstr.getRegisterA();

    Local first = body.getRegisterLocal(cmpInstr.getRegisterB());
    Local second = body.getRegisterLocal(cmpInstr.getRegisterC());

    Opcode opcode = instruction.getOpcode();
    Expr cmpExpr;
    //    Type type = null;
    switch (opcode) {
      case CMPL_DOUBLE:
        //        type = PrimitiveType.DoubleType.getInstance();
        cmpExpr = Jimple.newCmpExpr(first, second);
        break;
      case CMPL_FLOAT:
        //        type = PrimitiveType.FloatType.getInstance();
        cmpExpr = Jimple.newCmpExpr(first, second);
        break;
      case CMPG_DOUBLE:
        //        type = PrimitiveType.DoubleType.getInstance();
        cmpExpr = Jimple.newCmpgExpr(first, second);
        break;
      case CMPG_FLOAT:
        //        type = PrimitiveType.FloatType.getInstance();
        cmpExpr = Jimple.newCmpgExpr(first, second);
        break;
      case CMP_LONG:
        //        type = PrimitiveType.LongType.getInstance();
        cmpExpr = Jimple.newCmpExpr(first, second);
        break;
      default:
        throw new RuntimeException("no opcode for CMP: " + opcode);
    }

    JAssignStmt assign =
        Jimple.newAssignStmt(
            body.getRegisterLocal(dest), cmpExpr, StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(assign);
    body.add(assign);
  }

  public CmpInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
