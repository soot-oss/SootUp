package de.upb.swt.soot.analysis.interprocedural.ide;

import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.SootMethod;
import heros.IDETabulationProblem;
import heros.InterproceduralCFG;
import heros.solver.IDESolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JimpleIDESolver<D, V, I extends InterproceduralCFG<Stmt, SootMethod>> extends IDESolver<Stmt, D, SootMethod, V, I> {
    private static final Logger logger = LoggerFactory.getLogger(JimpleIDESolver.class);

    public JimpleIDESolver(IDETabulationProblem<Stmt, D, SootMethod, V, I> problem) {
        super(problem);
    }

    public void solve() {
        super.solve();
    }

}