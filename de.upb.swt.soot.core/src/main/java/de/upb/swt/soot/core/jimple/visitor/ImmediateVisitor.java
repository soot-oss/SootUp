package de.upb.swt.soot.core.jimple.visitor;

import de.upb.swt.soot.core.jimple.basic.Local;

/*
 * @author Markus Schmidt
 */
public interface ImmediateVisitor extends ConstantVisitor {
  void caseLocal(Local local);
}
