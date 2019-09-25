package de.upb.soot.bytecodefrontend.frontend;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2014 Raja Vallee-Rai and others
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

import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.stmt.AbstractDefinitionStmt;
import de.upb.soot.jimple.common.stmt.JAssignStmt;
import de.upb.soot.jimple.common.stmt.Stmt;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Frame of stack for an instruction.
 *
 * @author Aaloan Miftah
 */
final class StackFrame {

  @Nullable private Operand[] out;
  @Nullable private Local[] inStackLocals;
  @Nullable private ValueBox[] boxes;
  @Nullable private ArrayList<Operand[]> in;
  @Nonnull private final AsmMethodSource src;

  /**
   * Constructs a new stack frame.
   *
   * @param src source the frame belongs to.
   */
  StackFrame(@Nonnull AsmMethodSource src) {
    this.src = src;
  }

  /** @return operands produced by this frame. */
  @Nullable
  Operand[] out() {
    return out;
  }

  /**
   * Sets the operands used by this frame.
   *
   * @param oprs the operands.
   */
  void in(@Nonnull Operand... oprs) {
    ArrayList<Operand[]> in = this.in;
    if (in == null) {
      in = this.in = new ArrayList<>(1);
    } else {
      in.clear();
    }
    in.add(oprs);
    inStackLocals = new Local[oprs.length];
  }

  /**
   * Sets the value boxes corresponding to the operands used by this frame.
   *
   * @param boxes the boxes.
   */
  void boxes(ValueBox... boxes) {
    this.boxes = boxes;
  }

  /**
   * Sets the operands produced by this frame.
   *
   * @param oprs the operands.
   */
  void out(Operand... oprs) {
    out = oprs;
  }

  /**
   * Merges the specified operands with the operands used by this frame.
   *
   * @param oprs the new operands.
   * @throws IllegalArgumentException if the number of new operands is not equal to the number of
   *     old operands.
   */
  void mergeIn(@Nonnull Operand... oprs) {
    ArrayList<Operand[]> in = this.in;
    if (in.get(0).length != oprs.length) {
      throw new IllegalArgumentException("Invalid in operands length!");
    }
    int nrIn = in.size();
    boolean diff = false;
    for (int i = 0; i != oprs.length; i++) {
      Operand newOp = oprs[i];

      diff = true;
      /* merge, since prevOp != newOp */
      Local stack = inStackLocals[i];
      if (stack != null) {
        if (newOp.stack == null) {
          newOp.stack = stack;
          JAssignStmt as =
              Jimple.newAssignStmt(stack, newOp.value, PositionInfo.createNoPositionInfo());
          src.setUnit(newOp.insn, as);
          newOp.updateBoxes();
        } else {
          JAssignStmt as =
              Jimple.newAssignStmt(
                  stack, newOp.stackOrValue(), PositionInfo.createNoPositionInfo());
          src.mergeUnits(newOp.insn, as);
          newOp.addBox(as.getRightOpBox());
        }
      } else {
        for (int j = 0; j != nrIn; j++) {
          stack = in.get(j)[i].stack;
          if (stack != null) {
            break;
          }
        }
        if (stack == null) {
          stack = newOp.stack;
          if (stack == null) {
            stack = src.newStackLocal();
          }
        }
        /* add assign statement for prevOp */
        ValueBox box = boxes == null ? null : boxes[i];
        for (int j = 0; j != nrIn; j++) {
          Operand prevOp = in.get(j)[i];
          if (prevOp.stack == stack) {
            continue;
          }
          prevOp.removeBox(box);
          if (prevOp.stack == null) {
            prevOp.stack = stack;
            JAssignStmt as =
                Jimple.newAssignStmt(stack, prevOp.value, PositionInfo.createNoPositionInfo());
            src.setUnit(prevOp.insn, as);
          } else {
            Stmt u = src.getUnit(prevOp.insn);
            AbstractDefinitionStmt as =
                (AbstractDefinitionStmt)
                    (u instanceof StmtContainer ? ((StmtContainer) u).getFirstUnit() : u);
            ValueBox lvb = as.getLeftOpBox();
            assert lvb.getValue() == prevOp.stack : "Invalid stack local!";
            ValueBox.$Accessor.setValue(lvb, stack);
            prevOp.stack = stack;
          }
          prevOp.updateBoxes();
        }
        if (newOp.stack != stack) {
          if (newOp.stack == null) {
            newOp.stack = stack;
            JAssignStmt as =
                Jimple.newAssignStmt(stack, newOp.value, PositionInfo.createNoPositionInfo());
            src.setUnit(newOp.insn, as);
          } else {
            Stmt u = src.getUnit(newOp.insn);
            AbstractDefinitionStmt as =
                (AbstractDefinitionStmt)
                    (u instanceof StmtContainer ? ((StmtContainer) u).getFirstUnit() : u);
            ValueBox lvb = as.getLeftOpBox();
            assert lvb.getValue() == newOp.stack : "Invalid stack local!";
            ValueBox.$Accessor.setValue(lvb, stack);
            newOp.stack = stack;
          }
          newOp.updateBoxes();
        }
        if (box != null) {
          ValueBox.$Accessor.setValue(box, stack);
        }
        inStackLocals[i] = stack;
      }

      /*
       * this version uses allocates local if it finds both operands have stack locals allocated already
       */
      /*
       * if (stack == null) { if (in.size() != 1) throw new AssertionError("Local h " + in.size()); stack =
       * src.newStackLocal(); inStackLocals[i] = stack; ValueBox box = boxes == null ? null : boxes[i]; /* add assign
       * statement for prevOp * for (int j = 0; j != nrIn; j++) { Operand prevOp = in.get(j)[i]; prevOp.removeBox(box); if
       * (prevOp.stack == null) { prevOp.stack = stack; as = Jimple.v().newAssignStmt(stack, prevOp.value);
       * src.setUnit(prevOp.insn, as); prevOp.updateBoxes(); } else { as = Jimple.v().newAssignStmt(stack,
       * prevOp.stackOrValue()); src.mergeUnits(prevOp.insn, as); } prevOp.addBox(as.getRightOpBox()); } if (box != null)
       * box.setValue(stack); } if (newOp.stack == null) { newOp.stack = stack; as = Jimple.v().newAssignStmt(stack,
       * newOp.value); src.setUnit(newOp.insn, as); newOp.updateBoxes(); } else { as = Jimple.v().newAssignStmt(stack,
       * newOp.stackOrValue()); src.mergeUnits(newOp.insn, as); } newOp.addBox(as.getRightOpBox());
       */
    }
    if (diff) {
      in.add(oprs);
    }
  }
}
