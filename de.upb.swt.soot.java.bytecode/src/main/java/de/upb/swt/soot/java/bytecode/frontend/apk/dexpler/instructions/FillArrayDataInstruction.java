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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.common.constant.*;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexBody;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexType;
import de.upb.swt.soot.java.core.language.JavaJimple;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.ArrayPayload;
import org.jf.dexlib2.iface.instruction.formats.Instruction22c;
import org.jf.dexlib2.iface.instruction.formats.Instruction31t;
import org.jf.dexlib2.iface.reference.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FillArrayDataInstruction extends PseudoInstruction {
  private static final Logger logger = LoggerFactory.getLogger(FillArrayDataInstruction.class);

  public FillArrayDataInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction31t)) {
      throw new IllegalArgumentException("Expected Instruction31t but got: " + instruction.getClass());
    }

    Instruction31t fillArrayInstr = (Instruction31t) instruction;
    int destRegister = fillArrayInstr.getRegisterA();
    int offset = fillArrayInstr.getCodeOffset();
    int targetAddress = codeAddress + offset;

    Instruction referenceTable = body.instructionAtAddress(targetAddress).instruction;

    if (!(referenceTable instanceof ArrayPayload)) {
      throw new RuntimeException("Address " + targetAddress + "refers to an invalid PseudoInstruction.");
    }

    ArrayPayload arrayTable = (ArrayPayload) referenceTable;

    Local arrayReference = body.getRegisterLocal(destRegister);
    List<Number> elements = arrayTable.getArrayElements();
    int numElements = elements.size();

    Stmt firstAssign = null;
    for (int i = 0; i < numElements; i++) {
      JArrayRef arrayRef = JavaJimple.getInstance().newArrayRef(arrayReference, IntConstant.getInstance(i));
      NumericConstant element = getArrayElement(elements.get(i), body, destRegister);
      if (element == null) {
        break;
      }
      JAssignStmt assign = Jimple.newAssignStmt(arrayRef, element, StmtPositionInfo.createNoStmtPositionInfo());
      addTags(assign);
      body.add(assign);
      if (i == 0) {
        firstAssign = assign;
      }
    }
    if (firstAssign == null) { // if numElements == 0. Is it possible?
      firstAssign = Jimple.newNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
      body.add(firstAssign);
    }

    setStmt(firstAssign);

  }

  private NumericConstant getArrayElement(Number element, DexBody body, int arrayRegister) {

    List<DexlibAbstractInstruction> instructions = body.instructionsBefore(this);
    Set<Integer> usedRegisters = new HashSet<Integer>();
    usedRegisters.add(arrayRegister);

    Type elementType = null;
    Outer: for (DexlibAbstractInstruction i : instructions) {
      if (usedRegisters.isEmpty()) {
        break;
      }

      for (int reg : usedRegisters) {
        if (i instanceof NewArrayInstruction) {
          NewArrayInstruction newArrayInstruction = (NewArrayInstruction) i;
          Instruction22c instruction22c = (Instruction22c) newArrayInstruction.instruction;
          if (instruction22c.getRegisterA() == reg) {
            ArrayType arrayType = (ArrayType) DexType.toSoot((TypeReference) instruction22c.getReference());
            elementType = arrayType.getBaseType();
            break Outer;
          }
        }
      }

      // look for new registers
      for (int reg : usedRegisters) {
        int newRegister = i.movesToRegister(reg);
        if (newRegister != -1) {
          usedRegisters.add(newRegister);
          usedRegisters.remove(reg);
          break; // there can't be more than one new
        }
      }
    }

    if (elementType == null) {
      // throw new InternalError("Unable to find array type to type array elements!");
      logger.warn("Unable to find array type to type array elements! Array was not defined! (obfuscated bytecode?)");
      return null;
    }

    NumericConstant value;

    if (elementType instanceof PrimitiveType.BooleanType) {
      value = IntConstant.getInstance(element.intValue());
      IntConstant ic = (IntConstant) value;
      if (ic.getValue() != 0) {
        value = IntConstant.getInstance(1);
      }
    } else if (elementType instanceof PrimitiveType.ByteType) {
      value = IntConstant.getInstance(element.byteValue());
    } else if (elementType instanceof PrimitiveType.CharType || elementType instanceof PrimitiveType.ShortType) {
      value = IntConstant.getInstance(element.shortValue());
    } else if (elementType instanceof PrimitiveType.DoubleType) {
      value = DoubleConstant.getInstance(Double.longBitsToDouble(element.longValue()));
    } else if (elementType instanceof PrimitiveType.FloatType) {
      value = FloatConstant.getInstance(Float.intBitsToFloat(element.intValue()));
    } else if (elementType instanceof PrimitiveType.IntType) {
      value = IntConstant.getInstance(element.intValue());
    } else if (elementType instanceof PrimitiveType.LongType) {
      value = LongConstant.getInstance(element.longValue());
    } else {
      throw new RuntimeException("Invalid Array Type occured in FillArrayDataInstruction: " + elementType);
    }
    return value;

  }

  @Override
  public void computeDataOffsets(DexBody body) {
    if (!(instruction instanceof Instruction31t)) {
      throw new IllegalArgumentException("Expected Instruction31t but got: " + instruction.getClass());
    }

    Instruction31t fillArrayInstr = (Instruction31t) instruction;
    int offset = fillArrayInstr.getCodeOffset();
    int targetAddress = codeAddress + offset;

    Instruction referenceTable = body.instructionAtAddress(targetAddress).instruction;

    if (!(referenceTable instanceof ArrayPayload)) {
      throw new RuntimeException("Address 0x" + Integer.toHexString(targetAddress)
          + " refers to an invalid PseudoInstruction (" + referenceTable.getClass() + ").");
    }

    ArrayPayload arrayTable = (ArrayPayload) referenceTable;
    int numElements = arrayTable.getArrayElements().size();
    int widthElement = arrayTable.getElementWidth();
    int size = (widthElement * numElements) / 2; // addresses are on 16bits

    // From org.jf.dexlib.Code.Format.ArrayDataPseudoInstruction we learn
    // that there are 6 bytes after the magic number that we have to jump.
    // 6 bytes to jump = address + 3
    //
    // out.writeByte(0x00); // magic
    // out.writeByte(0x03); // number
    // out.writeShort(elementWidth); // 2 bytes
    // out.writeInt(elementCount); // 4 bytes
    // out.write(encodedValues);
    //

    setDataFirstByte(targetAddress + 3); // address for 16 bits elements not 8 bits
    setDataLastByte(targetAddress + 3 + size);// - 1);
    setDataSize(size);

    // TODO: how to handle this with dexlib2 ?
    // ByteArrayAnnotatedOutput out = new ByteArrayAnnotatedOutput();
    // arrayTable.write(out, targetAddress);
    //
    // byte[] outa = out.getArray();
    // byte[] data = new byte[outa.length-6];
    // for (int i=6; i<outa.length; i++) {
    // data[i-6] = outa[i];
    // }
    // setData (data);
  }

}
