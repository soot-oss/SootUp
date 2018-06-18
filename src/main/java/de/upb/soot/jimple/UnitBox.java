package de.upb.soot.jimple;

import de.upb.soot.UnitPrinter;
import de.upb.soot.core.Unit;

public interface UnitBox {

  Unit getUnit();

  void setUnit(Unit target);

  void toString(UnitPrinter up);

  boolean isBranchTarget();

}
