package de.upb.soot.core;

import de.upb.soot.UnitPrinter;

public interface UnitBox {

  Unit getUnit();

  void setUnit(Unit target);

  void toString(UnitPrinter up);

  boolean isBranchTarget();

}
