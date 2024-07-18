package sootup.codepropertygraph.cfg;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import sootup.codepropertygraph.CpgTestSuiteBase;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.core.model.SootMethod;

public class MethodCfgTest extends CpgTestSuiteBase {
  private String methodName;

  @Test
  public void testCfgForIfStatement() {
    methodName = "ifStatement";
    assertCfgMethod(methodName);
  }

  @Test
  public void testCfgForIfElseStatement() {
    methodName = "ifElseStatement";
    assertCfgMethod(methodName);
  }

  @Test
  public void testCfgForIfElseIfStatement() {
    methodName = "ifElseIfStatement";
    assertCfgMethod(methodName);
  }

  @Test
  public void testCfgForIfElseCascadingStatement() {
    methodName = "ifElseCascadingStatement";
    assertCfgMethod(methodName);
  }

  @Test
  public void testCfgForIfElseCascadingElseIfStatement() {
    methodName = "ifElseCascadingElseIfStatement";
    assertCfgMethod(methodName);
  }

  @Test
  public void testCfgForIfElseCascadingElseIfInElseStatement() {
    methodName = "ifElseCascadingElseIfInElseStatement";
    assertCfgMethod(methodName);
  }

  private void assertCfgMethod(String methodName) {
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());

    PropertyGraph cfgGraph = new CfgCreator().createGraph(method.get());

    writeGraph(cfgGraph.toDotGraph("CFG"), methodName, "CFG");
  }
}
