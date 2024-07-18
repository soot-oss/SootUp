package sootup.codepropertygraph.ddg;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.core.model.SootMethod;
import sootup.codepropertygraph.CpgTestSuiteBase;

public class BasicDdgTest extends CpgTestSuiteBase {
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
    writeGraph(ddgGraph.toDotGraph("DDG"), methodName, "DDG");
  }
}
