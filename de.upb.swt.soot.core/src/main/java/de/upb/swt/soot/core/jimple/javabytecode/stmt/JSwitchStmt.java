package de.upb.swt.soot.core.jimple.javabytecode.stmt;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractStmt;
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
public final class JSwitchStmt extends AbstractStmt implements Copyable {

  private final StmtBox defaultTargetBox;
  private final ValueBox keyBox;
  private final List<StmtBox> stmtBoxes;
  private final StmtBox[] targetBoxes;
  private List<IntConstant> values;
  private final boolean isTableSwitch;

  // new attributes: later if ValueBox is deleted, then add "final" to it.
  private Value key;

  private JSwitchStmt(
      boolean isTableSwitch,
      StmtPositionInfo positionInfo,
      ValueBox keyBox,
      StmtBox defaultTargetBox,
      StmtBox... targetBoxes) {
    super(positionInfo);
    this.isTableSwitch = isTableSwitch;
    this.keyBox = keyBox;
    this.defaultTargetBox = defaultTargetBox;
    this.targetBoxes = targetBoxes;
    // new attributes: later if ValueBox is deleted, then fit the constructor.
    this.key = keyBox.getValue();

    // Build up stmtBoxes
    List<StmtBox> list = new ArrayList<>();
    stmtBoxes = Collections.unmodifiableList(list);

    Collections.addAll(list, targetBoxes);
    list.add(defaultTargetBox);
  }

  public JSwitchStmt(
      Value key,
      int lowIndex,
      int highIndex,
      List<? extends Stmt> targets,
      Stmt defaultTarget,
      StmtPositionInfo positionInfo) {
    this(
        Jimple.newImmediateBox(key),
        lowIndex,
        highIndex,
        getTargetBoxesArray(targets),
        Jimple.newStmtBox(defaultTarget),
        positionInfo);
  }

  public JSwitchStmt(
      Value key,
      int lowIndex,
      int highIndex,
      List<? extends StmtBox> targets,
      StmtBox defaultTarget,
      StmtPositionInfo positionInfo) {
    this(
        Jimple.newImmediateBox(key),
        lowIndex,
        highIndex,
        targets.toArray(new StmtBox[0]),
        defaultTarget,
        positionInfo);
  }

  private JSwitchStmt(
      ValueBox keyBox,
      int lowIndex,
      int highIndex,
      StmtBox[] targetBoxes,
      StmtBox defaultTargetBox,
      StmtPositionInfo positionInfo) {
    this(true, positionInfo, keyBox, defaultTargetBox, targetBoxes);

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
      Value key,
      List<IntConstant> lookupValues,
      List<? extends Stmt> targets,
      Stmt defaultTarget,
      StmtPositionInfo positionInfo) {
    this(
        Jimple.newImmediateBox(key),
        lookupValues,
        getTargetBoxesArray(targets),
        Jimple.newStmtBox(defaultTarget),
        positionInfo);
  }

  /** Constructs a new JSwitchStmt. lookupValues should be a list of IntConst s. */
  public JSwitchStmt(
      Value key,
      List<IntConstant> lookupValues,
      List<? extends StmtBox> targets,
      StmtBox defaultTarget,
      StmtPositionInfo positionInfo) {
    this(
        Jimple.newImmediateBox(key),
        lookupValues,
        targets.toArray(new StmtBox[0]),
        defaultTarget,
        positionInfo);
  }

  private JSwitchStmt(
      ValueBox keyBox,
      List<IntConstant> lookupValues,
      StmtBox[] targetBoxes,
      StmtBox defaultTargetBox,
      StmtPositionInfo positionInfo) {
    this(false, positionInfo, keyBox, defaultTargetBox, targetBoxes);
    values = Collections.unmodifiableList(new ArrayList<>(lookupValues));
  }

  public boolean isTableSwitch() {
    return isTableSwitch;
  }

  private static StmtBox[] getTargetBoxesArray(List<? extends Stmt> targets) {
    StmtBox[] targetBoxes = new StmtBox[targets.size()];
    for (int i = 0; i < targetBoxes.length; i++) {
      targetBoxes[i] = Jimple.newStmtBox(targets.get(i));
    }
    return targetBoxes;
  }

  public final Stmt getDefaultTarget() {
    return defaultTargetBox.getStmt();
  }

  @Deprecated
  private void setDefaultTarget(Stmt defaultTarget) {
    StmtBox.$Accessor.setStmt(defaultTargetBox, defaultTarget);
  }

  protected final StmtBox getDefaultTargetBox() {
    return defaultTargetBox;
  }

  public final Value getKey() {
    return keyBox.getValue();
  }

  public final ValueBox getKeyBox() {
    return keyBox;
  }

  @Override
  public final List<Value> getUses() {
    List<Value> list = new ArrayList<>(key.getUses());
    list.add(key);
    return list;
  }

  public final int getTargetCount() {
    return targetBoxes.length;
  }

  // This method is necessary to deal with constructor-must-be-first-ism.

  public final Stmt getTarget(int index) {
    return targetBoxes[index].getStmt();
  }

  /* Constructors with a TableSwitch Signature */
  protected final StmtBox getTargetBox(int index) {
    return targetBoxes[index];
  }

  /** Returns a list targets of type Stmt. */
  public final List<Stmt> getTargets() {
    List<Stmt> targets = new ArrayList<>();

    for (StmtBox element : targetBoxes) {
      targets.add(element.getStmt());
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
      StmtBox.$Accessor.setStmt(targetBoxes[i], targets.get(i));
    }
  }

  /* Constructors with a LookupSwitch Signature */
  @Override
  public final List<StmtBox> getStmtBoxes() {
    return stmtBoxes;
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
    getKeyBox().toString(up);
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
      getTargetBox(i).toString(up);
      up.literal(";");
      up.newline();
    }

    up.handleIndent();
    up.literal(Jimple.DEFAULT);
    up.literal(": ");
    up.literal(Jimple.GOTO);
    up.literal(" ");
    getDefaultTargetBox().toString(up);
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
