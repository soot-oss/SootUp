package sootup.java.bytecode.frontend.conversion;

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
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
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

  @NonNull protected AbstractInsnNode insn;
  @NonNull protected final Value value;
  // TODO probably need to store the `insn` for the STORE instruction when a *real*
  //  local is used here and use that when merging,
  //  or more specifically when changing to a different stack local in `changeStackLocal`
  @Nullable protected Local stackLocal;
  @NonNull private final AsmMethodSource methodSource;
  @NonNull private final StmtPositionInfo positionInfo;

  /**
   * All trap handlers (catch blocks) that were active at the instruction where the operand was
   * created. This is important because when the operand is used, the active trap handlers might
   * differ, in which case the operand can't be inlined into its usage.
   */
  private final Set<TryCatchBlockNode> activeTrapHandlers;

  /**
   * Constructs a new stack operand.
   *
   * @param insn the instruction that produced this operand.
   * @param value the generated value.
   */
  Operand(
      @NonNull AbstractInsnNode insn, @NonNull Value value, @NonNull AsmMethodSource methodSource) {
    this.insn = insn;
    this.value = value;
    this.methodSource = methodSource;
    this.positionInfo = methodSource == null ? null : methodSource.getStmtPositionInfo();
    this.activeTrapHandlers =
        methodSource == null ? new HashSet<>() : new HashSet<>(methodSource.activeTrapHandlers);
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

    if (methodSource.getStmt(insn) != null) {
      // the operand is already used, which means side effects already happen as well
      return;
    }

    if (value instanceof AbstractInvokeExpr) {
      methodSource.setStmt(insn, Jimple.newInvokeStmt((AbstractInvokeExpr) value, positionInfo));
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

    Stmt stmt = methodSource.getStmt(insn);
    if (!(stmt instanceof JAssignStmt)) {
      // emit `$newStackLocal = value`
      if (value instanceof JCaughtExceptionRef) {
        JIdentityStmt identityStmt =
            Jimple.newIdentityStmt(newStackLocal, (JCaughtExceptionRef) value, positionInfo);
        methodSource.setStmt(insn, identityStmt);
      } else {
        methodSource.setStmt(insn, Jimple.newAssignStmt(newStackLocal, value, positionInfo));
      }
    } else {
      JAssignStmt assignStmt = (JAssignStmt) stmt;
      assert assignStmt.getLeftOp() == oldStackLocal || assignStmt.getLeftOp() == newStackLocal;
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
    // Don't inline when the trap handlers (catch blocks) change between the operand and the usage.
    // Even though immediates are just locals or constants,
    // the corresponding instructions could still throw a `VirtualMachineError`.
    boolean matchingTrapHandlers = this.activeTrapHandlers.equals(methodSource.activeTrapHandlers);

    if (stackLocal == null && value instanceof Immediate && matchingTrapHandlers) {
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
  boolean equivTo(@NonNull Operand other) {
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

  @NonNull
  public AbstractInsnNode getInsn() {
    return insn;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Operand && equivTo((Operand) other);
  }
}
