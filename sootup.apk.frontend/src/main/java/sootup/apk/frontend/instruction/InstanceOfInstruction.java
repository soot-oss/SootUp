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
import org.jf.dexlib2.iface.instruction.formats.Instruction22c;
import org.jf.dexlib2.iface.reference.TypeReference;
import sootup.apk.frontend.Util.DexUtil;
import sootup.apk.frontend.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.expr.JInstanceOfExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.types.Type;

public class InstanceOfInstruction extends DexLibAbstractInstruction {
  @Override
  public void jimplify(DexBody body) {
    Instruction22c i = (Instruction22c) instruction;
    int dest = i.getRegisterA();
    int source = i.getRegisterB();
    Type sootType = DexUtil.toSootType(((TypeReference) i.getReference()).getType(), 0);

    JInstanceOfExpr jInstanceOfExpr =
        Jimple.newInstanceOfExpr(body.getRegisterLocal(source), sootType);
    JAssignStmt jAssignStmt =
        Jimple.newAssignStmt(
            body.getRegisterLocal(dest), jInstanceOfExpr, StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(jAssignStmt);
    body.add(jAssignStmt);
  }

  public InstanceOfInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
