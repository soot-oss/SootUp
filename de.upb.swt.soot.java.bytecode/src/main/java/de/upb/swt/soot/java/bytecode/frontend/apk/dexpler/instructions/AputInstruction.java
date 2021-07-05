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

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.UnknownType;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexBody;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.IDalvikTyper;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.tags.ObjectOpTag;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.typing.DalvikTyper;
import de.upb.swt.soot.java.core.language.JavaJimple;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction23x;


public class AputInstruction extends FieldInstruction {

  public AputInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction23x)) {
      throw new IllegalArgumentException("Expected Instruction23x but got: " + instruction.getClass());
    }

    Instruction23x aPutInstr = (Instruction23x) instruction;
    int source = aPutInstr.getRegisterA();

    Local arrayBase = body.getRegisterLocal(aPutInstr.getRegisterB());
    Local index = body.getRegisterLocal(aPutInstr.getRegisterC());
    JArrayRef arrayRef = JavaJimple.getInstance().newArrayRef(arrayBase, index);

    Local sourceValue = body.getRegisterLocal(source);
    JAssignStmt assign = getAssignStmt(body, sourceValue, arrayRef);
    if (aPutInstr.getOpcode() == Opcode.APUT_OBJECT) {
      assign.addTag(new ObjectOpTag());
    }

    setStmt(assign);
    addTags(assign);
    body.add(assign);

    if (IDalvikTyper.ENABLE_DVKTYPER) {
      DalvikTyper.v().addConstraint(assign.getLeftOpBox(), assign.getRightOpBox());
      DalvikTyper.v().setType(arrayRef.getIndexBox(), PrimitiveType.IntType.getInstance(), true);
    }
  }

  @Override
  protected Type getTargetType(DexBody body) {
    Instruction23x aPutInstr = (Instruction23x) instruction;
    Type t = body.getRegisterLocal(aPutInstr.getRegisterB()).getType();
    if (t instanceof ArrayType) {
      return ((ArrayType) t).getBaseType();
    } else {
      return UnknownType.getInstance();
    }
  }
}
