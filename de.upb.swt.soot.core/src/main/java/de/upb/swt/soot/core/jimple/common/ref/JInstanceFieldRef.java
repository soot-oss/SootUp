/*
 * @author Linghui Luo
 * @version 1.0
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package de.upb.swt.soot.core.jimple.common.ref;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public final class JInstanceFieldRef extends JFieldRef implements Copyable {

  private final ValueBox baseBox;
  // new attribute: later if ValueBox is deleted, then add "final" to it.
  private Value base;

  /**
   * Create a reference to a class' instance field.
   *
   * @param base the base value of the field
   * @param fieldSig the field sig
   */
  public JInstanceFieldRef(Value base, FieldSignature fieldSig) {
    super(fieldSig);
    this.baseBox = Jimple.newLocalBox(base);
    // new attribute
    this.base = base;
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

  @Override
  public final List<Value> getUses() {
    List<Value> list = new ArrayList<>(base.getUses());
    list.add(base);
    return list;
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
