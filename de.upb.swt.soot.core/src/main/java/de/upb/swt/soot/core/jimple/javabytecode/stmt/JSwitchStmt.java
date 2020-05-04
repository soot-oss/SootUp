package de.upb.swt.soot.core.jimple.javabytecode.stmt;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.stmt.BranchingStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

/*
 * Switch Statements (combining LookupSwitch/TableSwitch)
 * @author Markus Schmidt
 */
public class JSwitchStmt extends Stmt implements Copyable, BranchingStmt {

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

  public Stmt getDefaTultTarget(Body body) {
    return body.getBranchTargets(this).get(0);
  }

  public Immediate getKey() {
    return key;
  }

  @Override
  @Nonnull
  public List<Value> getUses() {
    final List<Value> uses = key.getUses();
    List<Value> list = new ArrayList<>(uses.size() + 1);
    list.add(key);
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
  public void accept(@Nonnull Visitor sw) {
    ((StmtVisitor) sw).caseSwitchStmt(this);
  }

  public int getValueCount() {
    return values.size();
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
    return body.getBranchTargets(this);
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

    /* TODO [ms] leftover
    for (int i = 0; i < values.size(); i++) {
      Stmt target = getTarget(i);
      sb.append("    ")
          .append(Jimple.CASE)
          .append(' ')
          .append(values.get(i))
          .append(": ")
          .append(Jimple.GOTO)
          .append(' ')
          .append(target == this ? "self" : target)
          .append(';')
          .append(' ');
    }

    Stmt target = getDefaultTarget();
    sb.append("    " + Jimple.DEFAULT + ": " + Jimple.GOTO + " ")
        .append(target == this ? "self" : target)
        .append(';');
     */
    sb.append(' ').append('}');

    return sb.toString();
  }

  @Override
  public void toString(@Nonnull StmtPrinter up) {
    up.literal(Jimple.SWITCH);
    up.literal("(");
    getKey().toString(up);
    up.literal(")");
    up.newline();
    up.incIndent();
    up.handleIndent();
    up.literal("{");
    up.newline();
    final int size = values.size();
    for (int i = 0; i < size; i++) {
      up.handleIndent();
      up.literal(Jimple.CASE);
      up.literal(" ");
      up.constant(values.get(i));
      up.literal(": ");
      /* TODO: [ms] leftover
      up.literal(Jimple.GOTO);
      up.literal(" ");
      up.stmtRef(getTarget(i), true);
      up.literal(";");
      */
      up.newline();
    }

    /* TODO: [ms] leftover
    up.handleIndent();
    up.literal(Jimple.DEFAULT);
    up.literal(": ");
    up.literal(Jimple.GOTO);
    up.literal(" ");
    up.stmtRef(getDefaultTarget(), true);
    up.literal(";");
       */
    up.decIndent();
    up.newline();
    up.handleIndent();
    up.literal("}");
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
