package sootup.codepropertygraph.cdg;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import sootup.codepropertygraph.CpgTestSuiteBase;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.core.model.SootMethod;

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

    PropertyGraph cdgGraph = (new CdgCreator()).createGraph(method.get());

    writeGraph(cdgGraph.toDotGraph(), cdgGraph.getName());
  }
}
