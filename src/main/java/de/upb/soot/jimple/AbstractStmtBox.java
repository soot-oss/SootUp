package de.upb.soot.jimple;

import de.upb.soot.StmtPrinter;
import de.upb.soot.jimple.common.stmt.Stmt;

public abstract class AbstractStmtBox implements StmtBox {
  protected Stmt unit;

  public abstract boolean canContainUnit(Stmt u);

  @Override
  public boolean isBranchTarget() {
    return true;
  }

  public void setUnit(Stmt unit) {
    if (!canContainUnit(unit)) {
      throw new RuntimeException("attempting to put invalid unit in UnitBox");
    }

    // Remove this from set of back pointers.
    if (this.unit != null) {
      this.unit.removeBoxPointingToThis(this);
    }

    // Perform link
    this.unit = unit;

    // Add this to back pointers
    if (this.unit != null) {
      this.unit.addBoxPointingToThis(this);
    }
  }

  public Stmt getUnit() {
    return unit;
  }

  @Override
  public void toString(StmtPrinter up) {
    up.startUnitBox(this);
    up.unitRef(unit, isBranchTarget());
    up.endUnitBox(this);
  }
}
