package sootup.java.bytecode.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import categories.TestCategories;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.JEqExpr;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.util.ImmutableUtils;
import sootup.core.util.Utils;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.interceptors.ConditionalBranchFolder;
import sootup.java.core.interceptors.CopyPropagator;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

/** @author Marcus Nachtigall */
@Tag(TestCategories.JAVA_8_CATEGORY)
public class ConditionalBranchFolderTest {

  Path classFilePath =
      Paths.get("../shared-test-resources/bugfixes/ConditionalBranchFolderTest.class");

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
    new ConditionalBranchFolder().interceptBody(builder, new JavaView(Collections.emptyList()));
    Body body = builder.build();
    assertEquals(
        Stream.of(
                "java.lang.String a, b",
                "a = \"str\"",
                "b = \"str\"",
                "goto label1",
                "label1:",
                "return b")
            .collect(Collectors.toList()),
        Utils.filterJimple(body.toString()));
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
    new ConditionalBranchFolder().interceptBody(builder, new JavaView(Collections.emptyList()));
    Body processedBody = builder.build();

    assertEquals(
        Stream.of(
                "java.lang.String a, b",
                "a = \"str\"",
                "b = \"different string\"",
                "goto label1",
                "label1:",
                "return a")
            .collect(Collectors.toList()),
        Utils.filterJimple(processedBody.toString()));
  }

  @Test
  public void testConditionalBranchingWithNoConclusiveIfCondition() {
    Body.BodyBuilder builder = createBodyBuilder(2);
    Body originalBody = builder.build();
    new ConditionalBranchFolder().interceptBody(builder, new JavaView(Collections.emptyList()));
    Body processedBody = builder.build();

    assertEquals(Utils.bodyStmtsAsStrings(originalBody), Utils.bodyStmtsAsStrings(processedBody));
  }

  @Test
  public void testConditionalBranchFolderWithMultipleBranches() {
    AnalysisInputLocation inputLocation =
        new PathBasedAnalysisInputLocation.ClassFileBasedAnalysisInputLocation
            .ClassFileBasedAnalysisInputLocation(
            classFilePath,
            "",
            SourceType.Application,
            Arrays.asList(new CopyPropagator(), new ConditionalBranchFolder()));
    JavaView view = new JavaView(Collections.singletonList(inputLocation));

    final MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature(
                "ConditionalBranchFolderTest", "tc1", "void", Collections.emptyList());
    Body body = view.getMethod(methodSignature).get().getBody();
    assertFalse(body.getStmts().isEmpty());
    assertEquals(
        Stream.of(
                "ConditionalBranchFolderTest this",
                "unknown $stack3, $stack4, l1",
                "this := @this: ConditionalBranchFolderTest",
                "l1 = 1",
                "goto label1",
                "label1:",
                "goto label2",
                "label2:",
                "goto label3",
                "label3:",
                "$stack4 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $stack4.<java.io.PrintStream: void println(java.lang.String)>(\"lets see\")",
                "$stack3 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $stack3.<java.io.PrintStream: void println(java.lang.String)>(\"mid\")",
                "goto label4",
                "label4:",
                "return")
            .collect(Collectors.toList()),
        Utils.filterJimple(body.toString()));
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
    StmtPositionInfo noPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();

    JavaClassType stringType = factory.getClassType("java.lang.String");
    Local a = JavaJimple.newLocal("a", stringType);
    Local b = JavaJimple.newLocal("b", stringType);

    StringConstant stringConstant = javaJimple.newStringConstant("str");
    FallsThroughStmt strToA = JavaJimple.newAssignStmt(a, stringConstant, noPositionInfo);

    FallsThroughStmt strToB;
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
                    "java.lang.Object", "toString", "String", Collections.emptyList());
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

    BranchingStmt ifStmt = Jimple.newIfStmt(jEqExpr, noPositionInfo);
    Stmt reta = JavaJimple.newReturnStmt(a, noPositionInfo);
    Stmt retb = JavaJimple.newReturnStmt(b, noPositionInfo);

    Set<Local> locals = ImmutableUtils.immutableSet(a, b);

    Body.BodyBuilder bodyBuilder = Body.builder();
    final MutableStmtGraph stmtGraph = bodyBuilder.getStmtGraph();
    bodyBuilder.setLocals(locals);
    stmtGraph.putEdge(strToA, strToB);
    stmtGraph.putEdge(strToB, ifStmt);
    stmtGraph.putEdge(ifStmt, JIfStmt.FALSE_BRANCH_IDX, reta);
    stmtGraph.putEdge(ifStmt, JIfStmt.TRUE_BRANCH_IDX, retb);
    stmtGraph.setStartingStmt(strToA);
    bodyBuilder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("ab.c", "test", "void", Collections.emptyList()));
    return bodyBuilder;
  }
}
