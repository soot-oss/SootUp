package de.upb.swt.soot.core.jimple.visitor;

import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.ref.*;
import javax.annotation.Nonnull;

public class ReplaceUseRefVisitor extends AbstractRefVisitor {

  Value oldUse;
  Value newUse;
  Ref newRef;

  public ReplaceUseRefVisitor(Value oldUse, Value newUse) {
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

  @Override
  public void defaultCase(Object obj) {
    newRef = (Ref) obj;
  }

  public Ref getNewRef() {
    return newRef;
  }
}
