package sootup.core.jimple.visitor;

import javax.annotation.Nonnull;
import sootup.core.jimple.basic.Local;

/*
 * @author Markus Schmidt
 */
public interface ImmediateVisitor extends ConstantVisitor {
  void caseLocal(@Nonnull Local local);
}
