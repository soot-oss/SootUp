package de.upb.sse.sootup.core.jimple.visitor;

import de.upb.sse.sootup.core.jimple.basic.Local;
import javax.annotation.Nonnull;

/*
 * @author Markus Schmidt
 */
public interface ImmediateVisitor extends ConstantVisitor {
  void caseLocal(@Nonnull Local local);
}
