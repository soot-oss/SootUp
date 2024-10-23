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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.ArrayPayload;
import org.jf.dexlib2.iface.instruction.formats.Instruction22c;
import org.jf.dexlib2.iface.instruction.formats.Instruction31t;
import org.jf.dexlib2.iface.reference.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.apk.frontend.Util.DexUtil;
import sootup.apk.frontend.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ArrayType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.java.core.language.JavaJimple;

public class FillArrayDataInstruction extends DexLibAbstractInstruction {

  private static final Logger logger = LoggerFactory.getLogger(FillArrayDataInstruction.class);

  @Override
  public void jimplify(DexBody body) {
    Instruction31t fillArrayInstr = (Instruction31t) instruction;
    int destRegister = fillArrayInstr.getRegisterA();
    int offset = fillArrayInstr.getCodeOffset();
    int targetAddress = codeAddress + offset;

    Instruction referenceTable = body.instructionAtAddress(targetAddress).instruction;

    ArrayPayload arrayTable = (ArrayPayload) referenceTable;

    Local arrayReference = body.getRegisterLocal(destRegister);
    List<Number> elements = arrayTable.getArrayElements();
    int numElements = elements.size();

    Stmt firstAssign = null;
    for (int i = 0; i < numElements; i++) {
      JArrayRef arrayRef =
          JavaJimple.getInstance().newArrayRef(arrayReference, IntConstant.getInstance(i));
      NumericConstant element = getArrayElement(elements.get(i), body, destRegister);
      if (element == null) {
        break;
      }
      JAssignStmt assign =
          Jimple.newAssignStmt(arrayRef, element, StmtPositionInfo.getNoStmtPositionInfo());
      body.add(assign);
      if (i == 0) {
        firstAssign = assign;
      }
    }
    if (firstAssign == null) { // if numElements == 0. Is it possible?
      logger.warn("Number of elements in the array is 0.. Weird case...");
      firstAssign = Jimple.newNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
      body.add(firstAssign);
    }
    setStmt(firstAssign);
  }

  public FillArrayDataInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  private NumericConstant getArrayElement(Number element, DexBody dexBody, int arrayRegister) {
    List<DexLibAbstractInstruction> instructions = dexBody.instructionsBefore(this);
    Set<Integer> usedRegisters = new HashSet<Integer>();
    usedRegisters.add(arrayRegister);
    Type elementType = null;
    Outer:
    for (DexLibAbstractInstruction i : instructions) {
      if (usedRegisters.isEmpty()) {
        break;
      }
      for (int register : usedRegisters) {
        if (i instanceof NewArrayInstruction) {
          NewArrayInstruction newArrayInstruction = (NewArrayInstruction) i;
          Instruction22c instruction22c = (Instruction22c) newArrayInstruction.instruction;
          if (instruction22c.getRegisterA() == register) {
            ArrayType arrayType =
                (ArrayType)
                    DexUtil.toSootType(
                        ((TypeReference) instruction22c.getReference()).getType(), 0);
            elementType = arrayType.getElementType();
            break Outer;
          }
        }
      }
      // // look for obsolete registers
      // for (int reg : usedRegisters) {
      // if (i.overridesRegister(reg)) {
      // usedRegisters.remove(reg);
      // break; // there can't be more than one obsolete
      // }
      // }
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
      logger.warn(
          "Unable to find array type to type array elements! Array was not defined! (obfuscated bytecode?)");
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
    } else if (elementType instanceof PrimitiveType.CharType
        || elementType instanceof PrimitiveType.ShortType) {
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
      throw new IllegalStateException(
          "Invalid Array Type occured in FillArrayDataInstruction: " + elementType);
    }
    return value;
  }
}
