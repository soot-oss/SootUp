package sootup.java.codepropertygraph.ast;

import static org.junit.Assert.*;

import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootMethod;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.java.codepropertygraph.CpgTestSuiteBase;

@RunWith(Parameterized.class)
public class AstMinimalTSTest extends CpgTestSuiteBase {
  private final String methodName;
  private final String expectedName;
  private final Set<MethodModifier> expectedModifiers;
  private final List<Type> expectedParameterTypes;
  private final List<Stmt> expectedBodyStmts;
  private final Type expectedReturnType;

  public AstMinimalTSTest(
      String methodName,
      String expectedName,
      Set<MethodModifier> expectedModifiers,
      List<Type> expectedParameterTypes,
      List<Stmt> expectedBodyStmts,
      Type expectedReturnType) {
    this.methodName = methodName;
    this.expectedName = expectedName;
    this.expectedModifiers = expectedModifiers;
    this.expectedParameterTypes = expectedParameterTypes;
    this.expectedBodyStmts = expectedBodyStmts;
    this.expectedReturnType = expectedReturnType;
  }

  @Parameterized.Parameters(name = "TestAstMethod({0})")
  public static Object[][] data() {
    Object expectedModifiers1 = EnumSet.of(MethodModifier.PUBLIC);
    Object expectedParameterTypes1 = Collections.singletonList(PrimitiveType.IntType.getInstance());
    Object expectedBodyStmts1 = null;
    Object expectedReturnType1 = PrimitiveType.IntType.getInstance();

    Object expectedModifiers2 = EnumSet.of(MethodModifier.PUBLIC);
    Object expectedParameterTypes2 = Collections.singletonList(PrimitiveType.IntType.getInstance());
    Object expectedBodyStmts2 = null;
    Object expectedReturnType2 = PrimitiveType.IntType.getInstance();

    Object expectedModifiers3 = EnumSet.of(MethodModifier.PUBLIC);
    Object expectedParameterTypes3 = Collections.singletonList(PrimitiveType.IntType.getInstance());
    Object expectedBodyStmts3 = null;
    Object expectedReturnType3 = PrimitiveType.IntType.getInstance();

    Object expectedModifiers4 = EnumSet.of(MethodModifier.PUBLIC);
    Object expectedParameterTypes4 = Collections.singletonList(PrimitiveType.IntType.getInstance());
    Object expectedBodyStmts4 = null;
    Object expectedReturnType4 = PrimitiveType.IntType.getInstance();

    Object expectedModifiers5 = EnumSet.of(MethodModifier.PUBLIC);
    Object expectedParameterTypes5 = Collections.singletonList(PrimitiveType.IntType.getInstance());
    Object expectedBodyStmts5 = null;
    Object expectedReturnType5 = PrimitiveType.IntType.getInstance();

    Object expectedModifiers6 = EnumSet.of(MethodModifier.PUBLIC);
    Object expectedParameterTypes6 = Collections.singletonList(PrimitiveType.IntType.getInstance());
    Object expectedBodyStmts6 = null;
    Object expectedReturnType6 = PrimitiveType.IntType.getInstance();

    return new Object[][] {
      {
        "ifStatement",
        "ifStatement",
        expectedModifiers1,
        expectedParameterTypes1,
        expectedBodyStmts1,
        expectedReturnType1
      },
      {
        "ifElseStatement",
        "ifElseStatement",
        expectedModifiers2,
        expectedParameterTypes2,
        expectedBodyStmts2,
        expectedReturnType2
      },
      {
        "ifElseIfStatement",
        "ifElseIfStatement",
        expectedModifiers3,
        expectedParameterTypes3,
        expectedBodyStmts3,
        expectedReturnType3
      },
      {
        "ifElseCascadingStatement",
        "ifElseCascadingStatement",
        expectedModifiers4,
        expectedParameterTypes4,
        expectedBodyStmts4,
        expectedReturnType4
      },
      {
        "ifElseCascadingElseIfStatement",
        "ifElseCascadingElseIfStatement",
        expectedModifiers5,
        expectedParameterTypes5,
        expectedBodyStmts5,
        expectedReturnType5
      },
      {
        "ifElseCascadingElseIfInElseStatement",
        "ifElseCascadingElseIfInElseStatement",
        expectedModifiers6,
        expectedParameterTypes6,
        expectedBodyStmts6,
        expectedReturnType6
      }
    };
  }

  @Test
  public void testMethodAst() {
    Optional<? extends SootMethod> methodOpt =
        getMinimalTestSuiteMethod(methodName);
    assertTrue(methodOpt.isPresent());
    SootMethod method = methodOpt.get();
    MethodAst methodAst = new MethodAst(method);

    assertEquals(expectedName, methodAst.getName());
    assertEquals(expectedModifiers, methodAst.getModifiers());
    assertEquals(expectedParameterTypes, methodAst.getParameterTypes());
    // assertEquals(expectedBodyStmts, methodAst.getBodyStmts());
    assertEquals(expectedReturnType, methodAst.getReturnType());

    AstGraph astGraph = AstToGraphConverter.convert(methodAst);

    writeGraph(astGraph.toDotFormat(), methodName, "AST");
  }
}
