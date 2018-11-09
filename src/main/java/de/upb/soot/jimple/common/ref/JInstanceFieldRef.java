/*
 * @author Linghui Luo
 * @version 1.0
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package de.upb.soot.jimple.common.ref;

import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.IField;
import de.upb.soot.core.SootField;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.util.printer.IStmtPrinter;
import de.upb.soot.views.IView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class JInstanceFieldRef implements FieldRef {

  /**
   * 
   */
  private static final long serialVersionUID = 2900174317359676686L;

  private final FieldSignature fieldSig;
  private final ValueBox baseBox;
  private IView view;

  /**
   * Create a reference to a class' instance field.
   *
   * @param view
   *          the view
   * @param base
   *          the base value of the field
   * @param fieldSig
   *          the field sig
   */
  public JInstanceFieldRef(IView view, Value base, FieldSignature fieldSig) {
    ValueBox baseBox = Jimple.newLocalBox(base);
    this.baseBox = baseBox;
    this.fieldSig = fieldSig;
    this.view = view;
  }

  @Override
  public Object clone() {
    return new JInstanceFieldRef(this.view, Jimple.cloneIfNecessary(getBase()), fieldSig);
  }

  @Override
  public String toString() {
    return baseBox.getValue().toString() + "." + fieldSig.toString();
  }

  @Override
  public void toString(IStmtPrinter up) {
    baseBox.toString(up);
    up.literal(".");
    up.fieldSignature(fieldSig);
  }

  public Value getBase() {
    return baseBox.getValue();
  }

  public ValueBox getBaseBox() {
    return baseBox;
  }

  public void setBase(Value base) {
    baseBox.setValue(base);
  }

  @Override
  public Optional<SootField> getField() {
    Optional<AbstractClass> declClass = view.getClass(fieldSig.declClassSignature);
    if (declClass.isPresent()) {
      Optional<? extends IField> f = declClass.get().getField(fieldSig);
      return f.map(c -> (SootField) c);
    }
    return Optional.empty();
  }

  /**
   * Returns a list useBoxes of type ValueBox.
   */
  @Override
  public final List<ValueBox> getUseBoxes() {
    List<ValueBox> useBoxes = new ArrayList<ValueBox>();

    useBoxes.addAll(baseBox.getValue().getUseBoxes());
    useBoxes.add(baseBox);

    return useBoxes;
  }

  @Override
  public Type getType() {
    return view.getType(fieldSig.typeSignature);
  }

  @Override
  public void accept(IVisitor sw) {
    // TODO
  }

  @Override
  public boolean equivTo(Object o) {
    if (o instanceof JInstanceFieldRef) {
      JInstanceFieldRef fr = (JInstanceFieldRef) o;
      return fr.getField().equals(getField()) && fr.baseBox.getValue().equivTo(baseBox.getValue());
    }
    return false;
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    if (getField().isPresent()) {
      return getField().get().equivHashCode() * 101 + baseBox.getValue().equivHashCode() + 17;
    } else {
      return 16;
    }
  }

  @Override
  public boolean equivTo(Object o, Comparator<? extends Object> comparator) {
    // TODO Auto-generated method stub
    return false;
  }
}
