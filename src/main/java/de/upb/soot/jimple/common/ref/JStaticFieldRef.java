package de.upb.soot.jimple.common.ref;

import de.upb.soot.StmtPrinter;
import de.upb.soot.core.SootField;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.visitor.IVisitor;

import java.util.Collections;
import java.util.List;

public class JStaticFieldRef implements FieldRef {
  protected SootField field;

  protected JStaticFieldRef(SootField field) {
    if (!field.isStatic()) {
      throw new RuntimeException("wrong static-ness");
    }
    this.field = field;
  }

  @Override
  public Object clone() {
    return new JStaticFieldRef(field);
  }

  @Override
  public String toString() {
    return field.getSignature();
  }

  @Override
  public void toString(StmtPrinter up) {
    up.fieldRef(field);
  }

  @Override
  public SootField getFieldRef() {
    return field;
  }

  @Override
  public void setFieldRef(SootField field) {
    this.field = field;
  }

  @Override
  public SootField getField() {
    return field.resolve();
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
    return getField().equivHashCode();
  }

  @Override
  public Type getType() {
    return field.getType();
  }

  @Override
  public void accept(IVisitor v) {
    // TODO Auto-generated method stub

  }

}
