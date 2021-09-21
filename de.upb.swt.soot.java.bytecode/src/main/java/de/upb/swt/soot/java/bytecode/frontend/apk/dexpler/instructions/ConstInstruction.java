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
import de.upb.swt.soot.core.jimple.common.constant.Constant;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.constant.LongConstant;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexBody;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.IDalvikTyper;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.typing.DalvikTyper;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.typing.UntypedConstant;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.typing.UntypedIntOrFloatConstant;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.typing.UntypedLongOrDoubleConstant;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.NarrowLiteralInstruction;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction;


public class ConstInstruction extends DexlibAbstractInstruction {

  public ConstInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  @Override
  public void jimplify(DexBody body) {
    int dest = ((OneRegisterInstruction) instruction).getRegisterA();

    Constant cst = getConstant(dest, body);
    JAssignStmt assign = Jimple.newAssignStmt(body.getRegisterLocal(dest), cst, StmtPositionInfo.createNoStmtPositionInfo());
    setStmt(assign);
    addTags(assign);
    body.add(assign);

    if (IDalvikTyper.ENABLE_DVKTYPER) {
      if (cst instanceof UntypedConstant) {
        DalvikTyper.v().addConstraint(assign.getLeftOp(), assign.getRightOp());
      } else {
        DalvikTyper.v().setType(assign.getLeftOp(), cst.getType(), false);
      }
    }
  }

  /**
   * Return the literal constant for this instruction.
   *
   * @param dest
   *          the register number to fill
   * @param body
   *          the body containing the instruction
   */
  private Constant getConstant(int dest, DexBody body) {

    long literal = 0;

    if (instruction instanceof WideLiteralInstruction) {
      literal = ((WideLiteralInstruction) instruction).getWideLiteral();
    } else if (instruction instanceof NarrowLiteralInstruction) {
      literal = ((NarrowLiteralInstruction) instruction).getNarrowLiteral();
    } else {
      throw new RuntimeException("literal error: expected narrow or wide literal.");
    }

    // floats are handled later in DexBody by calling DexNumtransformer
    Opcode opcode = instruction.getOpcode();
    switch (opcode) {
      case CONST:
      case CONST_4:
      case CONST_16:
        if (IDalvikTyper.ENABLE_DVKTYPER) {
          return UntypedIntOrFloatConstant.v((int) literal);
        } else {
          return IntConstant.getInstance((int) literal);
        }

      case CONST_HIGH16:
        if (IDalvikTyper.ENABLE_DVKTYPER) {
          //
          // return UntypedIntOrFloatConstant.v((int)literal<<16).toFloatConstant();
          // seems that dexlib correctly puts the 16bits into the topmost bits.
          //
          return UntypedIntOrFloatConstant.v((int) literal);// .toFloatConstant();
        } else {
          return IntConstant.getInstance((int) literal);
        }

      case CONST_WIDE_HIGH16:
        if (IDalvikTyper.ENABLE_DVKTYPER) {
          // return UntypedLongOrDoubleConstant.v((long)literal<<48).toDoubleConstant();
          // seems that dexlib correctly puts the 16bits into the topmost bits.
          //
          return UntypedLongOrDoubleConstant.v(literal);// .toDoubleConstant();
        } else {
          return LongConstant.getInstance(literal);
        }

      case CONST_WIDE:
      case CONST_WIDE_16:
      case CONST_WIDE_32:
        if (IDalvikTyper.ENABLE_DVKTYPER) {
          return UntypedLongOrDoubleConstant.v(literal);
        } else {
          return LongConstant.getInstance(literal);
        }
      default:
        throw new IllegalArgumentException("Expected a const or a const-wide instruction, got neither.");
    }
  }

  @Override
  boolean overridesRegister(int register) {
    OneRegisterInstruction i = (OneRegisterInstruction) instruction;
    int dest = i.getRegisterA();
    return register == dest;
  }
}
