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
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction;
import org.jf.dexlib2.iface.reference.FieldReference;
import sootup.apk.frontend.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;

public class IgetInstruction extends FieldInstruction {
  @Override
  public void jimplify(DexBody body) {
    TwoRegisterInstruction i = (TwoRegisterInstruction) instruction;
    int dest = i.getRegisterA();
    int object = i.getRegisterB();
    FieldReference f = (FieldReference) ((ReferenceInstruction) instruction).getReference();
    JInstanceFieldRef jInstanceFieldRef =
        Jimple.newInstanceFieldRef(
            body.getRegisterLocal(object), getSootFieldRef(f).getFieldSignature());
    JAssignStmt assignStmt = getAssignStmt(body.getRegisterLocal(dest), jInstanceFieldRef);
    setStmt(assignStmt);
    body.add(assignStmt);
  }

  public IgetInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
