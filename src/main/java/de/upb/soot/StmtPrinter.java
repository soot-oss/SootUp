package de.upb.soot;

import de.upb.soot.jimple.Local;
import de.upb.soot.jimple.StmtBox;
import de.upb.soot.jimple.ValueBox;
import de.upb.soot.jimple.common.constant.Constant;
import de.upb.soot.jimple.common.ref.IdentityRef;
import de.upb.soot.jimple.common.ref.SootFieldRef;
import de.upb.soot.jimple.common.ref.SootMethodRef;
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

  public void local(Local l);

  public void type(Type t);

  public void methodRef(SootMethodRef m);

  public void constant(Constant c);

  public void fieldRef(SootFieldRef f);

  public void unitRef(Stmt u, boolean branchTarget);

  public void identityRef(IdentityRef r);

  public void setPositionTagger(AttributesUnitPrinter pt);

  public AttributesUnitPrinter getPositionTagger();

  public StringBuffer output();
}
