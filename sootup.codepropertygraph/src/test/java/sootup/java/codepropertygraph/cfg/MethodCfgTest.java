package sootup.java.codepropertygraph.cfg;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.java.codepropertygraph.CpgTestSuiteBase;
import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;

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

    MethodInfo methodInfo = new MethodInfo(method.get());
    PropertyGraph cfgGraph = CfgCreator.convert(methodInfo);

    writeGraph(cfgGraph.toDotGraph("CFG"), methodName, "CFG");
  }
}
