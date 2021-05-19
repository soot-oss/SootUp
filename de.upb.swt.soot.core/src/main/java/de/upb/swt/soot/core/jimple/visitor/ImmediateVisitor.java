package de.upb.swt.soot.core.jimple.visitor;

import de.upb.swt.soot.core.jimple.basic.Local;
import javax.annotation.Nonnull;

/*
 * @author Markus Schmidt
 */
public interface ImmediateVisitor extends ConstantVisitor {
  void caseLocal(@Nonnull Local local);
}
