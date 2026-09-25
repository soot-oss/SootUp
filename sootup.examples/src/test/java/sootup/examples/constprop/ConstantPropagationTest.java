package sootup.examples.constprop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.Local;
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

public class ConstantPropagationTest {

  @Test
  public void constantPropagation() {

    // --8<-- [start:setup]
    AnalysisInputLocation inputLocation =
        PathBasedAnalysisInputLocation.create(
            Paths.get("src/test/resources/ConstProp/binary"), SourceType.Application);
    View view = new JavaView(inputLocation);

    ClassType classType = view.getIdentifierFactory().getClassType("Example");
    SootClass sootClass = view.getClass(classType).get();

    MethodSignature methodSignature =
        view.getIdentifierFactory().parseMethodSignature("<Example: void assign()>");
    SootMethod method = sootClass.getMethod(methodSignature.getSubSignature()).get();
    Body body = method.getBody();

    // Run constant propagation and retrieve the solver for querying results.
    DataflowSolver<Map<Local, Value>> solver = ConstantPropagation.analyze(body);
    // --8<-- [end:setup]

    ControlFlowGraph<?> cfg = body.getControlFlowGraph();
    List<Stmt> stmts = body.getStmts();

    // --8<-- [start:assertions]
    // At the last assignment (y = x), OUT must contain {x=4, y=4}.
    // The exact last assignment stmt is the final "non-return" stmt, but we check
    // the return stmt's IN fact, which equals the OUT of the preceding assignment.
    Stmt lastAssign = stmts.get(stmts.size() - 2); // last stmt before return
    Map<Local, Value> out = solver.getOutFact(lastAssign);

    // Every local with a constant value must equal the expected constant.
    for (Map.Entry<Local, Value> entry : out.entrySet()) {
      Local local = entry.getKey();
      Value value = entry.getValue();
      assertTrue(value.isConstant(), local.getName() + " should be a constant but was " + value);
      if (local.getName().equals("x") || local.getName().equals("y")) {
        assertEquals(4, value.getConstant(), local.getName() + " should equal 4");
      }
    }
    // --8<-- [end:assertions]
  }
}
