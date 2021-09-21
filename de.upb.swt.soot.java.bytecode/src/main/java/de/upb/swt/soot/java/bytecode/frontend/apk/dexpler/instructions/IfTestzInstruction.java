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
import de.upb.swt.soot.core.jimple.common.expr.AbstractBinopExpr;
import de.upb.swt.soot.core.jimple.common.expr.AbstractConditionExpr;
import de.upb.swt.soot.core.jimple.common.stmt.JIfStmt;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexBody;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.IDalvikTyper;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21t;


public class IfTestzInstruction extends ConditionalJumpInstruction {

  public IfTestzInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  @Override
  protected JIfStmt ifStatement(DexBody body) {
    Instruction21t i = (Instruction21t) instruction;
    AbstractConditionExpr condition = getComparisonExpr(body, i.getRegisterA());
    JIfStmt jif = Jimple.newIfStmt(condition, targetInstruction.getStmt().getPositionInfo());
    // setUnit() is called in ConditionalJumpInstruction

    addTags(jif);
    if (IDalvikTyper.ENABLE_DVKTYPER) {
      // Debug.printDbg(IDalvikTyper.DEBUG, "constraint: "+ jif);
      /*
       * int op = instruction.getOpcode().value; switch (op) { case 0x38: case 0x39:
       * //DalvikTyper.v().addConstraint(condition.getOp1Box(), condition.getOp2Box()); break; case 0x3a: case 0x3b: case
       * 0x3c: case 0x3d: DalvikTyper.v().setType(condition.getOp1Box(), BooleanType.v(), true); break; default: throw new
       * RuntimeException("error: unknown op: 0x"+ Integer.toHexString(op)); }
       */
    }

    return jif;

  }
}
