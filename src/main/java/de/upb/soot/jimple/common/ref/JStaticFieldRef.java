package de.upb.soot.jimple.common.ref;

import de.upb.soot.jimple.basic.JimpleComparator;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.visitor.Visitor;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.util.Copyable;
import de.upb.soot.util.printer.StmtPrinter;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public final class JStaticFieldRef extends FieldRef implements Copyable {
  /** */
  private static final long serialVersionUID = -8744248848897714882L;

  public JStaticFieldRef(FieldSignature fieldSig) {
    super(fieldSig);
  }

  @Override
  public String toString() {
    return getFieldSignature().toString();
  }

  @Override
  public void toString(StmtPrinter up) {
    up.fieldSignature(getFieldSignature());
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
  public void accept(Visitor v) {
    // TODO Auto-generated methodRef stub
  }

  @Nonnull
  public JStaticFieldRef withFieldSignature(FieldSignature fieldSig) {
    return new JStaticFieldRef(fieldSig);
  }
}
