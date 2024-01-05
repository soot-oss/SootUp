package sootup.java.codepropertygraph.cpg;

import static org.junit.Assert.*;

import java.util.Optional;
import org.junit.Test;
import sootup.core.model.SootMethod;
import sootup.java.codepropertygraph.CpgTestSuiteBase;
import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;

public class MethodCpgTest extends CpgTestSuiteBase {
  private String methodName;

  @Test
  public void testCpgForIfStatement() {
    methodName = "ifStatement";
    assertCpgMethod(methodName);
  }

  @Test
  public void testCpgForIfElseStatement() {
    methodName = "ifElseStatement";
    assertCpgMethod(methodName);
  }

  @Test
  public void testCpgForIfElseIfStatement() {
    methodName = "ifElseIfStatement";
    assertCpgMethod(methodName);
  }

  @Test
  public void testCpgForIfElseCascadingStatement() {
    methodName = "ifElseCascadingStatement";
    assertCpgMethod(methodName);
  }

  @Test
  public void testCpgForIfElseCascadingElseIfStatement() {
    methodName = "ifElseCascadingElseIfStatement";
    assertCpgMethod(methodName);
  }

  @Test
  public void testCpgForIfElseCascadingElseIfInElseStatement() {
    methodName = "ifElseCascadingElseIfInElseStatement";
    assertCpgMethod(methodName);
  }

  private void assertCpgMethod(String methodName) {
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());

    MethodInfo methodInfo = new MethodInfo(method.get());
    PropertyGraph cpgGraph = CpgCreator.convert(methodInfo);

    writeGraph(cpgGraph.toDotGraph("CPG"), methodName, "CPG");
  }
}
