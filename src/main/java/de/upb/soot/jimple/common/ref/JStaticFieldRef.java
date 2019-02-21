package de.upb.soot.jimple.common.ref;

import java.util.Collections;
import java.util.List;

import de.upb.soot.jimple.basic.JimpleComparator;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.util.printer.IStmtPrinter;
import de.upb.soot.views.IView;

public class JStaticFieldRef extends FieldRef {
  /**
   * 
   */
  private static final long serialVersionUID = -8744248848897714882L;


  public JStaticFieldRef(IView view, FieldSignature fieldSig) {
    super(view, fieldSig);
  }

  @Override
  public Object clone() {
    return new JStaticFieldRef(this.view, fieldSignature);
  }

  @Override
  public String toString() {
    return fieldSignature.toString();
  }

  @Override
  public void toString(IStmtPrinter up) {
    up.fieldSignature(fieldSignature);
  }

  @Override
  public List<ValueBox> getUseBoxes() {
    return Collections.emptyList();
  }

  @Override
  public boolean equivTo(Object o) {
    return JimpleComparator.getInstance().caseStaticFieldRef(this, o);
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseStaticFieldRef(this, o);
  }

  @Override
  public int equivHashCode() {
    if (getField().isPresent()) {
      return getField().get().equivHashCode() * 23;
    } else {
      return 22;
    }
  }

  @Override
  public void accept(IVisitor v) {
    // TODO Auto-generated methodRef stub
  }

}
