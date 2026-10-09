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
import sootup.core.jimple.basic.SimpleStmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.JIdentityStmt;

public class MoveExceptionInstruction extends DexLibAbstractInstruction {

  @Override
  public void jimplify(DexBody body) {
    int dest = ((OneRegisterInstruction) instruction).getRegisterA();
    Local l = body.getRegisterLocal(dest);
    // the exception type of the try block(s) reaching this handler, precomputed from the try
    // blocks before any instruction is jimplified, so the ref carries the real type from the start
    JCaughtExceptionRef ref = new JCaughtExceptionRef(body.exceptionTypeAt(getCodeAddress()));
    JIdentityStmt stmt = Jimple.newIdentityStmt(l, ref, new SimpleStmtPositionInfo(lineNumber));
    setStmt(stmt);
    body.add(stmt);
  }

  public MoveExceptionInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
