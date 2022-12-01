package sootup.core.jimple.visitor;

import javax.annotation.Nonnull;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.constant.Constant;

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
