package de.upb.soot.jimple.common.ref;

import de.upb.soot.core.SootField;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.util.printer.IStmtPrinter;

import java.util.Collections;
import java.util.List;

public class JStaticFieldRef implements FieldRef {
  /**
   * 
   */
  private static final long serialVersionUID = -8744248848897714882L;
  protected SootField field;

  public JStaticFieldRef(SootField field) {
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
    return field.toString();
  }

  @Override
  public void toString(IStmtPrinter up) {
    up.field(field);
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
    return field;
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
