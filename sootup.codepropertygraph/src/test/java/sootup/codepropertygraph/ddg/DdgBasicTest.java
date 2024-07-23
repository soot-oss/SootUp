package sootup.codepropertygraph.ddg;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import sootup.codepropertygraph.CpgTestSuiteBase;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.core.model.SootMethod;

public class DdgBasicTest extends CpgTestSuiteBase {
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
