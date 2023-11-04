package sootup.java.bytecode.frontend;
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
import java.util.ArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.objectweb.asm.tree.AbstractInsnNode;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.visitor.ReplaceUseStmtVisitor;

/**
 * Frame of stack for an instruction. (see <a
 * href="https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-2.html#jvms-2.6">...</a> )
 *
 * @author Aaloan Miftah
 */
final class StackFrame {
  @Nonnull private final AbstractInsnNode insn;
  @Nullable private Operand[] out;
  @Nullable private Local[] inStackLocals;
  @Nonnull private final ArrayList<Operand[]> in = new ArrayList<>(1);
  @Nonnull private final AsmMethodSource src;

  /**
   * Constructs a new stack frame.
   *
   * @param src source the frame belongs to.
   */
  StackFrame(@Nonnull AbstractInsnNode insn, @Nonnull AsmMethodSource src) {
    this.insn = insn;
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
    if (in.get(0).length != oprs.length || oprs.length == 0) {
      throw new IllegalArgumentException("Invalid in operands length!");
    }

    for (int i = 0; i < oprs.length; i++) {
      Operand newOp = oprs[i];

      Local stack = inStackLocals[i];
      if (stack != null) {
        newOp.changeStackLocal(stack);
      } else {
        if (in.get(0)[i].value == newOp.value) {
          // all branches have the same value, so no stack local is needed to converge the values
          continue;
        }

        // Search for a stack local that was already allocated for an operand in a different branch
        for (int j = 0; j != in.size(); j++) {
          stack = in.get(j)[i].stackLocal;
          if (stack != null) {
            break;
          }
        }

        // The incoming operand may already have a stack local allocated that can be re-used
        if (stack == null && newOp.stackLocal != null) {
          stack = newOp.stackLocal;
        }

        // Didn't find any pre-allocated stack local from any operand.
        // So create a new stack local.
        // TODO use a special case when the statement is an assignment to a local since in that case
        //  we can use the local directly instead of creating a new stack local
        if (stack == null) {
          stack = src.newStackLocal();
        }

        /* add assign statement for prevOp */
        for (int j = 0; j != in.size(); j++) {
          Operand prevOp = in.get(j)[i];
          prevOp.changeStackLocal(stack);
        }
        newOp.changeStackLocal(stack);

        inStackLocals[i] = stack;
        // TODO `in.get(0)` is weird because of the index?
        // TODO make it more obvious that this is only run the first time (because `inStackLocals[i]
        //  == null`)
        ReplaceUseStmtVisitor replaceUseStmtVisitor =
            new ReplaceUseStmtVisitor(in.get(0)[i].value, stack);
        Stmt oldStatement = this.src.getStmt(this.insn);
        oldStatement.accept(replaceUseStmtVisitor);
        this.src.replaceStmt(oldStatement, replaceUseStmtVisitor.getResult());
      }
    }

    in.add(oprs);
  }
}
