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

import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.JimpleComparator;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.util.printer.IStmtPrinter;
import de.upb.soot.views.IView;

import java.util.ArrayList;
import java.util.List;

public class JInstanceFieldRef extends FieldRef {

  /**
   * 
   */
  private static final long serialVersionUID = 2900174317359676686L;

  private final ValueBox baseBox;

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
    super(view, fieldSig);
    this.baseBox = Jimple.newLocalBox(base);
  }

  @Override
  public Object clone() {
    return new JInstanceFieldRef(this.view, Jimple.cloneIfNecessary(getBase()), fieldSignature);
  }

  @Override
  public String toString() {
    return baseBox.getValue().toString() + "." + fieldSignature.toString();
  }

  @Override
  public void toString(IStmtPrinter up) {
    baseBox.toString(up);
    up.literal(".");
    up.fieldSignature(fieldSignature);
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

  /**
   * Returns a list useBoxes of type ValueBox.
   */
  @Override
  public final List<ValueBox> getUseBoxes() {

    List<ValueBox> useBoxes = new ArrayList<ValueBox>(baseBox.getValue().getUseBoxes());
    useBoxes.add(baseBox);

    return useBoxes;
  }

  @Override
  public void accept(IVisitor sw) {
    // TODO
  }

  @Override
  public boolean equivTo(Object o) {
    return JimpleComparator.getInstance().caseInstanceFieldRef(this, o);
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseInstanceFieldRef(this, o);
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

}