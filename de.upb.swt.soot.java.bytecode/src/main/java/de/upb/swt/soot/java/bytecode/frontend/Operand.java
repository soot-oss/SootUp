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
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Stack operand.
 *
 * @author Aaloan Miftah
 */
final class Operand {

  public static final Operand DWORD_DUMMY = new Operand(null, null, null);
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

    for (Expr exprUsage : exprUsages) {
      methodSource
          .getStmtsThatUse(exprUsage)
          .map(methodSource::getLatestVersionOfStmt)
          .filter(Objects::nonNull)
          .filter(stmt -> !stmtUsages.contains(stmt))
          .forEach(stmtUsages::add);
    }
    /*for(Stmt stmt : stmtUsages){
      System.out.println(stmt);
    }
    System.out.println("----------------------");

    System.out.println("oldUse: " + value);
    System.out.println("newUse: " + stackOrValue());*/

    if (value == stackOrValue()) return;

    ReplaceUseStmtVisitor replaceStmtVisitor = new ReplaceUseStmtVisitor(value, stackOrValue());

    List<Stmt> stmtsToDelete = new ArrayList<>();

    for (int i = 0; i < stmtUsages.size(); i++) {
      Stmt oldUsage = stmtUsages.get(i);
      // System.out.println("OldUsage: " + oldUsage);

      // resolve stmt in method source, it might not exist anymore!
      Stmt oldUsageLatestVersion = methodSource.getLatestVersionOfStmt(oldUsage);
      // System.out.println("OldUsageLatestVersion: " + oldUsage);

      if (oldUsageLatestVersion == null) {
        stmtsToDelete.add(oldUsageLatestVersion);
      } else {
        Stmt usage = null, newUsage = null;
        Set<Value> usesLV = new HashSet<>(oldUsageLatestVersion.getUses());
        Set<Value> uses = new HashSet<>(oldUsage.getUses());
        if (usesLV.contains(value)) {
          usage = oldUsageLatestVersion;
        } else if (uses.contains(value)) {
          usage = oldUsage;
        } // else{
        // System.out.println("CAN NOT UPDATE ANYMORE");
        // throw new RuntimeException("The given stmt :" + oldUsageLatestVersion + " can not be
        // replaced by " + value);
        // }
        if (usage != null) {
          usage.accept(replaceStmtVisitor);
          newUsage = replaceStmtVisitor.getResult();
        } else {
          newUsage = oldUsageLatestVersion;
        }

        // System.out.println("NewUsage: " + newUsage);

        if (oldUsageLatestVersion != newUsage) {
          methodSource.replaceStmt(oldUsageLatestVersion, newUsage);
          stmtUsages.set(i, newUsage);
        }
        // System.out.println(stmtUsages);
      }
      // System.out.println();
    }
    // System.out.println("**************************************************");

    stmtUsages.removeAll(stmtsToDelete);
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
        || ((this == DWORD_DUMMY) == (other == DWORD_DUMMY)
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
