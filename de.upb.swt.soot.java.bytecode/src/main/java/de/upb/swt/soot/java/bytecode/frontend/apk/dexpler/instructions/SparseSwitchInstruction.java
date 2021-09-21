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
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JSwitchStmt;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexBody;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.IDalvikTyper;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.typing.DalvikTyper;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.SwitchElement;
import org.jf.dexlib2.iface.instruction.formats.SparseSwitchPayload;


public class SparseSwitchInstruction extends SwitchInstruction {

  public SparseSwitchInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  @Override
  protected Stmt switchStatement(DexBody body, Instruction targetData, Local key) {
    SparseSwitchPayload i = (SparseSwitchPayload) targetData;
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
    // TODO: KK
    //JSwitchStmt switchStmt = Jimple.newLookupSwitchStmt(key, lookupValues, targets, defaultTarget);
    JSwitchStmt switchStmt = Jimple.newLookupSwitchStmt(key, lookupValues, StmtPositionInfo.createNoStmtPositionInfo());
    setStmt(switchStmt);
    addTags(switchStmt);

    if (IDalvikTyper.ENABLE_DVKTYPER) {
      DalvikTyper.v().setType(switchStmt.getKey(), PrimitiveType.IntType.getInstance(), true);
    }

    return switchStmt;
  }

  @Override
  public void computeDataOffsets(DexBody body) {
    // System.out.println("class of instruction: "+ instruction.getClass());
    // int offset = ((OffsetInstruction) instruction).getCodeOffset();
    // int targetAddress = codeAddress + offset;
    // Instruction targetData = body.instructionAtAddress(targetAddress).instruction;
    // SparseSwitchPayload ssInst = (SparseSwitchPayload) targetData;
    // List<? extends SwitchElement> targetAddresses = ssInst.getSwitchElements();
    // int size = targetAddresses.size() * 2; // @ are on 32bits
    //
    // // From org.jf.dexlib.Code.Format.SparseSwitchDataPseudoInstruction we learn
    // // that there are 2 bytes after the magic number that we have to jump.
    // // 2 bytes to jump = address + 1
    // //
    // // out.writeByte(0x00); // magic
    // // out.writeByte(0x02); // number
    // // out.writeShort(targets.length); // 2 bytes
    // // out.writeInt(firstKey);
    //
    // setDataFirstByte (targetAddress + 1);
    // setDataLastByte (targetAddress + 1 + size - 1);
    // setDataSize (size);
    //
    // ByteArrayAnnotatedOutput out = new ByteArrayAnnotatedOutput();
    // ssInst.write(out, targetAddress);
    //
    // byte[] outa = out.getArray();
    // byte[] data = new byte[outa.length-2];
    // for (int i=2; i<outa.length; i++) {
    // data[i-2] = outa[i];
    // }
    // setData (data);
  }

}
