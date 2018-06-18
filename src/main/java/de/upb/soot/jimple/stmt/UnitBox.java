package de.upb.soot.jimple.stmt;

import de.upb.soot.UnitPrinter;

public interface UnitBox {

  Unit getUnit();

  void setUnit(Unit target);

  void toString(UnitPrinter up);

  boolean isBranchTarget();

}
