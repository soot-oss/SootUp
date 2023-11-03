package sootup.java.bytecode.frontend;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Andreas Dann, Christian Brüggemann and others
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
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.objectweb.asm.tree.AbstractInsnNode;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.visitor.ReplaceUseStmtVisitor;

/**
 * Stack operand.
 *
 * @author Aaloan Miftah
 */
class Operand {

  @SuppressWarnings("ConstantConditions")
  static final Operand DWORD_DUMMY = new Operand(null, null, null);

  @Nonnull protected AbstractInsnNode insn;
  @Nonnull protected final Value value;
  @Nullable protected Local stackLocal;
  @Nonnull private final AsmMethodSource methodSource;

  /**
   * Constructs a new stack operand.
   *
   * @param insn the instruction that produced this operand.
   * @param value the generated value.
   */
  Operand(
      @Nonnull AbstractInsnNode insn, @Nonnull Value value, @Nonnull AsmMethodSource methodSource) {
    this.insn = insn;
    this.value = value;
    this.methodSource = methodSource;
  }

  Local getOrAssignValueToStackLocal() {
    if (stackLocal == null) {
      changeStackLocal(methodSource.newStackLocal());
    }

    return stackLocal;
  }

  void emitStatement() {
    if (this == DWORD_DUMMY) {
      return;
    }

    if (value instanceof AbstractInvokeExpr) {
      methodSource.setStmt(
          insn,
          Jimple.newInvokeStmt((AbstractInvokeExpr) value, methodSource.getStmtPositionInfo()));
    } else {
      // create an assignment that uses the value because it might have side effects
      getOrAssignValueToStackLocal();
    }
  }

  void changeStackLocal(Local newStackLocal) {
    Local oldStackLocal = this.stackLocal;

    if (oldStackLocal == newStackLocal) {
      // nothing to change
      return;
    }

    JAssignStmt assignStmt = methodSource.getStmt(insn);
    if (assignStmt == null) {
      // TODO the position info is the position of the *usage* (which is only mostly correct?)
      // emit `$newStackLocal = value`
      methodSource.setStmt(
          insn, Jimple.newAssignStmt(newStackLocal, value, methodSource.getStmtPositionInfo()));
    } else {
      assert assignStmt.getLeftOp() == oldStackLocal;
      // replace `$oldStackLocal = value` with `$newStackLocal = value`
      methodSource.replaceStmt(assignStmt, assignStmt.withVariable(newStackLocal));
    }

    // Replace all usages of `oldStackLocal` with `newStackLocal`
    if (oldStackLocal != null) {
      ReplaceUseStmtVisitor replaceStmtVisitor =
          new ReplaceUseStmtVisitor(oldStackLocal, newStackLocal);
      for (Stmt oldUsage :
          methodSource.getStmtsThatUse(oldStackLocal).collect(Collectors.toList())) {
        oldUsage.accept(replaceStmtVisitor);
        Stmt newUsage = replaceStmtVisitor.getResult();

        if (newUsage != null && oldUsage != newUsage) {
          methodSource.replaceStmt(oldUsage, newUsage);
        }
      }
    }

    this.stackLocal = newStackLocal;
  }

  Local toLocal() {
    if (stackLocal == null && value instanceof Local) {
      return (Local) value;
    }

    return getOrAssignValueToStackLocal();
  }

  Immediate toImmediate() {
    if (stackLocal == null && value instanceof Immediate) {
      return (Immediate) value;
    }

    return getOrAssignValueToStackLocal();
  }

  /**
   * Determines if this operand is equal to another operand.
   *
   * @param other the other operand.
   * @return {@code true} if this operand is equal to another operand, {@code false} otherwise.
   */
  boolean equivTo(@Nonnull Operand other) {
    Value stackOrValue = stackLocal == null ? value : stackLocal;
    Value stackOrValueOther = other.stackLocal == null ? other.value : other.stackLocal;

    // care for DWORD comparison, as asValue is null, which would result in a
    // NullPointerException
    return (this == other)
        || ((this == Operand.DWORD_DUMMY) == (other == Operand.DWORD_DUMMY)
            && stackOrValue.equivTo(stackOrValueOther));
  }

  @Override
  public String toString() {
    return "Operand{" + "insn=" + insn + ", value=" + value + ", stack=" + stackLocal + '}';
  }

  @Nonnull
  public AbstractInsnNode getInsn() {
    return insn;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Operand && equivTo((Operand) other);
  }
}
