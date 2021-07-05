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
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.expr.JNewArrayExpr;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexBody;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexType;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.IDalvikTyper;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.Util;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.typing.DalvikTyper;
import de.upb.swt.soot.java.core.language.JavaJimple;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction35c;
import org.jf.dexlib2.iface.reference.TypeReference;


public class FilledNewArrayInstruction extends FilledArrayInstruction {

  public FilledNewArrayInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction35c)) {
      throw new IllegalArgumentException("Expected Instruction35c but got: " + instruction.getClass());
    }

    Instruction35c filledNewArrayInstr = (Instruction35c) instruction;
    int[] regs = { filledNewArrayInstr.getRegisterC(), filledNewArrayInstr.getRegisterD(),
        filledNewArrayInstr.getRegisterE(), filledNewArrayInstr.getRegisterF(), filledNewArrayInstr.getRegisterG(), };
    // NopStmt nopStmtBeginning = Jimple.v().newNopStmt();
    // body.add(nopStmtBeginning);

    int usedRegister = filledNewArrayInstr.getRegisterCount();

    Type t = DexType.toSoot((TypeReference) filledNewArrayInstr.getReference());
    // NewArrayExpr needs the ElementType as it increases the array dimension by 1
    Type arrayType = ((ArrayType) t).getBaseType();
    JNewArrayExpr arrayExpr = JavaJimple.getInstance().newNewArrayExpr(arrayType, IntConstant.getInstance(usedRegister));
    // new local generated intentional, will be moved to real register by MoveResult
    Local arrayLocal = body.getStoreResultLocal();
    JAssignStmt assign = Jimple.newAssignStmt(arrayLocal, arrayExpr, StmtPositionInfo.createNoStmtPositionInfo());
    body.add(assign);
    for (int i = 0; i < usedRegister; i++) {
      JArrayRef arrayRef = JavaJimple.getInstance().newArrayRef(arrayLocal, IntConstant.getInstance(i));

      JAssignStmt assign2 = Jimple.newAssignStmt(arrayRef, body.getRegisterLocal(regs[i]), StmtPositionInfo.createNoStmtPositionInfo());
      addTags(assign2);
      body.add(assign2);
    }
    // NopStmt nopStmtEnd = Jimple.v().newNopStmt();
    // body.add(nopStmtEnd);
    // defineBlock(nopStmtBeginning, nopStmtEnd);
    setStmt(assign);

    // body.setDanglingInstruction(this);

    if (IDalvikTyper.ENABLE_DVKTYPER) {
      // Debug.printDbg(IDalvikTyper.DEBUG, "constraint: "+ assign);
      DalvikTyper.v().setType(assign.getLeftOpBox(), arrayExpr.getType(), false);
      // DalvikTyper.v().setType(array, arrayType, isUse)
      // DalvikTyper.v().addConstraint(assign.getLeftOpBox(), assign.getRightOpBox());
    }

  }

  @Override
  boolean isUsedAsFloatingPoint(DexBody body, int register) {
    Instruction35c i = (Instruction35c) instruction;
    Type arrayType = DexType.toSoot((TypeReference) i.getReference());
    return isRegisterUsed(register) && Util.isFloatLike(arrayType);
  }

  /**
   * Check if register is referenced by this instruction.
   *
   */
  private boolean isRegisterUsed(int register) {
    Instruction35c i = (Instruction35c) instruction;
    return register == i.getRegisterD() || register == i.getRegisterE() || register == i.getRegisterF()
        || register == i.getRegisterG() || register == i.getRegisterC();
  }

}
