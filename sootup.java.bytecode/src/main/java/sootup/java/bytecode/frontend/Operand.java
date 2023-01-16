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
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.objectweb.asm.tree.AbstractInsnNode;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.Expr;
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

  @Nonnull protected final AbstractInsnNode insn;
  @Nonnull protected final Value value;
  @Nullable protected Local stackLocal;
  @Nonnull private final AsmMethodSource methodSource;

  @Nonnull private final List<Stmt> usedByStmts = new ArrayList<>();
  @Nonnull private final List<Expr> usedByExpr = new ArrayList<>();

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

  /**
   * Adds a usage of this operand (so whenever it is used in a stmt)
   *
   * @param stmt the usage
   */
  void addUsageInStmt(@Nonnull Stmt stmt) {
    usedByStmts.add(stmt);
  }

  /**
   * Adds a usage of this operand (so whenever it is used in a Expr)
   *
   * @param expr the usage
   */
  void addUsageInExpr(@Nonnull Expr expr) {
    usedByExpr.add(expr);
  }

  /** Updates all statements and expressions that use this Operand. */
  void updateUsages() {

    for (Expr exprUsage : usedByExpr) {
      methodSource
          .getStmtsThatUse(exprUsage)
          .map(methodSource::getLatestVersionOfStmt)
          .filter(stmt -> !usedByStmts.contains(stmt))
          .forEach(usedByStmts::add);
    }

    if (value == stackOrValue()) return;

    ReplaceUseStmtVisitor replaceStmtVisitor = new ReplaceUseStmtVisitor(value, stackOrValue());

    List<Stmt> stmtsToDelete = new ArrayList<>();

    for (int i = 0; i < usedByStmts.size(); i++) {
      Stmt oldUsage = usedByStmts.get(i);

      // resolve stmt in method source, it might not exist anymore!
      oldUsage = methodSource.getLatestVersionOfStmt(oldUsage);

      oldUsage.accept(replaceStmtVisitor);
      Stmt newUsage = replaceStmtVisitor.getResult();

      if (oldUsage != newUsage) {
        methodSource.replaceStmt(oldUsage, newUsage);
        usedByStmts.set(i, newUsage);
      }
    }
    usedByStmts.removeAll(stmtsToDelete);
  }

  /** @return either the stack local allocated for this operand, or its value. */
  @Nonnull
  Value stackOrValue() {
    return stackLocal == null ? value : stackLocal;
  }

  /**
   * Determines if this operand is equal to another operand.
   *
   * @param other the other operand.
   * @return {@code true} if this operand is equal to another operand, {@code false} otherwise.
   */
  boolean equivTo(@Nonnull Operand other) {
    // care for DWORD comparison, as stackOrValue is null, which would result in a
    // NullPointerException
    return (this == other)
        || ((this == Operand.DWORD_DUMMY) == (other == Operand.DWORD_DUMMY)
            && stackOrValue().equivTo(other.stackOrValue()));
  }

  @Override
  public String toString() {
    return "Operand{" + "insn=" + insn + ", value=" + value + ", stack=" + stackLocal + '}';
  }

  @Nonnull
  public AbstractInsnNode getInsn() {
    return insn;
  }

  @Nonnull
  public Value getValue() {
    return value;
  }

  @Nullable
  public Local getStackLocal() {
    return stackLocal;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Operand && equivTo((Operand) other);
  }
}
