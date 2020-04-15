package de.upb.swt.soot.core.jimple.javabytecode.stmt;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.common.stmt.StmtHandler;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
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
public final class JSwitchStmt extends AbstractStmt implements Copyable {

  private final Stmt defaultTarget;
  private final Value key;
  private final List<Stmt> stmts;
  private final Stmt[] targets;
  private List<IntConstant> values;
  private final boolean isTableSwitch;

  private JSwitchStmt(
      boolean isTableSwitch,
      StmtPositionInfo positionInfo,
      Value key,
      Stmt defaultTarget,
      Stmt... targets) {
    super(positionInfo);
    this.isTableSwitch = isTableSwitch;
    if (key == null) {
      throw new IllegalArgumentException("value may not be null");
    }
    if (key instanceof Immediate) {
      this.key = key;
    } else {
      throw new RuntimeException(
          "JSwitchStmt " + this + " cannot contain value: " + key + " (" + key.getClass() + ")");
    }

    this.defaultTarget = defaultTarget;
    this.targets = targets;

    List<Stmt> list = new ArrayList<>();
    stmts = Collections.unmodifiableList(list);

    Collections.addAll(list, targets);
    list.add(defaultTarget);
  }

  private JSwitchStmt(
      Value key,
      int lowIndex,
      int highIndex,
      Stmt[] targets,
      Stmt defaultTarget,
      StmtPositionInfo positionInfo) {
    this(true, positionInfo, key, defaultTarget, targets);

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

  public JSwitchStmt(
      Value key,
      int lowIndex,
      int highIndex,
      List<? extends Stmt> targets,
      Stmt defaultTarget,
      StmtPositionInfo positionInfo) {
    this(key, lowIndex, highIndex, getTargetsArray(targets), defaultTarget, positionInfo);
  }

  /** Constructs a new JSwitchStmt. lookupValues should be a list of IntConst s. */
  public JSwitchStmt(
      Value key,
      List<IntConstant> lookupValues,
      List<? extends Stmt> targets,
      Stmt defaultTarget,
      StmtPositionInfo positionInfo) {
    this(key, lookupValues, getTargetsArray(targets), defaultTarget, positionInfo);
  }

  private JSwitchStmt(
      Value key,
      List<IntConstant> lookupValues,
      Stmt[] targets,
      Stmt defaultTarget,
      StmtPositionInfo positionInfo) {
    this(false, positionInfo, key, defaultTarget, targets);
    values = Collections.unmodifiableList(new ArrayList<>(lookupValues));
  }

  public boolean isTableSwitch() {
    return isTableSwitch;
  }

  private static Stmt[] getTargetsArray(List<? extends Stmt> targets) {
    Stmt[] targetsArray = new Stmt[targets.size()];
    for (int i = 0; i < targetsArray.length; i++) {
      targetsArray[i] = targets.get(i);
    }
    return targetsArray;
  }

  public final Stmt getDefaultTarget() {
    return defaultTarget;
  }

  @Deprecated
  private void setDefaultTarget(Stmt newDefaultTarget) {
    StmtHandler stmtHandler = new StmtHandler(defaultTarget);
    StmtHandler.$Accessor.setStmt(stmtHandler, newDefaultTarget);
  }

  public final Value getKey() {
    return key;
  }

  @Override
  public final List<Value> getUses() {
    List<Value> list = new ArrayList<>(key.getUses());
    list.add(key);
    return list;
  }

  public final int getTargetCount() {
    return targets.length;
  }

  // This method is necessary to deal with constructor-must-be-first-ism.

  public final Stmt getTarget(int index) {
    return targets[index];
  }

  /** Returns a list targets of type Stmt. */
  public final List<Stmt> getTargets() {
    List<Stmt> targets = new ArrayList<>();

    for (Stmt element : targets) {
      targets.add(element);
    }

    return targets;
  }

  /**
   * Violates immutability. Only use in legacy code. Sets the setStmt box for targetBoxes array.
   *
   * @param targets A list of type Stmt.
   */
  @Deprecated
  private void setTargets(List<? extends Stmt> targets) {

    for (int i = 0; i < targets.size(); i++) {
      StmtHandler stmtHandler = new StmtHandler(this.targets[i]);
      StmtHandler.$Accessor.setStmt(stmtHandler, targets.get(i));
    }
  }

  /* Constructors with a LookupSwitch Signature */
  @Override
  public final List<Stmt> getStmts() {
    return stmts;
  }

  @Override
  public final boolean fallsThrough() {
    return false;
  }

  @Override
  public final boolean branches() {
    return true;
  }

  @Override
  public void accept(Visitor sw) {
    ((StmtVisitor) sw).caseSwitchStmt(this);
  }

  public int getValueCount() {
    return values.size();
  }

  public int getValue(int index) {
    return values.get(index).getValue();
  }

  public List<IntConstant> getValues() {
    return Collections.unmodifiableList(values);
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseSwitchStmt(this, o);
  }

  @Override
  public int equivHashCode() {
    return Objects.hash(getValues(), getTargets(), getDefaultTarget());
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
        .append(';')
        .append(' ')
        .append('}');

    return sb.toString();
  }

  @Override
  public void toString(StmtPrinter up) {
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
      up.literal(Jimple.GOTO);
      up.literal(" ");
      getTarget(i).toString(up);
      up.literal(";");
      up.newline();
    }

    up.handleIndent();
    up.literal(Jimple.DEFAULT);
    up.literal(": ");
    up.literal(Jimple.GOTO);
    up.literal(" ");
    getDefaultTarget().toString(up);
    up.literal(";");
    up.decIndent();
    up.newline();
    up.handleIndent();
    up.literal("}");
  }

  @Nonnull
  public JSwitchStmt withKey(Value key) {
    return new JSwitchStmt(key, getValues(), getTargets(), getDefaultTarget(), getPositionInfo());
  }

  @Nonnull
  public JSwitchStmt withTargets(List<? extends Stmt> targets) {
    return new JSwitchStmt(getKey(), getValues(), targets, getDefaultTarget(), getPositionInfo());
  }

  @Nonnull
  public JSwitchStmt withDefaultTarget(Stmt defaultTarget) {
    return new JSwitchStmt(getKey(), getValues(), getTargets(), defaultTarget, getPositionInfo());
  }

  @Nonnull
  public JSwitchStmt withPositionInfo(StmtPositionInfo positionInfo) {
    return new JSwitchStmt(getKey(), getValues(), getTargets(), getDefaultTarget(), positionInfo);
  }

  /** This class is for internal use only. It will be removed in the future. */
  @Deprecated
  public static class $Accessor {
    // This class deliberately starts with a $-sign to discourage usage
    // of this Soot implementation detail.
    private $Accessor() {}

    /** Violates immutability. Only use this for legacy code. */
    @Deprecated
    public static void setTargets(JSwitchStmt stmt, List<? extends Stmt> targets) {
      stmt.setTargets(targets);
    }

    /** Violates immutability. Only use this for legacy code. */
    @Deprecated
    public static void setDefaultTarget(JSwitchStmt stmt, Stmt defaultTarget) {
      stmt.setDefaultTarget(defaultTarget);
    }
  }
}
