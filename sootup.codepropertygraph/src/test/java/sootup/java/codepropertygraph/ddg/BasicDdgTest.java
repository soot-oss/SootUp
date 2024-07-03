package sootup.java.codepropertygraph.ddg;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.java.codepropertygraph.CpgTestSuiteBase;
import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;

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

    MethodInfo methodInfo = new MethodInfo(method.get());
    PropertyGraph ddgGraph = DdgCreator.convert(methodInfo);
    writeGraph(ddgGraph.toDotGraph("DDG"), methodName, "DDG");
  }
}
