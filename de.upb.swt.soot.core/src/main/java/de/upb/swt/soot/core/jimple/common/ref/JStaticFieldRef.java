package de.upb.swt.soot.core.jimple.common.ref;

import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public final class JStaticFieldRef extends FieldRef implements Copyable {

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
  public List<Value> getUses() {
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
