package de.upb.soot.jimple.common.ref;

import com.google.common.base.Optional;

import de.upb.soot.core.SootField;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.symbolicreferences.FieldRef;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.util.printer.IStmtPrinter;
import de.upb.soot.views.IView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JStaticFieldRef implements JFieldRef {
  /** */
  private static final long serialVersionUID = -8744248848897714882L;

  private IView view;
  private final FieldRef fieldSig;

  // FIXME: DO wo really need a view here?
  public JStaticFieldRef(IView view, FieldRef fieldSig) {
    this.fieldSig = fieldSig;
    this.view = view;
  }

  @Override
  public Object clone() {
    return new JStaticFieldRef(this.view, fieldSig);
  }

  @Override
  public String toString() {
    return fieldSig.toString();
  }

  @Override
  public void toString(IStmtPrinter up) {
    up.fieldSignature(fieldSig.getSignature());
  }

  @Override
  public com.google.common.base.Optional<SootField> getField() {
    return Optional.fromNullable(fieldSig.resolve());
  }

  @Override
  public FieldSignature getFieldSignature() {
    return fieldSig.getSignature();
  }

  @Override
  public List<ValueBox> getUseBoxes() {
    return Collections.emptyList();
  }

  @Override
  public boolean equivTo(Object o) {
    if (o instanceof JStaticFieldRef) {
      return ((JStaticFieldRef) o).getField().equals(getField());
    }

    return false;
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
  public Type getType() {
    return view.getType(fieldSig.getSignature().typeSignature);
  }

  @Override
  public void accept(IVisitor v) {
    // TODO Auto-generated methodRef stub
  }

  @Override
  public boolean equivTo(Object o, Comparator comparator) {
    return comparator.compare(this, o) == 0;
  }
}
