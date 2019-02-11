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

import com.google.common.base.Optional;

import de.upb.soot.core.SootField;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.symbolicreferences.FieldRef;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.util.printer.IStmtPrinter;
import de.upb.soot.views.IView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JInstanceFieldRef implements JFieldRef {

  /** */
  private static final long serialVersionUID = 2900174317359676686L;

  private final FieldRef symbolicFieldRef;
  private final ValueBox baseBox;
  private IView view;

  /**
   * Create a reference to a class' instance field.
   *
   * @param view
   *          the view
   * @param base
   *          the base value of the field
   * @param symbolicFieldRef
   *          the field sig
   */
  public JInstanceFieldRef(IView view, Value base, FieldRef symbolicFieldRef) {
    ValueBox baseBox = Jimple.newLocalBox(base);
    this.baseBox = baseBox;
    this.symbolicFieldRef = symbolicFieldRef;
    this.view = view;
  }

  @Override
  public Object clone() {
    return new JInstanceFieldRef(this.view, Jimple.cloneIfNecessary(getBase()), symbolicFieldRef);
  }

  @Override
  public String toString() {
    return baseBox.getValue().toString() + "." + symbolicFieldRef.toString();
  }

  @Override
  public void toString(IStmtPrinter up) {
    baseBox.toString(up);
    up.literal(".");
    up.fieldSignature(symbolicFieldRef.getSignature());
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
    return com.google.common.base.Optional.fromNullable(symbolicFieldRef.resolve());
  }

  @Override
  public FieldSignature getFieldSignature() {
    return symbolicFieldRef.getSignature();
  }

  /** Returns a list useBoxes of type ValueBox. */
  @Override
  public final List<ValueBox> getUseBoxes() {
    List<ValueBox> useBoxes = new ArrayList<ValueBox>();

    useBoxes.addAll(baseBox.getValue().getUseBoxes());
    useBoxes.add(baseBox);

    return useBoxes;
  }

  @Override
  public Type getType() {
    return view.getType(symbolicFieldRef.getSignature().typeSignature);
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
  public boolean equivTo(Object o, Comparator comparator) {
    return comparator.compare(this, o) == 0;
  }
}
