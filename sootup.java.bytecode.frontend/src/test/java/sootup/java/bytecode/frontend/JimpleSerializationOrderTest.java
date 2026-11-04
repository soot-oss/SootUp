package sootup.java.bytecode.frontend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.jimple.common.Trap;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.core.util.printer.BriefStmtPrinter;
import sootup.java.bytecode.frontend.inputlocation.ClassFileBasedAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/** the serialized stmt order + traps must describe exactly the ControlFlowGraph */
public class JimpleSerializationOrderTest {

  @Test
  public void testSerializationIsConsistent() {
    Arrays.asList(
            "miniTestSuite/java6/binary/TryCatchFinally.class",
            "miniTestSuite/java7/binary/MultiTryCatch.class",
            "miniTestSuite/java7/binary/TryWithResources.class",
            "miniTestSuite/java9/binary/TryWithResourcesConcise.class",
            "bugfixes/TryWithResourcesFinally.class",
            "bugfixes/NestedTryCatchFinally.class",
            "bugfixes/LineIterator.class")
        .forEach(JimpleSerializationOrderTest::assertSerializationIsConsistent);
  }

  static void assertSerializationIsConsistent(String classFile) {
    JavaView view =
        new JavaView(
            Collections.singletonList(
                new ClassFileBasedAnalysisInputLocation(
                    Paths.get("src/test/resources/" + classFile), "", SourceType.Application)));
    view.getClasses()
        .forEach(
            clazz ->
                clazz.getMethods().stream()
                    .filter(method -> method.hasBody())
                    .forEach(method -> assertSerializationIsConsistent(method.getBody())));
  }

  static void assertSerializationIsConsistent(Body body) {
    ControlFlowGraph<?> graph = body.getControlFlowGraph();
    BriefStmtPrinter stmtPrinter = new BriefStmtPrinter();
    stmtPrinter.buildTraps(graph);
    List<Trap> traps = stmtPrinter.getTraps();
    List<Stmt> stmts = graph.getStmts();

    for (int i = 0; i < stmts.size(); i++) {
      Stmt stmt = stmts.get(i);
      String info = body.getMethodSignature() + ": '" + stmt + "'";

      // the fallsthrough successor has to be serialized directly after its predecessor
      if (stmt.fallsThrough() && !graph.successors(stmt).isEmpty()) {
        assertSame(
            graph.successors(stmt).get(0),
            i + 1 < stmts.size() ? stmts.get(i + 1) : null,
            "fallsthrough successor of " + info);
      }

      // the traps (applied to the serialized order) have to result in the same exceptional flows
      Map<ClassType, Stmt> coveredBy = new HashMap<>();
      for (Trap trap : traps) {
        int begin = stmts.indexOf(trap.getBeginStmt());
        int end = stmts.indexOf(trap.getEndStmt());
        if (end < 0) {
          // trap ends at a nop appended after the last stmt
          end = stmts.size();
        }
        if (begin <= i && i < end) {
          // first matching trap wins
          coveredBy.putIfAbsent(trap.getExceptionType(), trap.getHandlerStmt());
        }
      }
      assertEquals(graph.exceptionalSuccessors(stmt), coveredBy, "traps of " + info);
    }
  }
}
