package de.upb.soot.jimple.common.ref;

import de.upb.soot.jimple.basic.JimpleComparator;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.util.printer.IStmtPrinter;
import java.util.Collections;
import java.util.List;

public class JStaticFieldRef extends FieldRef {
  /** */
  private static final long serialVersionUID = -8744248848897714882L;

  public JStaticFieldRef(FieldSignature fieldSig) {
    super(fieldSig);
  }

  @Override
  public Object clone() {
    return new JStaticFieldRef(fieldSignature);
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
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseStaticFieldRef(this, o);
  }

  @Override
  public int equivHashCode() {
    return getFieldSignature().hashCode() * 23;
  }

  @Override
  public void accept(IVisitor v) {
    // TODO Auto-generated methodRef stub
  }
}
