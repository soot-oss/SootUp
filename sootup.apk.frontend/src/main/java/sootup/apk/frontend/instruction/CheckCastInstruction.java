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

import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21c;
import org.jf.dexlib2.iface.reference.TypeReference;
import sootup.apk.frontend.Util.DexUtil;
import sootup.apk.frontend.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.types.Type;

public class CheckCastInstruction extends DexLibAbstractInstruction {
  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction21c)) {
      throw new IllegalArgumentException(
          "Expected Instruction21c but got: " + instruction.getClass());
    }

    Instruction21c checkCastInstr = (Instruction21c) instruction;

    Local castValue = body.getRegisterLocal(checkCastInstr.getRegisterA());
    Type checkCastType =
        DexUtil.toSootType(((TypeReference) checkCastInstr.getReference()).getType(), 0);

    JCastExpr castExpr = Jimple.newCastExpr(castValue, checkCastType);
    // generate "x = (Type) x"
    // splitter will take care of the rest
    JAssignStmt jAssignStmt =
        Jimple.newAssignStmt(castValue, castExpr, StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(jAssignStmt);
    body.add(jAssignStmt);
  }

  public CheckCastInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
