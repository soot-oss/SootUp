package sootup.java.bytecode.frontend;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.Trap;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.util.printer.BriefStmtPrinter;
import sootup.java.bytecode.frontend.inputlocation.ClassFileBasedAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/**
 * nested try/catch/finally: "Invalid StmtGraph. A Trap is not created as a traps endStmt was not
 * visited during the iteration of all Stmts."
 *
 * @see <a href="https://github.com/soot-oss/SootUp/issues/996">#996</a>
 */
public class Issue996Tests {

  @Test
  public void testNestedTryCatchFinallyTraps() {
    // compiled with javac 8 (from the issue)
    Path classFilePath = Paths.get("src/test/resources/bugfixes/NestedTryCatchFinally.class");
    AnalysisInputLocation inputLocation =
        new ClassFileBasedAnalysisInputLocation(classFilePath, "", SourceType.Application);
    JavaView view = new JavaView(Collections.singletonList(inputLocation));

    MethodSignature methodSignature =
        view.getIdentifierFactory()
            .parseMethodSignature("<NestedTryCatchFinally: java.lang.String test0(java.io.File)>");
    Body body = view.getMethod(methodSignature).get().getBody();
    ControlFlowGraph<?> graph = body.getControlFlowGraph();

    // threw the IllegalArgumentException
    BriefStmtPrinter stmtPrinter = new BriefStmtPrinter();
    stmtPrinter.buildTraps(graph);
    List<Trap> traps = stmtPrinter.getTraps();
    assertFalse(traps.isEmpty());

    JimpleSerializationOrderTest.assertSerializationIsConsistent(body);
  }
}
