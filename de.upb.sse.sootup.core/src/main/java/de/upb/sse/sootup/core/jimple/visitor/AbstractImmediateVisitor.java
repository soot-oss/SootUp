package de.upb.sse.sootup.core.jimple.visitor;

import de.upb.sse.sootup.core.jimple.basic.Immediate;
import de.upb.sse.sootup.core.jimple.basic.Local;
import de.upb.sse.sootup.core.jimple.common.constant.Constant;
import javax.annotation.Nonnull;

public class AbstractImmediateVisitor<V> extends AbstractConstantVisitor<V>
    implements ImmediateVisitor {

  @Override
  public void caseLocal(@Nonnull Local local) {
    defaultCaseImmediate(local);
  }

  @Override
  public void defaultCaseConstant(@Nonnull Constant constant) {
    defaultCaseImmediate(constant);
  }

  private void defaultCaseImmediate(@Nonnull Immediate immediate) {}
}
