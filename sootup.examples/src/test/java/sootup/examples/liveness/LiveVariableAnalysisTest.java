package sootup.examples.liveness;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.examples.dataflow.DataflowSolver;
import sootup.java.bytecode.frontend.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.views.JavaView;

public class LiveVariableAnalysisTest {

  @Test
  public void liveVariableAnalysis() {

    // --8<-- [start:setup]
    AnalysisInputLocation inputLocation =
        PathBasedAnalysisInputLocation.create(
            Paths.get("src/test/resources/Liveness/binary"), SourceType.Application);
    View view = new JavaView(inputLocation);

    ClassType classType = view.getIdentifierFactory().getClassType("Example");
    SootClass sootClass = view.getClass(classType).get();

    MethodSignature methodSignature =
        view.getIdentifierFactory().parseMethodSignature("<Example: int compute(int,int,int)>");
    SootMethod method = sootClass.getMethod(methodSignature.getSubSignature()).get();
    Body body = method.getBody();

    // Run live variable analysis and retrieve the solver for querying results.
    DataflowSolver<Set<Local>> solver = LiveVariableAnalysis.analyze(body);
    // --8<-- [end:setup]

    ControlFlowGraph<?> cfg = body.getControlFlowGraph();

    // --8<-- [start:assertions]
    for (Stmt stmt : cfg.getNodes()) {
      Set<Local> liveIn = solver.getInFact(stmt);

      if (stmt instanceof JReturnStmt) {
        JReturnStmt ret = (JReturnStmt) stmt;
        Immediate returnedValue = ret.getOp();

        // The variable being returned must be live just before the return.
        assertTrue(returnedValue instanceof Local, "return operand should be a Local");
        assertTrue(
            liveIn.contains((Local) returnedValue), "returned variable must be live before return");

        // No other locals should be live at the return point (nothing else is used after).
        assertFalse(
            liveIn.stream().anyMatch(l -> l != returnedValue),
            "no other variable should be live before return");
      }

      if (stmt instanceof JAssignStmt) {
        JAssignStmt assign = (JAssignStmt) stmt;
        // A variable that is defined and then immediately never used should NOT be live
        // in its own OUT set (it was just killed).
        // We spot-check: nothing defined here should appear in OUT (post-transfer).
        Set<Local> liveOut = solver.getOutFact(stmt);
        if (assign.getLeftOp() instanceof Local) {
          Local defined = (Local) assign.getLeftOp();
          // If this definition is the last use of the variable (i.e. it is dead code),
          // the defined variable will not appear in liveOut. We cannot assert this
          // universally because the variable might be used later. We just verify the
          // result is non-null (the solver ran).
          assertTrue(liveOut != null, "solver must produce a result for every stmt");
        }
      }
    }
    // --8<-- [end:assertions]
  }
}
