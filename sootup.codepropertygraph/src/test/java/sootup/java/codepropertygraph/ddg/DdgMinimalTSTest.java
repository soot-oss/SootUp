package sootup.java.codepropertygraph.ddg;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.java.codepropertygraph.CpgTestSuiteBase;
import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;

public class DdgMinimalTSTest extends CpgTestSuiteBase {

  @Test
  public void testDdgForIfStatement() {
    String methodName = "ifStatement";
    assertDdgMethod(methodName);
  }

  @Test
  public void testDdgForIfElseStatement() {
    String methodName = "ifElseStatement";
    assertDdgMethod(methodName);
  }

  @Test
  public void testDdgForIfElseIfStatement() {
    String methodName = "ifElseIfStatement";
    assertDdgMethod(methodName);
  }

  @Test
  public void testDdgForIfElseCascadingElseIfStatement() {
    String methodName = "ifElseCascadingElseIfStatement";
    assertDdgMethod(methodName);
  }

  @Test
  public void testDdgForIfElseCascadingElseIfInElseStatement() {
    String methodName = "ifElseCascadingElseIfInElseStatement";
    assertDdgMethod(methodName);
  }

  private void assertDdgMethod(String methodName) {
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());

    MethodInfo methodInfo = new MethodInfo(method.get());
    PropertyGraph ddgGraph = DdgCreator.convert(methodInfo);

    writeGraph(ddgGraph.toDotGraph("DDG"), methodName, "DDG");
  }
}
