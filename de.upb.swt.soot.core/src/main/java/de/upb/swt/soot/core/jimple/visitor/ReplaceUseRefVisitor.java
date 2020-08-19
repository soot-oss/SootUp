package de.upb.swt.soot.core.jimple.visitor;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.ref.*;
import javax.annotation.Nonnull;

/**
 * Replace old use of a Ref with a new use
 *
 * @author Zun Wang
 */
public class ReplaceUseRefVisitor extends AbstractRefVisitor {

  Value oldUse;
  Local newUse;
  Ref newRef;

  public ReplaceUseRefVisitor(@Nonnull Value oldUse, @Nonnull Local newUse) {
    this.oldUse = oldUse;
    this.newUse = newUse;
  }

  @Nonnull
  @Override
  public void caseStaticFieldRef(@Nonnull JStaticFieldRef v) {}

  @Nonnull
  @Override
  public void caseInstanceFieldRef(@Nonnull JInstanceFieldRef v) {
    if (v.getBase().equivTo(oldUse)) {
      newRef = v.withBase(newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseArrayRef(@Nonnull JArrayRef v) {
    if (v.getBase().equivTo(oldUse) && v.getIndex().equivTo(oldUse)) {
      JArrayRef ref = v.withBase(newUse);
      newRef = ref.withIndex(newUse);
    } else if (v.getBase().equivTo(oldUse)) {
      newRef = v.withBase(newUse);
    } else if (v.getIndex().equivTo(oldUse)) {
      newRef = v.withIndex(newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseParameterRef(@Nonnull JParameterRef v) {
    defaultCase(v);
  }

  @Nonnull
  @Override
  public void caseCaughtExceptionRef(@Nonnull JCaughtExceptionRef v) {
    defaultCase(v);
  }

  @Nonnull
  @Override
  public void caseThisRef(@Nonnull JThisRef v) {
    defaultCase(v);
  }

  @Nonnull
  @Override
  public void defaultCase(@Nonnull Object obj) {
    newRef = (Ref) obj;
  }

  @Nonnull
  public Ref getNewRef() {
    return newRef;
  }
}
