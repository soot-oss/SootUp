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
import de.upb.swt.soot.core.jimple.common.stmt.JIdentityStmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexBody;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.IDalvikTyper;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.typing.DalvikTyper;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;


public class MoveExceptionInstruction extends DexlibAbstractInstruction implements RetypeableInstruction {

  protected Type realType;
  protected JIdentityStmt stmtToRetype;

  public MoveExceptionInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  @Override
  public void jimplify(DexBody body) {
    int dest = ((OneRegisterInstruction) instruction).getRegisterA();
    Local l = body.getRegisterLocal(dest);
    stmtToRetype = Jimple.newIdentityStmt(l, JavaJimple.getInstance().newCaughtExceptionRef(), StmtPositionInfo.createNoStmtPositionInfo());
    setStmt(stmtToRetype);
    addTags(stmtToRetype);
    body.add(stmtToRetype);

    if (IDalvikTyper.ENABLE_DVKTYPER) {
      DalvikTyper.v().setType(stmtToRetype.getLeftOp(), new JavaClassType("java.lang.Throwable"), false);
    }
  }

  @Override
  public void setRealType(DexBody body, Type t) {
    realType = t;
    body.addRetype(this);
  }

  @Override
  public void retype(Body.BodyBuilder bodyBuilder) {
    if (realType == null) {
      throw new RuntimeException("Real type of this instruction has not been set or was already retyped: " + this);
    }
    if (bodyBuilder.getStmts().contains(stmtToRetype)) {
      Local l = (Local) (stmtToRetype.getLeftOp());
      l = l.withType(realType);
      int idx = bodyBuilder.getStmts().indexOf(stmtToRetype);
      JIdentityStmt idWithLocal = stmtToRetype.withLocal(l);
      bodyBuilder.getStmts().set(idx, idWithLocal);
      realType = null;
    }
  }

  @Override
  boolean overridesRegister(int register) {
    OneRegisterInstruction i = (OneRegisterInstruction) instruction;
    int dest = i.getRegisterA();
    return register == dest;
  }

}
