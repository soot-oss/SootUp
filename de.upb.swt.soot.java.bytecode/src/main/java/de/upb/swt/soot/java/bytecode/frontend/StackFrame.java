package de.upb.swt.soot.java.bytecode.frontend;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
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
  @Nullable private ValueBox[] boxes;
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
   * Sets the value boxes corresponding to the operands used by this frame.
   *
   * @param boxes the boxes.
   */
  void setBoxes(@Nonnull ValueBox... boxes) {
    this.boxes = boxes;
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
          newOp.updateBoxes();
        } else {
          final Value rvalue = newOp.stackOrValue();
          // check for self/identity assignments
          if (stack != rvalue) {
            JAssignStmt as =
                Jimple.newAssignStmt(stack, rvalue, StmtPositionInfo.createNoStmtPositionInfo());
            src.mergeStmts(newOp.insn, as);
            newOp.addBox(as.getRightOpBox());
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
                Jimple.newAssignStmt(
                    stack, prevOp.value, StmtPositionInfo.createNoStmtPositionInfo());
            src.setStmt(prevOp.insn, as);
          } else {
            Stmt u = src.getStmt(prevOp.insn);
            AbstractDefinitionStmt as =
                (AbstractDefinitionStmt)
                    (u instanceof StmtContainer ? ((StmtContainer) u).getFirstStmt() : u);
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
                Jimple.newAssignStmt(
                    stack, newOp.value, StmtPositionInfo.createNoStmtPositionInfo());
            src.setStmt(newOp.insn, as);
          } else {
            Stmt u = src.getStmt(newOp.insn);
            AbstractDefinitionStmt as =
                (AbstractDefinitionStmt)
                    (u instanceof StmtContainer ? ((StmtContainer) u).getFirstStmt() : u);
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
    // add if there is a difference
    if (0 < oprs.length) {
      in.add(oprs);
    }
  }
}
