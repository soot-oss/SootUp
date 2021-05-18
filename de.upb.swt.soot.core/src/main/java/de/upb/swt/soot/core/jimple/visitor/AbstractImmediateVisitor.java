package de.upb.swt.soot.core.jimple.visitor;

import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.common.constant.Constant;
import javax.annotation.Nonnull;

public class AbstractImmediateVisitor<V> extends AbstractConstantVisitor<V>
    implements ImmediateVisitor {

  @Override
  public void caseLocal(Local local) {
    defaultCaseImmediate(local);
  }

  @Override
  public void defaultCaseConstant(@Nonnull Constant constant) {
    defaultCaseImmediate(constant);
  }

  private void defaultCaseImmediate(Immediate immediate) {}
}
