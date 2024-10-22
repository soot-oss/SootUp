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
import org.jf.dexlib2.iface.instruction.formats.Instruction23x;
import sootup.apk.frontend.main.DexBody;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.java.core.language.JavaJimple;

public class AputInstruction extends FieldInstruction {
  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction23x)) {
      throw new IllegalArgumentException(
          "Expected Instruction23x but got: " + instruction.getClass());
    }

    Instruction23x aPutInstr = (Instruction23x) instruction;
    int source = aPutInstr.getRegisterA();

    Local arrayBase = body.getRegisterLocal(aPutInstr.getRegisterB());
    Local index = body.getRegisterLocal(aPutInstr.getRegisterC());
    JArrayRef jArrayRef = JavaJimple.getInstance().newArrayRef(arrayBase, index);

    Local sourceValue = body.getRegisterLocal(source);
    JAssignStmt assign = getAssignStmt(sourceValue, jArrayRef);
    setStmt(assign);
    body.add(assign);
  }

  public AputInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
