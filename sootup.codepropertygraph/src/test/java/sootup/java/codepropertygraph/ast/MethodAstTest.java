package sootup.java.codepropertygraph.ast;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import sootup.core.IdentifierFactory;
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.Modifier;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.views.View;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.language.JavaLanguage;

@RunWith(Parameterized.class)
public class MethodAstTest {
  private static final Path MINIMAL_TEST_SUITE_DIR =
      Paths.get("..//shared-test-resources/miniTestSuite/java6/binary");

  private static final String CLASS_NAME = "IfElseStatement";
  private final String methodName;
  private final String expectedName;
  private final Set<Modifier> expectedModifiers;
  private final List<Type> expectedParameterTypes;
  private final List<Stmt> expectedBodyStmts;
  private final Type expectedReturnType;

  public MethodAstTest(
      String methodName,
      String expectedName,
      Set<Modifier> expectedModifiers,
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
    Object expectedModifiers1 = EnumSet.of(Modifier.PUBLIC);
    Object expectedParameterTypes1 = Collections.singletonList(PrimitiveType.IntType.getInstance());
    Object expectedBodyStmts1 = null;
    Object expectedReturnType1 = PrimitiveType.IntType.getInstance();

    Object expectedModifiers2 = EnumSet.of(Modifier.PUBLIC);
    Object expectedParameterTypes2 = Collections.singletonList(PrimitiveType.IntType.getInstance());
    Object expectedBodyStmts2 = null;
    Object expectedReturnType2 = PrimitiveType.IntType.getInstance();

    Object expectedModifiers3 = EnumSet.of(Modifier.PUBLIC);
    Object expectedParameterTypes3 = Collections.singletonList(PrimitiveType.IntType.getInstance());
    Object expectedBodyStmts3 = null;
    Object expectedReturnType3 = PrimitiveType.IntType.getInstance();

    Object expectedModifiers4 = EnumSet.of(Modifier.PUBLIC);
    Object expectedParameterTypes4 = Collections.singletonList(PrimitiveType.IntType.getInstance());
    Object expectedBodyStmts4 = null;
    Object expectedReturnType4 = PrimitiveType.IntType.getInstance();

    Object expectedModifiers5 = EnumSet.of(Modifier.PUBLIC);
    Object expectedParameterTypes5 = Collections.singletonList(PrimitiveType.IntType.getInstance());
    Object expectedBodyStmts5 = null;
    Object expectedReturnType5 = PrimitiveType.IntType.getInstance();

    Object expectedModifiers6 = EnumSet.of(Modifier.PUBLIC);
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
        "ifElseCascadingStatement",
        "ifElseCascadingStatement",
        expectedModifiers3,
        expectedParameterTypes3,
        expectedBodyStmts3,
        expectedReturnType3
      },
      {
        "ifElseCascadingInElseStatement",
        "ifElseCascadingInElseStatement",
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
    Optional<? extends SootMethod> methodOpt = getMinimalTestSuiteMethod(methodName);
    assertTrue(methodOpt.isPresent());
    SootMethod method = methodOpt.get();
    MethodAst methodAst = new MethodAst(method);

    assertEquals(expectedName, methodAst.getName());
    assertEquals(expectedModifiers, methodAst.getModifiers());
    assertEquals(expectedParameterTypes, methodAst.getParameterTypes());
    // assertEquals(expectedBodyStmts, methodAst.getBodyStmts());
    assertEquals(expectedReturnType, methodAst.getReturnType());

    System.out.println(methodName);
    System.out.println(AstToGraphConverter.convert(methodAst).toDotFormat());
  }

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
}
