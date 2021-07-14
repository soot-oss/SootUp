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

import java.util.ArrayList;
import java.util.List;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JSwitchStmt;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexBody;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.IDalvikTyper;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.typing.DalvikTyper;
import de.upb.swt.soot.java.core.language.JavaJimple;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.SwitchElement;
import org.jf.dexlib2.iface.instruction.formats.PackedSwitchPayload;



public class PackedSwitchInstruction extends SwitchInstruction {

  public PackedSwitchInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  @Override
  protected Stmt switchStatement(DexBody body, Instruction targetData, Local key) {
    PackedSwitchPayload i = (PackedSwitchPayload) targetData;
    List<? extends SwitchElement> seList = i.getSwitchElements();

    // the default target always follows the switch statement
    int defaultTargetAddress = codeAddress + instruction.getCodeUnits();
    Stmt defaultTarget = body.instructionAtAddress(defaultTargetAddress).getStmt();

    List<IntConstant> lookupValues = new ArrayList<IntConstant>();
    List<Stmt> targets = new ArrayList<>();
    for (SwitchElement se : seList) {
      lookupValues.add(IntConstant.getInstance(se.getKey()));
      int offset = se.getOffset();
      targets.add(body.instructionAtAddress(codeAddress + offset).getStmt());
    }
    // TODO:
    JSwitchStmt switchStmt = Jimple.newLookupSwitchStmt(key, lookupValues, targets, defaultTarget);
    setStmt(switchStmt);

    if (IDalvikTyper.ENABLE_DVKTYPER) {
      DalvikTyper.v().setType(switchStmt.getKey(), PrimitiveType.IntType.getInstance(), true);
    }

    return switchStmt;
  }

  @Override
  public void computeDataOffsets(DexBody body) {
  }

}
