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
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.common.stmt.JGotoStmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;

import java.util.Collections;


public class GotoInstruction extends JumpInstruction implements DeferableInstruction {
  public GotoInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  public void jimplify(DexBody body) {
    // check if target instruction has been jimplified
    if (getTargetInstruction(body).getStmt() != null) {
      body.add(gotoStatement());
      return;
    }
    // set marker unit to swap real gotostmt with otherwise
    body.addDeferredJimplification(this);
    markerStmt = Jimple.newNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    addTags(markerStmt);
    stmt = markerStmt;
    body.add(markerStmt);
  }

  public void deferredJimplify(DexBody body) {
    Body.BodyBuilder bb = Body.builder(body.getBody(), Collections.emptySet());
    bb = bb.addFlow(markerStmt, gotoStatement());
    body.setBody(bb.build());
  }

  private JGotoStmt gotoStatement() {
    JGotoStmt go = Jimple.newGotoStmt(targetInstruction.getStmt().getPositionInfo());
    setStmt(go);
    addTags(go);
    return go;
  }

}
