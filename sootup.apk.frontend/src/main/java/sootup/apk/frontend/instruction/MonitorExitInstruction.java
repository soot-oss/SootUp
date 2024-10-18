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
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;
import sootup.apk.frontend.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.javabytecode.stmt.JExitMonitorStmt;

public class MonitorExitInstruction extends DexLibAbstractInstruction {
  @Override
  public void jimplify(DexBody body) {
    int reg = ((OneRegisterInstruction) instruction).getRegisterA();
    Local object = body.getRegisterLocal(reg);
    JExitMonitorStmt exitMonitorStmt =
        Jimple.newExitMonitorStmt(object, StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(exitMonitorStmt);
    body.add(exitMonitorStmt);
  }

  public MonitorExitInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
