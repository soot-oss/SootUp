package sootup.codepropertygraph.ddg;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.core.model.SootMethod;
import sootup.codepropertygraph.CpgTestSuiteBase;

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

    PropertyGraph ddgGraph = (new DdgCreator()).createGraph(method.get());

    writeGraph(ddgGraph.toDotGraph("DDG"), methodName, "DDG");
  }
}
