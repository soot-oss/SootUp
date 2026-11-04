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
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction;
import sootup.apk.frontend.main.DexBody;
import sootup.apk.frontend.main.DexCastExpr;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.SimpleStmtPositionInfo;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;

public class CastInstruction extends DexLibAbstractInstruction {
  @Override
  public void jimplify(DexBody body) {
    TwoRegisterInstruction i = (TwoRegisterInstruction) instruction;
    int dest = i.getRegisterA();
    int source = i.getRegisterB();
    Type targetType = getTargetType();
    JCastExpr jCastExpr =
        new DexCastExpr(body.getRegisterLocal(source), targetType, getSourceType());
    JAssignStmt jAssignStmt =
        Jimple.newAssignStmt(
            body.getRegisterLocal(dest), jCastExpr, new SimpleStmtPositionInfo(lineNumber));
    setStmt(jAssignStmt);
    body.add(jAssignStmt);
  }

  /** The type the conversion converts from, which only the opcode tells */
  private Type getSourceType() {
    switch (instruction.getOpcode()) {
      case INT_TO_BYTE:
      case INT_TO_CHAR:
      case INT_TO_SHORT:
      case INT_TO_LONG:
      case INT_TO_FLOAT:
      case INT_TO_DOUBLE:
        return PrimitiveType.IntType.getInstance();
      case LONG_TO_INT:
      case LONG_TO_FLOAT:
      case LONG_TO_DOUBLE:
        return PrimitiveType.LongType.getInstance();
      case FLOAT_TO_INT:
      case FLOAT_TO_LONG:
      case FLOAT_TO_DOUBLE:
        return PrimitiveType.FloatType.getInstance();
      case DOUBLE_TO_INT:
      case DOUBLE_TO_LONG:
      case DOUBLE_TO_FLOAT:
        return PrimitiveType.DoubleType.getInstance();
      default:
        throw new IllegalStateException("Invalid Opcode: " + instruction.getOpcode());
    }
  }

  private Type getTargetType() {
    Opcode opcode = instruction.getOpcode();
    switch (opcode) {
      case INT_TO_BYTE:
        return PrimitiveType.ByteType.getInstance();
      case INT_TO_CHAR:
        return PrimitiveType.CharType.getInstance();
      case INT_TO_SHORT:
        return PrimitiveType.ShortType.getInstance();

      case LONG_TO_INT:
      case FLOAT_TO_INT:
      case DOUBLE_TO_INT:
        return PrimitiveType.IntType.getInstance();

      case INT_TO_LONG:
      case FLOAT_TO_LONG:
      case DOUBLE_TO_LONG:
        return PrimitiveType.LongType.getInstance();

      case LONG_TO_FLOAT:
      case INT_TO_FLOAT:
      case DOUBLE_TO_FLOAT:
        return PrimitiveType.FloatType.getInstance();

      case INT_TO_DOUBLE:
      case LONG_TO_DOUBLE:
      case FLOAT_TO_DOUBLE:
        return PrimitiveType.DoubleType.getInstance();

      default:
        throw new IllegalStateException("Invalid Opcode: " + opcode);
    }
  }

  public CastInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
