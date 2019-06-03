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
import de.upb.soot.jimple.visitor.Visitor;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.util.Copyable;
import de.upb.soot.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public final class JInstanceFieldRef extends FieldRef implements Copyable {

  /** */
  private static final long serialVersionUID = 2900174317359676686L;

  private final ValueBox baseBox;

  /**
   * Create a reference to a class' instance field.
   *
   * @param base the base value of the field
   * @param fieldSig the field sig
   */
  public JInstanceFieldRef(Value base, FieldSignature fieldSig) {
    super(fieldSig);
    this.baseBox = Jimple.newLocalBox(base);
  }

  @Override
  public String toString() {
    return baseBox.getValue().toString() + "." + getFieldSignature().toString();
  }

  @Override
  public void toString(StmtPrinter up) {
    baseBox.toString(up);
    up.literal(".");
    up.fieldSignature(getFieldSignature());
  }

  public Value getBase() {
    return baseBox.getValue();
  }

  public ValueBox getBaseBox() {
    return baseBox;
  }

  /** Returns a list useBoxes of type ValueBox. */
  @Override
  public final List<ValueBox> getUseBoxes() {

    List<ValueBox> useBoxes = new ArrayList<ValueBox>(baseBox.getValue().getUseBoxes());
    useBoxes.add(baseBox);

    return useBoxes;
  }

  @Override
  public void accept(Visitor sw) {
    // TODO
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseInstanceFieldRef(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return getFieldSignature().hashCode() * 101 + baseBox.getValue().hashCode() + 17;
  }

  @Nonnull
  public JInstanceFieldRef withBase(Value base) {
    return new JInstanceFieldRef(base, getFieldSignature());
  }

  @Nonnull
  public JInstanceFieldRef withFieldSignature(FieldSignature fieldSignature) {
    return new JInstanceFieldRef(getBase(), fieldSignature);
  }
}
