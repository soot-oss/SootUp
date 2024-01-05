package sootup.java.codepropertygraph.ddg;

import static org.junit.Assert.*;

import java.util.Optional;
import org.junit.Test;
import sootup.core.model.SootMethod;
import sootup.java.codepropertygraph.CpgTestSuiteBase;

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

  private void assertDdgMethod(String methodName) {
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());
    MethodDdg ddg = new MethodDdg(method.get());
    DdgGraph ddgGraph = DdgToGraphConverter.convert(ddg);

    writeGraph(ddgGraph.toDotFormat(), methodName, "DDG");
  }

  @Test
  public void testDdgForIfElseCascadingElseIfInElseStatement() {
    String methodName = "ifElseCascadingElseIfInElseStatement";
    assertDdgMethod(methodName);
  }
}
