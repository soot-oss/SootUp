package de.upb.soot;

import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.StmtBox;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.constant.Constant;
import de.upb.soot.jimple.common.ref.IdentityRef;
import de.upb.soot.jimple.common.stmt.Stmt;
import de.upb.soot.jimple.common.type.Type;

/**
 * Interface for different methods of printing out a Unit.
 */
public interface StmtPrinter {
  public void startUnit(Stmt u);

  public void endUnit(Stmt u);

  public void startUnitBox(StmtBox u);

  public void endUnitBox(StmtBox u);

  public void startValueBox(ValueBox u);

  public void endValueBox(ValueBox u);

  public void incIndent();

  public void decIndent();

  public void noIndent();

  public void setIndent(String newIndent);

  public String getIndent();

  public void literal(String s);

  public void newline();

  public void local(Local jimpleLocal);

  public void type(Type t);

  public void method(SootMethod m);

  public void constant(Constant c);

  public void field(SootField f);

  public void unitRef(Stmt u, boolean branchTarget);

  public void identityRef(IdentityRef r);

  public void setPositionTagger(AttributesUnitPrinter pt);

  public AttributesUnitPrinter getPositionTagger();

  public StringBuffer output();

  public void fieldRef(SootField field);
}
