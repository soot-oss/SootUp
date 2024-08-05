package sootup.codepropertygraph.ddg;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.codepropertygraph.BenchmarkTestSuiteBase;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.core.model.SootMethod;

@Tag("Java8")
public class DdgBasicTest extends BenchmarkTestSuiteBase {
  @Test
  public void testDdgForReassignment() {
    String methodName = "calculate";
    assertDdgMethod(methodName);
  }

  private void assertDdgMethod(String methodName) {
    Optional<? extends SootMethod> method =
        getTestResourcesMethod("codepropertygraph.Reassignment", methodName);
    assertTrue(method.isPresent());

    PropertyGraph ddgGraph = (new DdgCreator()).createGraph(method.get());
    System.out.println(ddgGraph.toDotGraph());
  }
}
