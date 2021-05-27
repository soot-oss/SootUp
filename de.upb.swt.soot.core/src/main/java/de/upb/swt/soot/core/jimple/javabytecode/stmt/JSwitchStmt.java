package de.upb.swt.soot.core.jimple.javabytecode.stmt;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Markus Schmidt, Thomas Johannesmeyer
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
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.stmt.BranchingStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.*;
import javax.annotation.Nonnull;

/*
 * Switch Statements (combining LookupSwitch/TableSwitch)
 * @author Markus Schmidt
 */
public class JSwitchStmt extends BranchingStmt implements Copyable {

  private final Immediate key;
  private List<IntConstant> values;
  private final boolean isTableSwitch;

  private JSwitchStmt(
      boolean isTableSwitch, @Nonnull StmtPositionInfo positionInfo, @Nonnull Immediate key) {
    super(positionInfo);
    this.isTableSwitch = isTableSwitch;
    this.key = key;
  }

  public JSwitchStmt(
      @Nonnull Immediate key, int lowIndex, int highIndex, @Nonnull StmtPositionInfo positionInfo) {
    this(true, positionInfo, key);

    if (lowIndex > highIndex) {
      throw new RuntimeException(
          "Error creating switch: lowIndex("
              + lowIndex
              + ") can't be greater than highIndex("
              + highIndex
              + ").");
    }

    values = new ArrayList<>();
    int i;
    // "<=" is not possible; possible overflow would wrap i resulting in an infinite loop
    for (i = lowIndex; i < highIndex; i++) {
      values.add(IntConstant.getInstance(i));
    }
    if (i == highIndex) {
      values.add(IntConstant.getInstance(i));
    }
  }

  /** Constructs a new JSwitchStmt. lookupValues should be a list of IntConst s. */
  public JSwitchStmt(
      @Nonnull Immediate key,
      @Nonnull List<IntConstant> lookupValues,
      @Nonnull StmtPositionInfo positionInfo) {
    this(false, positionInfo, key);
    values = Collections.unmodifiableList(new ArrayList<>(lookupValues));
  }

  public boolean isTableSwitch() {
    return isTableSwitch;
  }

  @Nonnull
  public Optional<Stmt> getDefaultTarget(Body body) {
    return Optional.ofNullable(body.getBranchTargetsOf(this).get(values.size()));
  }

  public Immediate getKey() {
    return key;
  }

  @Override
  @Nonnull
  public List<Value> getUses() {
    final List<Value> uses = key.getUses();
    List<Value> list = new ArrayList<>(uses.size() + 1);
    list.addAll(uses);
    list.add(getKey());
    return list;
  }

  @Override
  public boolean fallsThrough() {
    return false;
  }

  @Override
  public boolean branches() {
    return true;
  }

  @Override
  public void accept(@Nonnull StmtVisitor sw) {
    sw.caseSwitchStmt(this);
  }

  /** Amount of labels +1 for default handler */
  public int getValueCount() {
    return values.size() + 1;
  }

  public int getValue(int index) {
    return values.get(index).getValue();
  }

  @Nonnull
  public List<IntConstant> getValues() {
    return Collections.unmodifiableList(values);
  }

  @Override
  @Nonnull
  public List<Stmt> getTargetStmts(Body body) {
    return body.getBranchTargetsOf(this);
  }

  @Override
  public int getSuccessorCount() {
    return getValueCount();
  }

  @Override
  public boolean equivTo(@Nonnull Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseSwitchStmt(this, o);
  }

  @Override
  public int equivHashCode() {
    return Objects.hash(getValues());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append(Jimple.SWITCH)
        .append('(')
        .append(getKey())
        .append(')')
        .append(' ')
        .append('{')
        .append(" ");

    for (IntConstant value : values) {
      sb.append("    ").append(Jimple.CASE).append(' ').append(value).append(": ");
    }

    sb.append("    ").append(Jimple.DEFAULT).append(": ");
    sb.append(' ').append('}');

    return sb.toString();
  }

  @Override
  public void toString(@Nonnull StmtPrinter stmtPrinter) {
    stmtPrinter.literal(Jimple.SWITCH);
    stmtPrinter.literal("(");
    getKey().toString(stmtPrinter);
    stmtPrinter.literal(")");
    stmtPrinter.newline();
    stmtPrinter.incIndent();
    stmtPrinter.handleIndent();
    stmtPrinter.literal("{");
    stmtPrinter.newline();

    final Iterable<Stmt> targets = stmtPrinter.getBody().getBranchTargetsOf(this);
    Iterator<Stmt> targetIt = targets.iterator();
    for (IntConstant value : values) {
      stmtPrinter.handleIndent();
      stmtPrinter.literal(Jimple.CASE);
      stmtPrinter.literal(" ");
      stmtPrinter.constant(value);
      stmtPrinter.literal(": ");
      stmtPrinter.literal(Jimple.GOTO);
      stmtPrinter.literal(" ");
      stmtPrinter.stmtRef(targetIt.next(), true);
      stmtPrinter.literal(";");

      stmtPrinter.newline();
    }
    Stmt defaultTarget = targetIt.next();
    stmtPrinter.handleIndent();
    stmtPrinter.literal(Jimple.DEFAULT);
    stmtPrinter.literal(": ");
    stmtPrinter.literal(Jimple.GOTO);
    stmtPrinter.literal(" ");
    stmtPrinter.stmtRef(defaultTarget, true);
    stmtPrinter.literal(";");

    stmtPrinter.decIndent();
    stmtPrinter.newline();
    stmtPrinter.handleIndent();
    stmtPrinter.literal("}");
  }

  @Nonnull
  public JSwitchStmt withKey(@Nonnull Immediate key) {
    return new JSwitchStmt(key, getValues(), getPositionInfo());
  }

  @Nonnull
  public JSwitchStmt withValues(@Nonnull List<IntConstant> values) {
    return new JSwitchStmt(getKey(), values, getPositionInfo());
  }

  @Nonnull
  public JSwitchStmt withPositionInfo(@Nonnull StmtPositionInfo positionInfo) {
    return new JSwitchStmt(getKey(), getValues(), positionInfo);
  }
}
