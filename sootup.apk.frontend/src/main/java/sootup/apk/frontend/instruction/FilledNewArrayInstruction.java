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

import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction35c;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.reference.TypeReference;
import sootup.apk.frontend.Util.DexUtil;
import sootup.apk.frontend.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.SimpleStmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.types.ArrayType;
import sootup.core.types.Type;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;

public class FilledNewArrayInstruction extends FilledArrayInstruction {
  @Override
  public void jimplify(DexBody body) {
    DexBackedInstruction35c filledNewArrayInstr = (DexBackedInstruction35c) instruction;
    int[] regs = {
      filledNewArrayInstr.getRegisterC(),
      filledNewArrayInstr.getRegisterD(),
      filledNewArrayInstr.getRegisterE(),
      filledNewArrayInstr.getRegisterF(),
      filledNewArrayInstr.getRegisterG(),
    };
    int usedRegister = filledNewArrayInstr.getRegisterCount();

    Type t = DexUtil.toSootType(((TypeReference) filledNewArrayInstr.getReference()).getType(), 0);
    // NewArrayExpr needs the ElementType as it increases the array dimension by 1
    Type arrayType = ((ArrayType) t).getElementType();
    JNewArrayExpr arrayExpr =
        JavaJimple.newNewArrayExpr(
            arrayType, IntConstant.getInstance(usedRegister), JavaIdentifierFactory.getInstance());
    // new local generated intentional, will be moved to real register by MoveResult
    Local arrayLocal = body.getStoreResultLocal();
    JAssignStmt assign =
        Jimple.newAssignStmt(arrayLocal, arrayExpr, new SimpleStmtPositionInfo(lineNumber));
    body.add(assign);
    for (int i = 0; i < usedRegister; i++) {
      JArrayRef arrayRef = JavaJimple.newArrayRef(arrayLocal, IntConstant.getInstance(i));
      JAssignStmt assign2 =
          Jimple.newAssignStmt(
              arrayRef, body.getRegisterLocal(regs[i]), new SimpleStmtPositionInfo(lineNumber));
      body.add(assign2);
    }
    setStmt(assign);
  }

  public FilledNewArrayInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  @Override
  public void finalize(DexBody body, DexLibAbstractInstruction successor) {}
}
