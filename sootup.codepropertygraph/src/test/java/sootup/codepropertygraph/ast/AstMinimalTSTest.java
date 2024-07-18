package sootup.codepropertygraph.ast;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;
import sootup.codepropertygraph.MethodInfo;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootMethod;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.codepropertygraph.CpgTestSuiteBase;

public class AstMinimalTSTest extends CpgTestSuiteBase {
  private String methodName;

  @Test
  public void testAstForIfStatement() {
    methodName = "ifStatement";
    assertAstMethod(
        methodName,
        "ifStatement",
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.singletonList(PrimitiveType.IntType.getInstance()),
        null,
        PrimitiveType.IntType.getInstance());
  }

  @Test
  public void testAstForIfElseStatement() {
    methodName = "ifElseStatement";
    assertAstMethod(
        methodName,
        "ifElseStatement",
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.singletonList(PrimitiveType.IntType.getInstance()),
        null,
        PrimitiveType.IntType.getInstance());
  }

  @Test
  public void testAstForIfElseIfStatement() {
    methodName = "ifElseIfStatement";
    assertAstMethod(
        methodName,
        "ifElseIfStatement",
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.singletonList(PrimitiveType.IntType.getInstance()),
        null,
        PrimitiveType.IntType.getInstance());
  }

  @Test
  public void testAstForIfElseCascadingStatement() {
    methodName = "ifElseCascadingStatement";
    assertAstMethod(
        methodName,
        "ifElseCascadingStatement",
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.singletonList(PrimitiveType.IntType.getInstance()),
        null,
        PrimitiveType.IntType.getInstance());
  }

  @Test
  public void testAstForIfElseCascadingElseIfStatement() {
    methodName = "ifElseCascadingElseIfStatement";
    assertAstMethod(
        methodName,
        "ifElseCascadingElseIfStatement",
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.singletonList(PrimitiveType.IntType.getInstance()),
        null,
        PrimitiveType.IntType.getInstance());
  }

  @Test
  public void testAstForIfElseCascadingElseIfInElseStatement() {
    methodName = "ifElseCascadingElseIfInElseStatement";
    assertAstMethod(
        methodName,
        "ifElseCascadingElseIfInElseStatement",
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.singletonList(PrimitiveType.IntType.getInstance()),
        null,
        PrimitiveType.IntType.getInstance());
  }

  private void assertAstMethod(
      String methodName,
      String expectedName,
      Set<MethodModifier> expectedModifiers,
      List<Type> expectedParameterTypes,
      List<Stmt> expectedBodyStmts,
      Type expectedReturnType) {
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());

    PropertyGraph astGraph = (new AstCreator()).createGraph(method.get());

    MethodInfo methodInfo = new MethodInfo(method.get());
    assertEquals(expectedName, methodInfo.getName());
    assertEquals(expectedModifiers, methodInfo.getModifiers());
    assertEquals(expectedParameterTypes, methodInfo.getParameterTypes());
    // assertEquals(expectedBodyStmts, methodInfo.getBodyStmts());
    assertEquals(expectedReturnType, methodInfo.getReturnType());

    writeGraph(astGraph.toDotGraph("AST"), methodName, "AST");
  }
}
