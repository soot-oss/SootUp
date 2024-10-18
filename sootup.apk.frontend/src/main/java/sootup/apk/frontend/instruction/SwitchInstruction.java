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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jf.dexlib2.dexbacked.instruction.DexBackedPackedSwitchPayload;
import org.jf.dexlib2.dexbacked.instruction.DexBackedSparseSwitchPayload;
import org.jf.dexlib2.iface.instruction.*;
import org.jf.dexlib2.iface.instruction.formats.PackedSwitchPayload;
import org.jf.dexlib2.iface.instruction.formats.SparseSwitchPayload;
import sootup.apk.frontend.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.Stmt;

public abstract class SwitchInstruction extends DexLibAbstractInstruction
    implements DeferableInstruction {

  protected Stmt markerUnit;

  Stmt defaultTarget;

  List<IntConstant> lookupValues = new ArrayList<>();

  List<Stmt> targets = new ArrayList<>();

  Instruction targetData;

  Stmt switchStmt;

  SwitchPayload switchPayload;

  /**
   * @param instruction the underlying dexlib instruction
   * @param codeAddress the bytecode address of this instruction
   */
  public SwitchInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  protected abstract Stmt switchStatement(DexBody body, Instruction targetData, Local key);

  @Override
  public void jimplify(DexBody body) {
    markerUnit = Jimple.newNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(markerUnit);
    body.add(markerUnit);
    body.addDeferredJimplification(this);
  }

  public void computeLookUpValues(Instruction targetData) {
    if (targetData instanceof DexBackedSparseSwitchPayload) {
      switchPayload = (SparseSwitchPayload) targetData;
    } else if (targetData instanceof DexBackedPackedSwitchPayload) {
      switchPayload = (PackedSwitchPayload) targetData;
    }
    assert switchPayload != null;
    List<? extends SwitchElement> seList = switchPayload.getSwitchElements();
    for (SwitchElement se : seList) {
      lookupValues.add(IntConstant.getInstance(se.getKey()));
    }
  }

  public void computeBranchingStmts(DexBody body) {
    List<? extends SwitchElement> seList = switchPayload.getSwitchElements();

    // the default target always follows the switch statement
    int defaultTargetAddress = codeAddress + instruction.getCodeUnits();
    DexLibAbstractInstruction defaultTargetInstruction =
        body.instructionAtAddress(defaultTargetAddress);
    defaultTarget = defaultTargetInstruction.getStmt();

    for (SwitchElement se : seList) {
      int offset = se.getOffset();
      DexLibAbstractInstruction instructionAtAddress =
          body.instructionAtAddress(codeAddress + offset);
      targets.add(instructionAtAddress.stmt);
    }
    targets.add(defaultTarget);
  }

  public void addBranchingStmts(DexBody body) {
    computeBranchingStmts(body);
    if (targetData instanceof DexBackedPackedSwitchPayload) {
      body.addBranchingStmt((BranchingStmt) switchStmt, targets);
      body.addBranchingStmt((BranchingStmt) switchStmt, Collections.singletonList(defaultTarget));
    } else if (targetData instanceof DexBackedSparseSwitchPayload) {
      body.addBranchingStmt((BranchingStmt) switchStmt, targets);
    }
  }

  @Override
  public void deferredJimplify(DexBody body) {
    int keyRegister = ((OneRegisterInstruction) instruction).getRegisterA();
    int offset = ((OffsetInstruction) instruction).getCodeOffset();
    Local key = body.getRegisterLocal(keyRegister);
    int targetAddress = codeAddress + offset;
    targetData = body.instructionAtAddress(targetAddress).instruction;
    computeLookUpValues(targetData);
    switchStmt = switchStatement(body, targetData, key);
    body.replaceStmt(markerUnit, switchStmt);
    setStmt(switchStmt);
  }
}
