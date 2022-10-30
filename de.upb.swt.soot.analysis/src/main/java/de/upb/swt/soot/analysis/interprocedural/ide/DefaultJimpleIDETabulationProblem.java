package de.upb.swt.soot.analysis.interprocedural.ide;

import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.SootMethod;
import heros.InterproceduralCFG;
import heros.template.DefaultIDETabulationProblem;

public abstract class DefaultJimpleIDETabulationProblem<
        D, V, I extends InterproceduralCFG<Stmt, SootMethod>>
    extends DefaultIDETabulationProblem<Stmt, D, SootMethod, V, I> {
  public DefaultJimpleIDETabulationProblem(I icfg) {
    super(icfg);
  }
}
