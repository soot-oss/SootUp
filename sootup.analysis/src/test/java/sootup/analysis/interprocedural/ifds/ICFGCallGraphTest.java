package sootup.analysis.interprocedural.ifds;


import heros.InterproceduralCFG;
import org.junit.Test;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;

public class ICFGCallGraphTest extends IFDSTaintTestSetUp{

    @Test
    public void ICFGDotExportTest() {
        JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> analysis =
                executeStaticAnalysis("ICFGExample");
    }
}
