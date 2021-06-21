package de.upb.swt.soot.java.bytecode.frontend;
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
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.expr.Expr;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.visitor.ReplaceUseStmtVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Stack operand.
 *
 * @author Aaloan Miftah
 */
final class Operand {

  @Nonnull protected final AbstractInsnNode insn;
  @Nonnull protected final Value value;
  @Nullable protected Local stackLocal;
  @Nonnull private final AsmMethodSource methodSource;

  @Nonnull private final List<Stmt> stmtUsages = new ArrayList<>();
  @Nonnull private final List<Expr> exprUsages = new ArrayList<>();

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
    stmtUsages.add(stmt);
  }

  /**
   * Adds a usage of this operand (so whenever it is used in a Expr)
   *
   * @param expr the usage
   */
  void addUsageInExpr(@Nonnull Expr expr) {
    exprUsages.add(expr);
  }

  /** Updates all statements and expressions that use this Operand. */
  void updateUsages() {
    ReplaceUseStmtVisitor replaceStmtVisitor =
        new ReplaceUseStmtVisitor(this.value, this.stackOrValue());

    for (Expr exprUsage : exprUsages) {
      List<Stmt> stmts = this.methodSource.getStmtsThatUse(exprUsage);

      stmts =
          stmts.stream()
              .map(this.methodSource::getLatestVersionOfStmt)
              .collect(Collectors.toList());

      stmtUsages.addAll(
          stmts.stream().filter(stmt -> !stmtUsages.contains(stmt)).collect(Collectors.toList()));
    }

    for (int i = 0; i < stmtUsages.size(); i++) {
      Stmt oldUsage = stmtUsages.get(i);

      // resolve stmt in method source, it might not exist anymore!
      oldUsage = methodSource.getLatestVersionOfStmt(oldUsage);

      oldUsage.accept(replaceStmtVisitor);
      Stmt newUsage = replaceStmtVisitor.getResult();

      if (oldUsage != newUsage) {
        methodSource.replaceStmt(oldUsage, newUsage);
        stmtUsages.set(i, newUsage);
      }
    }
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
    return stackOrValue().equivTo(other.stackOrValue());
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
