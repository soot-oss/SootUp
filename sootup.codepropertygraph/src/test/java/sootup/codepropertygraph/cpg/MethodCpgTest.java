package sootup.codepropertygraph.cpg;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import sootup.codepropertygraph.ast.AstCreator;
import sootup.codepropertygraph.cdg.CdgCreator;
import sootup.codepropertygraph.cfg.CfgCreator;
import sootup.codepropertygraph.ddg.DdgCreator;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.core.model.SootMethod;
import sootup.codepropertygraph.CpgTestSuiteBase;

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

    PropertyGraph cpgGraph =
        (new CpgCreator(new AstCreator(), new CfgCreator(), new CdgCreator(), new DdgCreator()))
            .createCpg(method.get());

    writeGraph(cpgGraph.toDotGraph("CPG"), methodName, "CPG");
  }
}
