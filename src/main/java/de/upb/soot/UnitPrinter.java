package de.upb.soot;

import de.upb.soot.core.SootFieldRef;
import de.upb.soot.core.SootMethodRef;
import de.upb.soot.jimple.Local;
import de.upb.soot.jimple.ValueBox;
import de.upb.soot.jimple.constant.Constant;
import de.upb.soot.jimple.ref.IdentityRef;
import de.upb.soot.jimple.stmt.Unit;
import de.upb.soot.jimple.stmt.UnitBox;
import de.upb.soot.jimple.type.Type;

/**
 * Interface for different methods of printing out a Unit.
 */
public interface UnitPrinter {
  public void startUnit(Unit u);

  public void endUnit(Unit u);

  public void startUnitBox(UnitBox u);

  public void endUnitBox(UnitBox u);

  public void startValueBox(ValueBox u);

  public void endValueBox(ValueBox u);

  public void incIndent();

  public void decIndent();

  public void noIndent();

  public void setIndent(String newIndent);

  public String getIndent();

  public void literal(String s);

  public void newline();

  public void local(Local l);

  public void type(Type t);

  public void methodRef(SootMethodRef m);

  public void constant(Constant c);

  public void fieldRef(SootFieldRef f);

  public void unitRef(Unit u, boolean branchTarget);

  public void identityRef(IdentityRef r);

  public void setPositionTagger(AttributesUnitPrinter pt);

  public AttributesUnitPrinter getPositionTagger();

  public StringBuffer output();
}

