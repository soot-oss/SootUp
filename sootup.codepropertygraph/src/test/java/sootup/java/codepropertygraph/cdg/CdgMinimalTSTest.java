package sootup.java.codepropertygraph.cdg;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.java.codepropertygraph.CpgTestSuiteBase;
import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;

public class CdgMinimalTSTest extends CpgTestSuiteBase {
  @Test
  public void testCdgForIfStatement() {
    String methodName = "ifStatement";
    assertCdgMethod(methodName);
  }

  @Test
  public void testCdgForIfElseStatement() {
    String methodName = "ifElseStatement";
    assertCdgMethod(methodName);
  }

  @Test
  public void testCdgForIfElseIfStatement() {
    String methodName = "ifElseIfStatement";
    assertCdgMethod(methodName);
  }

  @Test
  public void testCdgForIfElseCascadingElseIfStatement() {
    String methodName = "ifElseCascadingElseIfStatement";
    assertCdgMethod(methodName);
  }

  @Test
  public void testCdgForIfElseCascadingElseIfInElseStatement() {
    String methodName = "ifElseCascadingElseIfInElseStatement";
    assertCdgMethod(methodName);
  }

  private void assertCdgMethod(String methodName) {
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());

    MethodInfo methodInfo = new MethodInfo(method.get());
    PropertyGraph cdgGraph = CdgCreator.convert(methodInfo);

    writeGraph(cdgGraph.toDotGraph("CDG"), methodName, "CDG");
  }
}
