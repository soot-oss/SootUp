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
import org.jf.dexlib2.iface.instruction.formats.Instruction3rc;
import org.jf.dexlib2.iface.reference.TypeReference;
import sootup.apk.frontend.Util.DexUtil;
import sootup.apk.frontend.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.types.ArrayType;
import sootup.core.types.Type;
import sootup.java.core.language.JavaJimple;

public class FilledNewArrayRangeInstruction extends FilledArrayInstruction {
  @Override
  public void jimplify(DexBody body) {

    Instruction3rc filledNewArrayInstr = (Instruction3rc) instruction;

    int usedRegister = filledNewArrayInstr.getRegisterCount();
    Type t = DexUtil.toSootType(((TypeReference) filledNewArrayInstr.getReference()).getType(), 0);
    // NewArrayExpr needs the ElementType as it increases the array dimension by 1
    Type arrayType = ((ArrayType) t).getElementType();
    JNewArrayExpr arrayExpr =
        JavaJimple.getInstance().newNewArrayExpr(arrayType, IntConstant.getInstance(usedRegister));
    Local arrayLocal = body.getStoreResultLocal();
    JAssignStmt assignStmt =
        Jimple.newAssignStmt(arrayLocal, arrayExpr, StmtPositionInfo.getNoStmtPositionInfo());
    body.add(assignStmt);

    for (int i = 0; i < usedRegister; i++) {
      JArrayRef arrayRef =
          JavaJimple.getInstance().newArrayRef(arrayLocal, IntConstant.getInstance(i));

      JAssignStmt assign =
          Jimple.newAssignStmt(
              arrayRef,
              body.getRegisterLocal(i + filledNewArrayInstr.getStartRegister()),
              StmtPositionInfo.getNoStmtPositionInfo());
      body.add(assign);
    }
    setStmt(assignStmt);
  }

  public FilledNewArrayRangeInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  @Override
  public void finalize(DexBody body, DexLibAbstractInstruction successor) {}
}
