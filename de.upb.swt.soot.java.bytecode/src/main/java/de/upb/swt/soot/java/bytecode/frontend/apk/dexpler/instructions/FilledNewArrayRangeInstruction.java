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
import org.jf.dexlib2.iface.instruction.formats.Instruction3rc;
import org.jf.dexlib2.iface.reference.TypeReference;


public class FilledNewArrayRangeInstruction extends FilledArrayInstruction {

  public FilledNewArrayRangeInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction3rc)) {
      throw new IllegalArgumentException("Expected Instruction3rc but got: " + instruction.getClass());
    }

    Instruction3rc filledNewArrayInstr = (Instruction3rc) instruction;

    // NopStmt nopStmtBeginning = Jimple.v().newNopStmt();
    // body.add(nopStmtBeginning);

    int usedRegister = filledNewArrayInstr.getRegisterCount();
    Type t = DexType.toSoot((TypeReference) filledNewArrayInstr.getReference());
    // NewArrayExpr needs the ElementType as it increases the array dimension by 1
    Type arrayType = ((ArrayType) t).getBaseType();
    JNewArrayExpr arrayExpr = JavaJimple.getInstance().newNewArrayExpr(arrayType, IntConstant.getInstance(usedRegister));
    Local arrayLocal = body.getStoreResultLocal();
    JAssignStmt assignStmt = Jimple.newAssignStmt(arrayLocal, arrayExpr, StmtPositionInfo.createNoStmtPositionInfo());
    body.add(assignStmt);

    for (int i = 0; i < usedRegister; i++) {
      JArrayRef arrayRef = JavaJimple.getInstance().newArrayRef(arrayLocal, IntConstant.getInstance(i));

      JAssignStmt assign
          = Jimple.newAssignStmt(arrayRef, body.getRegisterLocal(i + filledNewArrayInstr.getStartRegister()), StmtPositionInfo.createNoStmtPositionInfo());
      addTags(assign);
      body.add(assign);
    }
    // NopStmt nopStmtEnd = Jimple.v().newNopStmt();
    // body.add(nopStmtEnd);

    // defineBlock(nopStmtBeginning,nopStmtEnd);
    setStmt(assignStmt);

    // body.setDanglingInstruction(this);

    if (IDalvikTyper.ENABLE_DVKTYPER) {
      // Debug.printDbg(IDalvikTyper.DEBUG, "constraint: "+ assignStmt);
      DalvikTyper.v().setType(assignStmt.getLeftOp(), arrayExpr.getType(), false);
      // DalvikTyper.v().addConstraint(assignStmt.getLeftOp(), assignStmt.getRightOp());
    }

  }

  @Override
  boolean isUsedAsFloatingPoint(DexBody body, int register) {
    Instruction3rc i = (Instruction3rc) instruction;
    Type arrayType = DexType.toSoot((TypeReference) i.getReference());
    int startRegister = i.getStartRegister();
    int endRegister = startRegister + i.getRegisterCount();

    return register >= startRegister && register <= endRegister && Util.isFloatLike(arrayType);
  }

}
