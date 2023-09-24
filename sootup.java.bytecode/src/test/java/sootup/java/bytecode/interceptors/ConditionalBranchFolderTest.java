package sootup.java.bytecode.interceptors;

import static org.junit.Assert.assertEquals;

import categories.Java8Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.JEqExpr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.util.ImmutableUtils;
import sootup.core.util.Utils;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;

/** @author Marcus Nachtigall */
@Category(Java8Test.class)
public class ConditionalBranchFolderTest {

  /**
   * Tests the correct deletion of an if-statement with a constant condition. Transforms from
   *
   * <p>a = "str"; b = "str"; if(a == b) return a; else return b;
   *
   * <p>to
   *
   * <p>a = "str"; b = "str"; return a;
   */
  @Test
  public void testUnconditionalBranching() {
    Body.BodyBuilder builder = createBodyBuilder(0);
    new ConditionalBranchFolder().interceptBody(builder, null);
    assertEquals(
        Arrays.asList("a = \"str\"", "b = \"str\"", "return a"),
        Utils.bodyStmtsAsStrings(builder.build()));
  }

  /**
   * Tests the correct handling of an if-statement with a always false condition. Consider the
   * following code
   *
   * <p>a = "str"; b = "different string"; if(a == b) return a; else return b;
   */
  @Test
  public void testConditionalBranching() {
    Body.BodyBuilder builder = createBodyBuilder(1);
    Body originalBody = builder.build();
    new ConditionalBranchFolder().interceptBody(builder, null);
    Body processedBody = builder.build();

    assertEquals(
        Arrays.asList("a = \"str\"", "b = \"different string\"", "return b"),
        Utils.bodyStmtsAsStrings(processedBody));
  }

  @Test
  public void testConditionalBranchingWithNoConclusiveIfCondition() {
    Body.BodyBuilder builder = createBodyBuilder(2);
    Body originalBody = builder.build();
    new ConditionalBranchFolder().interceptBody(builder, null);
    Body processedBody = builder.build();

    assertEquals(Utils.bodyStmtsAsStrings(originalBody), Utils.bodyStmtsAsStrings(processedBody));
  }

  /**
   * Generates the correct test {@link Body} for the corresponding test case.
   *
   * @param constantCondition indicates, whether the condition is constant.
   * @return the generated {@link Body}
   */
  private static Body.BodyBuilder createBodyBuilder(int constantCondition) {
    JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
    JavaJimple javaJimple = JavaJimple.getInstance();
    StmtPositionInfo noPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

    JavaClassType stringType = factory.getClassType("java.lang.String");
    Local a = JavaJimple.newLocal("a", stringType);
    Local b = JavaJimple.newLocal("b", stringType);

    StringConstant stringConstant = javaJimple.newStringConstant("str");
    Stmt strToA = JavaJimple.newAssignStmt(a, stringConstant, noPositionInfo);

    Stmt strToB;
    StringConstant anotherStringConstant;
    JEqExpr jEqExpr;
    switch (constantCondition) {
      case 0:
        anotherStringConstant = javaJimple.newStringConstant("str");
        strToB = JavaJimple.newAssignStmt(b, anotherStringConstant, noPositionInfo);
        jEqExpr = new JEqExpr(stringConstant, anotherStringConstant);

        break;
      case 1:
        anotherStringConstant = javaJimple.newStringConstant("different string");
        strToB = JavaJimple.newAssignStmt(b, anotherStringConstant, noPositionInfo);
        jEqExpr = new JEqExpr(stringConstant, anotherStringConstant);

        break;
      case 2:
        final MethodSignature methodSignature =
            JavaIdentifierFactory.getInstance()
                .getMethodSignature(
                    "toString", "java.lang.Object", "String", Collections.emptyList());
        Local base =
            new Local(
                "someObjectThatHasSomethingToString",
                new JavaClassType("StringBuilder", new PackageName("java.lang")));
        strToB =
            JavaJimple.newAssignStmt(
                b, Jimple.newVirtualInvokeExpr(base, methodSignature), noPositionInfo);
        jEqExpr = new JEqExpr(stringConstant, b);
        break;
      default:
        throw new IllegalArgumentException();
    }

    Stmt ifStmt = Jimple.newIfStmt(jEqExpr, noPositionInfo);
    Stmt reta = JavaJimple.newReturnStmt(a, noPositionInfo);
    Stmt retb = JavaJimple.newReturnStmt(b, noPositionInfo);

    Set<Local> locals = ImmutableUtils.immutableSet(a, b);

    Body.BodyBuilder bodyBuilder = Body.builder();
    bodyBuilder.setLocals(locals);
    bodyBuilder.setStartingStmt(strToA);
    bodyBuilder.addFlow(strToA, strToB);
    bodyBuilder.addFlow(strToB, ifStmt);
    bodyBuilder.addFlow(ifStmt, reta);
    bodyBuilder.addFlow(ifStmt, retb);
    bodyBuilder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("test", "ab.c", "void", Collections.emptyList()));
    return bodyBuilder;
  }
}
