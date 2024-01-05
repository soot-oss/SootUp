package sootup.java.codepropertygraph.cfg;

import static org.junit.Assert.*;

import java.util.Optional;
import org.junit.Test;
import sootup.core.model.SootMethod;
import sootup.java.codepropertygraph.CpgTestSuiteBase;

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
    MethodCfg cfg = new MethodCfg(method.get());
    CfgGraph cfgGraph = CfgToGraphConverter.convert(cfg);
    writeGraph(cfgGraph.toDotFormat(), methodName, "CFG");
  }
}
