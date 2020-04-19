package de.upb.swt.soot.core.jimple.javabytecode.stmt;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
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
public class JSwitchStmt extends Stmt implements Copyable {

  private final Immediate key;
  private List<IntConstant> values;
  private List<Stmt> targets;
  private Stmt defaultTarget;
  private final boolean isTableSwitch;

  // ** concatenation of targets + defaultTarget */
  @Nonnull private final List<Stmt> stmts;

  private JSwitchStmt(
      @Nonnull boolean isTableSwitch,
      @Nonnull StmtPositionInfo positionInfo,
      @Nonnull Immediate key,
      @Nonnull Stmt defaultTarget,
      @Nonnull List<Stmt> targets) {
    super(positionInfo);
    this.isTableSwitch = isTableSwitch;
    this.key = key;
    this.defaultTarget = defaultTarget;
    this.targets = (targets); // ImmutableUtils.immutableListOf

    List<Stmt> list = new ArrayList<>(targets.size() + 1);
    list.addAll(targets);
    list.add(defaultTarget);
    stmts = (list); // Collections.unmodifiableList
  }

  public JSwitchStmt(
      @Nonnull Immediate key,
      @Nonnull int lowIndex,
      @Nonnull int highIndex,
      @Nonnull List<Stmt> targets,
      @Nonnull Stmt defaultTarget,
      @Nonnull StmtPositionInfo positionInfo) {
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

  /** Constructs a new JSwitchStmt. lookupValues should be a list of IntConst s. */
  public JSwitchStmt(
      @Nonnull Immediate key,
      @Nonnull List<IntConstant> lookupValues,
      @Nonnull List<Stmt> targets,
      @Nonnull Stmt defaultTarget,
      @Nonnull StmtPositionInfo positionInfo) {
    this(false, positionInfo, key, defaultTarget, targets);
    values = Collections.unmodifiableList(new ArrayList<>(lookupValues));
  }

  public boolean isTableSwitch() {
    return isTableSwitch;
  }

  public Stmt getDefaultTarget() {
    return defaultTarget;
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

  public int getTargetCount() {
    return targets.size();
  }

  // This method is necessary to deal with constructor-must-be-first-ism.

  public Stmt getTarget(@Nonnull int index) {
    return targets.get(index);
  }
  /** Returns a list targets of type Stmt. */
  public List<Stmt> getTargets() {
    return targets;
  }

  /**
   * Violates immutability. Only use in legacy code. Sets the setStmt box for targetBoxes array.
   *
   * @param newDefaultTarget Stmt.
   */
  @Deprecated
  private void setDefaultTarget(@Nonnull Stmt newDefaultTarget) {
    if (defaultTarget != null) {
      Stmt.$Accessor.removeStmtPointingToTarget(this, defaultTarget);
    }
    defaultTarget = newDefaultTarget;
    Stmt.$Accessor.addStmtPointingToTarget(this, newDefaultTarget);
  }

  /**
   * Violates immutability. Only use in legacy code. Sets the setStmt box for targetBoxes array.
   *
   * @param newTargets A list of type Stmt.
   */
  @Deprecated
  private void setTargets(List<Stmt> newTargets) {
    // cleanup old target links
    for (Stmt target : targets) {
      if (target == null) {
        continue;
      }
      Stmt.$Accessor.removeStmtPointingToTarget(this, target);
    }
    targets = newTargets;
    for (Stmt newTarget : newTargets) {
      Stmt.$Accessor.addStmtPointingToTarget(this, newTarget);
    }
  }

  @Override
  @Nonnull
  public List<Stmt> getStmts() {
    return stmts;
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

  public int getValue(@Nonnull int index) {
    return values.get(index).getValue();
  }

  @Nonnull
  public List<IntConstant> getValues() {
    return Collections.unmodifiableList(values);
  }

  @Override
  public boolean equivTo(@Nonnull Object o, @Nonnull JimpleComparator comparator) {
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
      up.literal(Jimple.GOTO);
      up.literal(" ");
      up.stmtRef(getTarget(i), true);
      up.literal(";");
      up.newline();
    }

    up.handleIndent();
    up.literal(Jimple.DEFAULT);
    up.literal(": ");
    up.literal(Jimple.GOTO);
    up.literal(" ");
    up.stmtRef(getDefaultTarget(), true);
    up.literal(";");
    up.decIndent();
    up.newline();
    up.handleIndent();
    up.literal("}");
  }

  @Nonnull
  public JSwitchStmt withKey(@Nonnull Immediate key) {
    return new JSwitchStmt(key, getValues(), getTargets(), getDefaultTarget(), getPositionInfo());
  }

  @Nonnull
  public JSwitchStmt withTargets(@Nonnull List<Stmt> targets) {
    return new JSwitchStmt(getKey(), getValues(), targets, getDefaultTarget(), getPositionInfo());
  }

  @Nonnull
  public JSwitchStmt withDefaultTarget(@Nonnull Stmt defaultTarget) {
    return new JSwitchStmt(getKey(), getValues(), getTargets(), defaultTarget, getPositionInfo());
  }

  @Nonnull
  public JSwitchStmt withPositionInfo(@Nonnull StmtPositionInfo positionInfo) {
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
    public static void setTargets(@Nonnull JSwitchStmt stmt, @Nonnull List<Stmt> targets) {
      stmt.setTargets(targets);
    }

    /** Violates immutability. Only use this for legacy code. */
    @Deprecated
    public static void setDefaultTarget(@Nonnull JSwitchStmt stmt, @Nonnull Stmt defaultTarget) {
      stmt.setDefaultTarget(defaultTarget);
    }
  }
}
