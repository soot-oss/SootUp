package de.upb.swt.soot.core.jimple.javabytecode.stmt;

import akka.util.Switch;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractSwitchStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Handles lookup- / table-switch statements
 */
public final class JSwitchStmt extends AbstractSwitchStmt implements Copyable {

  enum SwitchType {
    JLookupSwitchStmt,
    JTableSwitchStmt
  }

  private final SwitchType switchType;

  // LookupSwitchStmt related
  // List of lookup values from the corresponding bytecode instruction, represented as IntConstants.
  private List<IntConstant> lookupValues;

   // TableSwitchStmt related
  private int lowIndex;
  private int highIndex;

  // This method is necessary to deal with constructor-must-be-first-ism.
  private static StmtBox[] getTargetBoxesArray(List<? extends Stmt> targets) {
    StmtBox[] targetBoxes = new StmtBox[targets.size()];
    for (int i = 0; i < targetBoxes.length; i++) {
      targetBoxes[i] = Jimple.newStmtBox(targets.get(i));
    }
    return targetBoxes;
  }

  /* Constructors with a TableSwitch Signature */
  public JSwitchStmt(
          Value key,
          int lowIndex,
          int highIndex,
          List<? extends Stmt> targets,
          Stmt defaultTarget,
          PositionInfo positionInfo) {
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
          PositionInfo positionInfo) {
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
          PositionInfo positionInfo) {
    super(positionInfo, keyBox, defaultTargetBox, targetBoxes);

    if (lowIndex > highIndex) {
      throw new RuntimeException(
              "Error creating tableswitch: lowIndex("
                      + lowIndex
                      + ") can't be greater than highIndex("
                      + highIndex
                      + ").");
    }
    this.switchType = SwitchType.JTableSwitchStmt;
    this.lowIndex = lowIndex;
    this.highIndex = highIndex;
  }

  /* Constructors with a LookupSwitch Signature */

  /** Constructs a new JSwitchStmt. lookupValues should be a list of IntConst s. */
  public JSwitchStmt(
          Value key,
          List< IntConstant > lookupValues,
          List<? extends Stmt> targets,
          Stmt defaultTarget,
          PositionInfo positionInfo) {
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
          PositionInfo positionInfo) {
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
          PositionInfo positionInfo) {
    super(positionInfo, keyBox, defaultTargetBox, targetBoxes);
    this.switchType = SwitchType.JLookupSwitchStmt;
    this.lookupValues = Collections.unmodifiableList(new ArrayList<>(lookupValues));
  }

  private String getJimpleSwitchStmtStringForType(){
    String jSwitchTypeString = (this.switchType == SwitchType.JLookupSwitchStmt)
            ? Jimple.LOOKUPSWITCH : Jimple.TABLESWITCH;
    return jSwitchTypeString;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    String endOfLine = " ";

    builder
            .append(this.getJimpleSwitchStmtStringForType() + "(")
            .append(getKey().toString())
            .append(")")
            .append(endOfLine);

    builder.append("{").append(endOfLine);

    if (this.switchType == SwitchType.JTableSwitchStmt) {
      // TableSwitchStmt handling

      // In this for-loop, we cannot use "<=" since 'i' would wrap around.
      // The case for "i == highIndex" is handled separately after the loop.
      for (int i = lowIndex; i < highIndex; i++) {
        Stmt target = getTarget(i - lowIndex);
        builder
                .append("    " + Jimple.CASE + " ")
                .append(i)
                .append(": ")
                .append(Jimple.GOTO)
                .append(" ")
                .append(target == this ? "self" : target)
                .append(";")
                .append(endOfLine);
      }

      Stmt target = getTarget(highIndex - lowIndex);
      builder
              .append("    " + Jimple.CASE + " ")
              .append(highIndex)
              .append(": ")
              .append(Jimple.GOTO)
              .append(" ")
              .append(target == this ? "self" : target)
              .append(";")
              .append(endOfLine);
    } else {
      // LookupSwitchStmt handling

      for (int i = 0; i < lookupValues.size(); i++) {
        Stmt target = getTarget(i);
        builder
                .append("    " + Jimple.CASE + " ")
                .append(lookupValues.get(i))
                .append(": ")
                .append(Jimple.GOTO)
                .append(" ")
                .append(target == this ? "self" : target)
                .append(";")
                .append(endOfLine);
      }
    }

    Stmt target = getDefaultTarget();
    builder
            .append("    " + Jimple.DEFAULT + ": " + Jimple.GOTO + " ")
            .append(target == this ? "self" : target)
            .append(";")
            .append(endOfLine);

    builder.append("}");

    return builder.toString();
  }

  @Override
  public void toString(StmtPrinter up) {
    up.literal(this.getJimpleSwitchStmtStringForType());
    up.literal("(");
    getKeyBox().toString(up);
    up.literal(")");
    up.newline();
    up.literal("{");
    up.newline();

    if (this.switchType == SwitchType.JTableSwitchStmt) {
      // TableSwitchStmt handling

      // In this for-loop, we cannot use "<=" since 'i' would wrap around.
      // The case for "i == highIndex" is handled separately after the loop.
      for (int i = lowIndex; i < highIndex; i++) {
        printCaseTarget(up, i);
      }
      printCaseTarget(up, highIndex);

    } else {
      // LookupSwitchStmt handling
      final int size = lookupValues.size();
      for (int i = 0; i < size; i++) {
        up.literal("    ");
        up.literal(Jimple.CASE);
        up.literal(" ");
        up.constant(lookupValues.get(i));
        up.literal(": ");
        up.literal(Jimple.GOTO);
        up.literal(" ");
        getTargetBox(i).toString(up);
        up.literal(";");
        up.newline();
      }
    }

      up.literal("    ");
      up.literal(Jimple.DEFAULT);
      up.literal(": ");
      up.literal(Jimple.GOTO);
      up.literal(" ");
      getDefaultTargetBox().toString(up);
      up.literal(";");
      up.newline();
      up.literal("}");
  }

  private void printCaseTarget(StmtPrinter up, int targetIndex) {
    up.literal("    ");
    up.literal(Jimple.CASE);
    up.literal(" ");
    up.literal(Integer.toString(targetIndex));
    up.literal(": ");
    up.literal(Jimple.GOTO);
    up.literal(" ");
    getTargetBox(targetIndex - lowIndex).toString(up);
    up.literal(";");
    up.newline();
  }

  // region Former TableSwitchStmt methods
  public int getLowIndex() {
    return lowIndex;
  }

  public int getHighIndex() {
    return highIndex;
  }

  @Override
  public void accept(Visitor sw) {
    ((StmtVisitor) sw).caseTableSwitchStmt(this);
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseTableSwitchStmt(this, o);
  }

  @Override
  public int equivHashCode() {

    if(this.switchType == SwitchType.JTableSwitchStmt) {
      int prime = 31;
      int ret = prime * lowIndex;
      ret = prime * ret + highIndex;
      ret = prime * ret + super.equivHashCode();
      return ret;
    } else {
      int res = super.equivHashCode();
      int prime = 31;

      for (IntConstant lv : lookupValues) {
        res = res * prime + lv.equivHashCode();
      }
      return res;
    }
  }

  @Nonnull
  public JSwitchStmt withKey(Value key) {
    if(this.switchType == SwitchType.JTableSwitchStmt) {
      return new JSwitchStmt(
              key,
              lowIndex,
              highIndex,
              getTargets(),
              getDefaultTarget(),
              getPositionInfo());
    } else {
      return new JSwitchStmt(
              key,
              lookupValues,
              getTargets(),
              getDefaultTarget(),
              getPositionInfo());
    }
  }

  @Nonnull
  public JSwitchStmt withLowIndex(int lowIndex) {
    return new JSwitchStmt(
            getKey(),
            lowIndex,
            highIndex,
            getTargets(),
            getDefaultTarget(),
            getPositionInfo());
  }

  @Nonnull
  public JSwitchStmt withHighIndex(int highIndex) {
    return new JSwitchStmt(
            getKey(),
            lowIndex,
            highIndex,
            getTargets(),
            getDefaultTarget(),
            getPositionInfo());
  }

  @Nonnull
  public JSwitchStmt withTargets(List<? extends Stmt> targets) {
    if(this.switchType == SwitchType.JTableSwitchStmt) {
      return new JSwitchStmt(
              getKey(),
              lowIndex,
              highIndex,
              targets,
              getDefaultTarget(),
              getPositionInfo());
    } else {
      return new JSwitchStmt(
              getKey(),
              lookupValues,
              targets,
              getDefaultTarget(),
              getPositionInfo());
    }
  }

  @Nonnull
  public JSwitchStmt withDefaultTarget(Stmt defaultTarget) {
    if(this.switchType == SwitchType.JTableSwitchStmt) {
      return new JSwitchStmt(
              getKey(), lowIndex, highIndex, getTargets(), defaultTarget, getPositionInfo());
    } else {
      return new JSwitchStmt(
              getKey(), lookupValues, getTargets(), defaultTarget, getPositionInfo());
    }
  }

  @Nonnull
  public JSwitchStmt withPositionInfo(PositionInfo positionInfo) {


    if(this.switchType == SwitchType.JTableSwitchStmt) {
      return new JSwitchStmt(
              getKey(), lowIndex, highIndex, getTargets(), getDefaultTarget(), positionInfo);
    } else {

      return new JSwitchStmt(
              getKey(), lookupValues, getTargets(), getDefaultTarget(), positionInfo);
    }
  }
  // endregion

  // region Former LookupSwitchStmt methods
  public int getLookupValueCount() {
    return lookupValues.size();
  }

  public int getLookupValue(int index) {
    return lookupValues.get(index).getValue();
  }

  public List<IntConstant> getLookupValues() {
    return Collections.unmodifiableList(lookupValues);
  }

  @Override
  public void accept(Visitor sw) {
    ((StmtVisitor) sw).caseLookupSwitchStmt(this);
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseLookupSwitchStmt(this, o);
  }

  // endregion

}
