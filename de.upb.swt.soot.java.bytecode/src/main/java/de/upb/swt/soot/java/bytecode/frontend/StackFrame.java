package de.upb.swt.soot.java.bytecode.frontend;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Andreas Dann, Markus Schmidt and others
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
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractDefinitionStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Frame of stack for an instruction. (see
 * https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-2.html#jvms-2.6 )
 *
 * @author Aaloan Miftah
 */
final class StackFrame {

  @Nullable private Operand[] out;
  @Nullable private Local[] inStackLocals;
  @Nullable private Value[] values;
  @Nonnull private final ArrayList<Operand[]> in = new ArrayList<>(1);
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
  Operand[] getOut() {
    return out;
  }

  /**
   * Sets the operands used by this frame.
   *
   * @param oprs the operands.
   */
  void setIn(@Nonnull Operand... oprs) {
    in.clear();
    in.add(oprs);
    inStackLocals = new Local[oprs.length];
  }

  /**
   * Sets the values corresponding to the operands used by this frame.
   *
   * @param boxes the boxes.
   */
  void setValues(Value... boxes) {
    this.values = boxes;
  }

  /**
   * Sets the operands produced by this frame.
   *
   * @param oprs the operands.
   */
  void setOut(@Nonnull Operand... oprs) {
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
    if (in.get(0).length != oprs.length) {
      throw new IllegalArgumentException("Invalid in operands length!");
    }
    final int nrIn = in.size();
    for (int i = 0; i < oprs.length; i++) {
      Operand newOp = oprs[i];

      /* merge, since prevOp != newOp */
      Local stack = inStackLocals[i];
      if (stack != null) {
        if (newOp.stack == null) {
          newOp.stack = stack;
          JAssignStmt as =
              Jimple.newAssignStmt(stack, newOp.value, StmtPositionInfo.createNoStmtPositionInfo());
          src.setStmt(newOp.insn, as);
          newOp.updateUsages();
        } else {
          final Value rvalue = newOp.stackOrValue();
          // check for self/identity assignments
          if (stack != rvalue) {
            JAssignStmt as =
                Jimple.newAssignStmt(stack, rvalue, StmtPositionInfo.createNoStmtPositionInfo());
            src.mergeStmts(newOp.insn, as);
          }
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
        Value box = values == null ? null : values[i];
        for (int j = 0; j != nrIn; j++) {
          Operand prevOp = in.get(j)[i];
          if (prevOp.stack == stack) {
            continue;
          }
          if (prevOp.stack == null) {
            prevOp.stack = stack;
            JAssignStmt as =
                Jimple.newAssignStmt(
                    stack, prevOp.value, StmtPositionInfo.createNoStmtPositionInfo());
            src.setStmt(prevOp.insn, as);
          } else {
            Stmt u = src.getStmt(prevOp.insn);
            AbstractDefinitionStmt as =
                (AbstractDefinitionStmt)
                    (u instanceof StmtContainer ? ((StmtContainer) u).getFirstStmt() : u);
            Value lvb = as.getLeftOp();
            assert lvb == prevOp.stack : "Invalid stack local!";
            prevOp.stack = stack;
          }
          prevOp.updateUsages();
        }
        if (newOp.stack != stack) {
          if (newOp.stack == null) {
            newOp.stack = stack;
            JAssignStmt as =
                Jimple.newAssignStmt(
                    stack, newOp.value, StmtPositionInfo.createNoStmtPositionInfo());
            src.setStmt(newOp.insn, as);
          } else {
            Stmt u = src.getStmt(newOp.insn);
            AbstractDefinitionStmt as =
                (AbstractDefinitionStmt)
                    (u instanceof StmtContainer ? ((StmtContainer) u).getFirstStmt() : u);
            Value lvb = as.getLeftOp();
            assert lvb == newOp.stack : "Invalid stack local!";
            newOp.stack = stack;
          }
          newOp.updateUsages();
        }
        if (box != null) {
          values[i] = stack;
        }
        inStackLocals[i] = stack;
      }

      /*
       * this version uses allocated locals if it finds both operands have stack locals allocated already
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
    // add if there is a difference
    if (0 < oprs.length) {
      in.add(oprs);
    }
  }
}
