package sootup.java.codepropertygraph.ddg;

import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.junit.Test;
import sootup.core.model.SootMethod;
import sootup.java.codepropertygraph.CpgTestSuiteBase;

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

    MethodDdg ddg = new MethodDdg(method.get());
    DdgGraph ddgGraph = DdgToGraphConverter.convert(ddg);
    writeGraph(ddgGraph.toDotFormat(), methodName, "DDG");
  }
}
