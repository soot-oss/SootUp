package sootup.java.codepropertygraph.ast;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import sootup.core.IdentifierFactory;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.language.JavaLanguage;

public class MethodAstTest {
  private static final Path MINIMAL_TEST_SUITE_DIR =
      Paths.get("..//shared-test-resources/miniTestSuite/java6/binary");

  private static final String CLASS_NAME = "IfElseStatement";

  Optional<? extends SootMethod> getMinimalTestSuiteMethod(String methodName) {
    PathBasedAnalysisInputLocation inputLocation =
        new PathBasedAnalysisInputLocation(MINIMAL_TEST_SUITE_DIR, null);
    JavaLanguage language = new JavaLanguage(8);
    JavaProject project = JavaProject.builder(language).addInputLocation(inputLocation).build();
    IdentifierFactory projectIdentifierFactory = project.getIdentifierFactory();
    View<JavaSootClass> view = project.createView();
    ClassType appClass = projectIdentifierFactory.getClassType(CLASS_NAME);
    MethodSignature methodSignature =
        projectIdentifierFactory.getMethodSignature(
            appClass, methodName, "int", Collections.singletonList("int"));
    return view.getMethod(methodSignature);
  }

  @org.junit.Before
  public void setUp() {}

  @Test
  public void testAstForIfStatement() {
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod("ifStatement");
    Assert.assertTrue(method.isPresent());
    MethodAst ast = new MethodAst(method.get());
    AstGraph astGraph = AstToGraphConverter.convert(ast);
    System.out.println(astGraph.toDotFormat());
  }

  @Test
  public void testAstForIfElseStatement() {
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod("ifElseStatement");
    Assert.assertTrue(method.isPresent());
    MethodAst ast = new MethodAst(method.get());
    AstGraph astGraph = AstToGraphConverter.convert(ast);
    System.out.println(astGraph.toDotFormat());
  }

  @Test
  public void testAstForIfElseIfStatement() {
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod("ifElseIfStatement");
    Assert.assertTrue(method.isPresent());
    MethodAst ast = new MethodAst(method.get());
    AstGraph astGraph = AstToGraphConverter.convert(ast);
    System.out.println(astGraph.toDotFormat());
  }
}
