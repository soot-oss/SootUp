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
import java.util.*;
import javax.annotation.Nonnull;

/*
 * Switch Statements (combining LookupSwitch/TableSwitch)
 * @author Markus Schmidt
 */
public class JSwitchStmt extends BranchingStmt implements Copyable {

  private final ValueBox keyBox;
  private List<IntConstant> values;
  private final boolean isTableSwitch;

  private JSwitchStmt(
      boolean isTableSwitch, @Nonnull StmtPositionInfo positionInfo, @Nonnull ValueBox keyBox) {
    super(positionInfo);
    this.isTableSwitch = isTableSwitch;
    this.keyBox = keyBox;
  }

  public JSwitchStmt(
      @Nonnull Value key, int lowIndex, int highIndex, @Nonnull StmtPositionInfo positionInfo) {
    this(Jimple.newImmediateBox(key), lowIndex, highIndex, positionInfo);
  }

  public JSwitchStmt(
      @Nonnull ValueBox keyBox,
      int lowIndex,
      int highIndex,
      @Nonnull StmtPositionInfo positionInfo) {
    this(true, positionInfo, keyBox);

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
      @Nonnull ValueBox keyBox,
      @Nonnull List<IntConstant> lookupValues,
      @Nonnull StmtPositionInfo positionInfo) {
    this(false, positionInfo, keyBox);
    values = Collections.unmodifiableList(new ArrayList<>(lookupValues));
  }

  public JSwitchStmt(
      @Nonnull Value key,
      @Nonnull List<IntConstant> lookupValues,
      @Nonnull StmtPositionInfo positionInfo) {
    this(Jimple.newImmediateBox(key), lookupValues, positionInfo);
  }

  public boolean isTableSwitch() {
    return isTableSwitch;
  }

  @Nonnull
  public Optional<Stmt> getDefaultTarget(Body body) {
    return Optional.ofNullable(body.getBranchTargetsOf(this).get(values.size()));
  }

  public Value getKey() {
    return keyBox.getValue();
  }

  public final ValueBox getKeyBox() {
    return keyBox;
  }

  @Override
  @Nonnull
  public List<Value> getUses() {
    final List<Value> uses = getKey().getUses();
    List<Value> list = new ArrayList<>(uses.size() + 1);
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
  public void accept(@Nonnull Visitor sw) {
    ((StmtVisitor) sw).caseSwitchStmt(this);
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
