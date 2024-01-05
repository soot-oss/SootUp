package sootup.java.codepropertygraph.cdg;

import static org.junit.Assert.*;

import java.util.Optional;
import org.junit.Test;
import sootup.core.model.SootMethod;
import sootup.java.codepropertygraph.CpgTestSuiteBase;

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
    MethodCdg cdg = new MethodCdg(method.get());
    CdgGraph cdgGraph = CdgToGraphConverter.convert(cdg);

    writeGraph(cdgGraph.toDotFormat(), methodName, "CDG");
  }
}
