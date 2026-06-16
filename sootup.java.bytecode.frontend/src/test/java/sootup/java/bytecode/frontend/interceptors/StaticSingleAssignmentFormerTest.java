package sootup.java.bytecode.frontend.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableBlockControlFlowGraph;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Trap;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
import sootup.core.util.ImmutableUtils;
import sootup.interceptors.StaticSingleAssignmentFormer;
import sootup.java.bytecode.frontend.inputlocation.ClassFileBasedAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

/**
 * @author Zun Wang
 */
public class StaticSingleAssignmentFormerTest {

  // Preparation
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();
  final String location = "src/test/resources/bugfixes/";

  JavaClassType intType = factory.getClassType("int");
  JavaClassType classType = factory.getClassType("Test");
  JavaClassType refType = factory.getClassType("ref");
  MethodSignature methodSignature =
      new MethodSignature(classType, "test", Collections.emptyList(), VoidType.getInstance());
  IdentityRef identityRef = JavaJimple.newThisRef(classType);
  ClassType exceptionType = factory.getClassType("Exception");
  IdentityRef caughtExceptionRef = JavaJimple.newCaughtExceptionRef();

  // build locals
  Local l0 = JavaJimple.newLocal("l0", classType);
  Local l1 = JavaJimple.newLocal("l1", intType);
  Local l2 = JavaJimple.newLocal("l2", intType);
  Local l3 = JavaJimple.newLocal("l3", intType);
  Local stack4 = JavaJimple.newLocal("stack4", refType);

  JIdentityStmt startingStmt = JavaJimple.newIdentityStmt(l0, identityRef, noStmtPositionInfo);
  JAssignStmt assign1tol1 =
      JavaJimple.newAssignStmt(l1, IntConstant.getInstance(1), noStmtPositionInfo);
  JAssignStmt assign1tol2 =
      JavaJimple.newAssignStmt(l2, IntConstant.getInstance(2), noStmtPositionInfo);
  JAssignStmt assign0tol3 =
      JavaJimple.newAssignStmt(l3, IntConstant.getInstance(0), noStmtPositionInfo);
  BranchingStmt ifStmt =
      JavaJimple.newIfStmt(
          JavaJimple.newLtExpr(l3, IntConstant.getInstance(100)), noStmtPositionInfo);
  BranchingStmt ifStmt2 =
      JavaJimple.newIfStmt(
          JavaJimple.newLtExpr(l2, IntConstant.getInstance(20)), noStmtPositionInfo);
  JReturnStmt returnStmt = JavaJimple.newReturnStmt(l2, noStmtPositionInfo);
  JAssignStmt assignl1tol2 = JavaJimple.newAssignStmt(l2, l1, noStmtPositionInfo);
  JAssignStmt assignl3plus1tol3 =
      JavaJimple.newAssignStmt(
          l3, JavaJimple.newAddExpr(l3, IntConstant.getInstance(1)), noStmtPositionInfo);
  JAssignStmt assignl3tol2 = JavaJimple.newAssignStmt(l2, l3, noStmtPositionInfo);
  JAssignStmt assignl3plus2tol3 =
      JavaJimple.newAssignStmt(
          l3, JavaJimple.newAddExpr(l3, IntConstant.getInstance(2)), noStmtPositionInfo);
  JGotoStmt gotoStmt1 = JavaJimple.newGotoStmt(noStmtPositionInfo);
  JGotoStmt gotoStmt2 = JavaJimple.newGotoStmt(noStmtPositionInfo);
  JGotoStmt gotoStmt3 = JavaJimple.newGotoStmt(noStmtPositionInfo);

  FallsThroughStmt handlerStmt =
      JavaJimple.newIdentityStmt(stack4, caughtExceptionRef, noStmtPositionInfo);
  JAssignStmt assign2tol2 =
      JavaJimple.newAssignStmt(l2, IntConstant.getInstance(2), noStmtPositionInfo);
  JGotoStmt gotoStmt = JavaJimple.newGotoStmt(noStmtPositionInfo);

  @Test
  public void testSSA() {
    StaticSingleAssignmentFormer ssa = new StaticSingleAssignmentFormer();
    Body.BodyBuilder builder = createBody();
    ssa.interceptBody(builder, new JavaView(Collections.emptyList()));
    String expectedBodyString =
        "{\n"
            + "    Test l0, l0#0;\n"
            + "    int l1, l1#1, l2, l2#10, l2#2, l2#4, l2#6, l2#8, l3, l3#11, l3#3, l3#5, l3#7, l3#9;\n"
            + "\n"
            + "\n"
            + "    l0#0 := @this: Test;\n"
            + "    l1#1 = 1;\n"
            + "    l2#2 = 2;\n"
            + "    l3#3 = 0;\n"
            + "\n"
            + "  label1:\n"
            + "    l2#4 = phi(l2#2, l2#10);\n"
            + "    l3#5 = phi(l3#3, l3#11);\n"
            + "\n"
            + "    if l3#5 < 100 goto label2;\n"
            + "\n"
            + "    return l2#4;\n"
            + "\n"
            + "  label2:\n"
            + "    if l2#4 < 20 goto label3;\n"
            + "    l2#6 = l3#5;\n"
            + "    l3#7 = l3#5 + 2;\n"
            + "\n"
            + "    goto label4;\n"
            + "\n"
            + "  label3:\n"
            + "    l2#8 = l1#1;\n"
            + "    l3#9 = l3#5 + 1;\n"
            + "\n"
            + "    goto label4;\n"
            + "\n"
            + "  label4:\n"
            + "    l2#10 = phi(l2#6, l2#8);\n"
            + "    l3#11 = phi(l3#7, l3#9);\n"
            + "\n"
            + "    goto label1;\n"
            + "}\n";

    assertEquals(expectedBodyString, builder.build().toString());
  }

  @Test
  public void testSSA2() {
    ClassType clazzType = factory.getClassType("TrapSSA");
    MethodSignature methodSignature =
        factory.getMethodSignature(clazzType, factory.getMainSubSignature());
    final Path path = Paths.get(location + "TrapSSA.class");
    PathBasedAnalysisInputLocation inputLocationWithSSA =
        new ClassFileBasedAnalysisInputLocation(
            path,
            "",
            SourceType.Application,
            Collections.singletonList(new StaticSingleAssignmentFormer()));
    JavaView viewSSA = new JavaView(inputLocationWithSSA);
    Body bodyAfterSSA = viewSSA.getMethod(methodSignature).get().getBody();
    String expectedBodyString =
        "{\n"
            + "    byte[] $stack9, $stack9#5, l1, l1#6;\n"
            + "    java.io.ByteArrayOutputStream $stack7, $stack7#1, l2, l2#2;\n"
            + "    java.io.PrintStream $stack11, $stack11#9;\n"
            + "    java.lang.String $stack10, $stack10#10, $stack8, $stack8#4;\n"
            + "    java.lang.String[] l0, l0#0;\n"
            + "    java.lang.Throwable l4, l4#12, l4#8, l6, l6#17;\n"
            + "    \"null\" l3, l3#13, l3#3;\n"
            + "    unknown $stack12, $stack12#16, $stack13, $stack13#14, $stack14, $stack14#11, $stack15, $stack15#7, l5, l5#15;\n"
            + "\n"
            + "\n"
            + "    l0#0 := @parameter0: java.lang.String[];\n"
            + "    $stack7#1 = new java.io.ByteArrayOutputStream;\n"
            + "    specialinvoke $stack7#1.<java.io.ByteArrayOutputStream: void <init>()>();\n"
            + "    l2#2 = $stack7#1;\n"
            + "    l3#3 = null;\n"
            + "\n"
            + "  label01:\n"
            + "    $stack8#4 = l0#0[0];\n"
            + "    $stack9#5 = virtualinvoke $stack8#4.<java.lang.String: byte[] getBytes(java.lang.String)>(\"UTF-8\");\n"
            + "    virtualinvoke l2#2.<java.io.ByteArrayOutputStream: void write(byte[])>($stack9#5);\n"
            + "    l1#6 = virtualinvoke l2#2.<java.io.ByteArrayOutputStream: byte[] toByteArray()>();\n"
            + "\n"
            + "  label02:\n"
            + "    if l2#2 == null goto label13;\n"
            + "\n"
            + "    if l3#3 == null goto label06;\n"
            + "\n"
            + "  label03:\n"
            + "    virtualinvoke l2#2.<java.io.ByteArrayOutputStream: void close()>();\n"
            + "\n"
            + "  label04:\n"
            + "    goto label13;\n"
            + "\n"
            + "  label05:\n"
            + "    $stack15#7 := @caughtexception;\n"
            + "    l4#8 = $stack15#7;\n"
            + "    virtualinvoke l3#3.<java.lang.Throwable: void addSuppressed(java.lang.Throwable)>(l4#8);\n"
            + "\n"
            + "    goto label13;\n"
            + "\n"
            + "  label06:\n"
            + "    virtualinvoke l2#2.<java.io.ByteArrayOutputStream: void close()>();\n"
            + "\n"
            + "    goto label13;\n"
            + "\n"
            + "  label07:\n"
            + "    $stack14#11 := @caughtexception;\n"
            + "    l4#12 = $stack14#11;\n"
            + "    l3#13 = l4#12;\n"
            + "\n"
            + "    throw l4#12;\n"
            + "\n"
            + "  label08:\n"
            + "    $stack13#14 := @caughtexception;\n"
            + "    l5#15 = $stack13#14;\n"
            + "\n"
            + "  label09:\n"
            + "    if l2#2 == null goto label15;\n"
            + "\n"
            + "    if l3#13 == null goto label14;\n"
            + "\n"
            + "  label10:\n"
            + "    virtualinvoke l2#2.<java.io.ByteArrayOutputStream: void close()>();\n"
            + "\n"
            + "  label11:\n"
            + "    goto label15;\n"
            + "\n"
            + "  label12:\n"
            + "    $stack12#16 := @caughtexception;\n"
            + "    l6#17 = $stack12#16;\n"
            + "    virtualinvoke l3#13.<java.lang.Throwable: void addSuppressed(java.lang.Throwable)>(l6#17);\n"
            + "\n"
            + "    goto label15;\n"
            + "\n"
            + "  label13:\n"
            + "    $stack11#9 = <java.lang.System: java.io.PrintStream out>;\n"
            + "    $stack10#10 = new java.lang.String;\n"
            + "    specialinvoke $stack10#10.<java.lang.String: void <init>(byte[],java.lang.String)>(l1#6, \"UTF-8\");\n"
            + "    virtualinvoke $stack11#9.<java.io.PrintStream: void println(java.lang.String)>($stack10#10);\n"
            + "\n"
            + "    return;\n"
            + "\n"
            + "  label14:\n"
            + "    virtualinvoke l2#2.<java.io.ByteArrayOutputStream: void close()>();\n"
            + "\n"
            + "  label15:\n"
            + "    throw l5#15;\n"
            + "\n"
            + " catch java.lang.Throwable from label01 to label02 with label07;\n"
            + " catch java.lang.Throwable from label03 to label04 with label05;\n"
            + " catch java.lang.Throwable from label07 to label09 with label08;\n"
            + " catch java.lang.Throwable from label10 to label11 with label12;\n"
            + "}\n";
    assertEquals(expectedBodyString, bodyAfterSSA.toString());
  }

  @Test
  public void testSSA3() {
    ClassType clazzType = factory.getClassType("ForLoopSSA");
    MethodSignature methodSignature =
        factory.getMethodSignature(clazzType, factory.getMainSubSignature());
    final Path path = Paths.get(location + "ForLoopSSA.class");
    PathBasedAnalysisInputLocation inputLocationWithSSA =
        new ClassFileBasedAnalysisInputLocation(
            path,
            "",
            SourceType.Application,
            Collections.singletonList(new StaticSingleAssignmentFormer()));
    JavaView viewSSA = new JavaView(inputLocationWithSSA);
    Body bodyAfterSSA = viewSSA.getMethod(methodSignature).get().getBody();

    String expectedBodyString =
        "{\n"
            + "    int $stack3, $stack3#5, l2, l2#11, l2#2, l2#4;\n"
            + "    java.io.PrintStream $stack4, $stack4#12;\n"
            + "    java.lang.String $stack6, $stack6#8, l1, l1#1, l1#10, l1#3;\n"
            + "    java.lang.StringBuilder $stack5, $stack5#6, $stack7, $stack7#7, $stack8, $stack8#9;\n"
            + "    java.lang.String[] l0, l0#0;\n"
            + "\n"
            + "\n"
            + "    l0#0 := @parameter0: java.lang.String[];\n"
            + "    l1#1 = \"\";\n"
            + "    l2#2 = 0;\n"
            + "\n"
            + "  label1:\n"
            + "    l1#3 = phi(l1#1, l1#10);\n"
            + "    l2#4 = phi(l2#2, l2#11);\n"
            + "    $stack3#5 = lengthof l0#0;\n"
            + "\n"
            + "    if l2#4 >= $stack3#5 goto label2;\n"
            + "    $stack5#6 = new java.lang.StringBuilder;\n"
            + "    specialinvoke $stack5#6.<java.lang.StringBuilder: void <init>()>();\n"
            + "    $stack7#7 = virtualinvoke $stack5#6.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(l1#3);\n"
            + "    $stack6#8 = l0#0[l2#4];\n"
            + "    $stack8#9 = virtualinvoke $stack7#7.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>($stack6#8);\n"
            + "    l1#10 = virtualinvoke $stack8#9.<java.lang.StringBuilder: java.lang.String toString()>();\n"
            + "    l2#11 = l2#4 + 1;\n"
            + "\n"
            + "    goto label1;\n"
            + "\n"
            + "  label2:\n"
            + "    $stack4#12 = <java.lang.System: java.io.PrintStream out>;\n"
            + "    virtualinvoke $stack4#12.<java.io.PrintStream: void println(java.lang.String)>(l1#3);\n"
            + "\n"
            + "    return;\n"
            + "}\n";

    assertEquals(expectedBodyString, bodyAfterSSA.toString());
  }

  @Test
  public void testTrappedSSA() {
    StaticSingleAssignmentFormer ssa = new StaticSingleAssignmentFormer();
    Body.BodyBuilder builder = createTrapBody();
    ssa.interceptBody(builder, new JavaView(Collections.emptyList()));
    String expectedBodyString =
        "{\n"
            + "    Test l0, l0#0;\n"
            + "    int l1, l1#1, l2, l2#12, l2#13, l2#2, l2#4, l2#6, l2#8, l2#9, l3, l3#10, l3#14, l3#3, l3#5, l3#7;\n"
            + "    ref stack4, stack4#11;\n"
            + "\n"
            + "\n"
            + "    l0#0 := @this: Test;\n"
            + "    l1#1 = 1;\n"
            + "    l2#2 = 2;\n"
            + "    l3#3 = 0;\n"
            + "\n"
            + "  label1:\n"
            + "    l2#4 = phi(l2#2, l2#13);\n"
            + "    l3#5 = phi(l3#3, l3#14);\n"
            + "\n"
            + "    if l3#5 < 100 goto label2;\n"
            + "\n"
            + "    return l2#4;\n"
            + "\n"
            + "  label2:\n"
            + "    if l2#4 < 20 goto label3;\n"
            + "    l2#6 = l3#5;\n"
            + "    l3#7 = l3#5 + 2;\n"
            + "\n"
            + "    goto label6;\n"
            + "\n"
            + "  label3:\n"
            + "    l2#8 = l1#1;\n"
            + "\n"
            + "  label4:\n"
            + "    l2#9 = phi(l2#8, l2#12);\n"
            + "    l3#10 = l3#5 + 1;\n"
            + "\n"
            + "    goto label6;\n"
            + "\n"
            + "  label5:\n"
            + "    stack4#11 := @caughtexception;\n"
            + "    l2#12 = 2;\n"
            + "\n"
            + "    goto label4;\n"
            + "\n"
            + "  label6:\n"
            + "    l2#13 = phi(l2#6, l2#9);\n"
            + "    l3#14 = phi(l3#7, l3#10);\n"
            + "\n"
            + "    goto label1;\n"
            + "\n"
            + " catch Exception from label3 to label4 with label5;\n"
            + "}\n";

    assertEquals(expectedBodyString, builder.build().toString());
  }

  /**
   *
   *
   * <pre>
   *    l0 := @this Test
   *    l1 = 1
   *    l2 = 2
   *    l3 = 0
   * label1:
   *    if l3 < 100 goto label2
   *    return l2
   * label2:
   *    if l2 < 20 goto label3
   *    l2 = l3
   *    l3 = l3 + 2
   *    goto label4;
   * label3:
   *    l2 = l1
   *    l3 = l3 + 1
   *    goto label4
   * label4:
   *    goto label1
   * </pre>
   */
  private Body.BodyBuilder createBody() {
    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    Body.BodyBuilder builder = Body.builder(graph);
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, l3);
    builder.setLocals(locals);

    // create blocks
    List<List<Stmt>> blocks =
        Arrays.asList(
            Collections.singletonList(startingStmt),
            Arrays.asList(assign1tol1, assign1tol2, assign0tol3),
            Collections.singletonList(ifStmt),
            Collections.singletonList(returnStmt),
            Collections.singletonList(ifStmt2),
            Arrays.asList(assignl3tol2, assignl3plus2tol3, gotoStmt1),
            Arrays.asList(assignl1tol2, assignl3plus1tol3, gotoStmt2),
            Collections.singletonList(gotoStmt));

    // create maps
    Map<BranchingStmt, List<Stmt>> successorMap = new HashMap<>();
    successorMap.put(ifStmt, Collections.singletonList(ifStmt2));
    successorMap.put(ifStmt2, Collections.singletonList(assignl1tol2));
    successorMap.put(gotoStmt1, Collections.singletonList(gotoStmt));
    successorMap.put(gotoStmt2, Collections.singletonList(gotoStmt));
    successorMap.put(gotoStmt, Collections.singletonList(ifStmt));

    graph.initializeWith(blocks, successorMap, Collections.emptyList());

    return builder;
  }

  /**
   *
   *
   * <pre>
   *    l0 := @this Test
   *    l1 = 1
   *    l2 = 2
   *    l3 = 0
   * label1:
   *    if l3 < 100 goto label2
   *    return l2
   * label2:
   *    if l2 < 20 goto label3
   *    l2 = l3
   *    l3 = l3 + 2
   *    goto label6
   * label3:
   *    l2 = l1
   * label4:
   *    l3 = l3 + 1
   *    goto label6
   * label5:
   *    stack4 := @caughtexception
   *    l2 = 2
   *    goto label4
   * label6:
   *    goto label1
   *
   * catch Exception from label3 to label4 with label5
   * </pre>
   */
  private Body.BodyBuilder createTrapBody() {
    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    Body.BodyBuilder builder = Body.builder(graph);
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, l3, stack4);
    builder.setLocals(locals);

    // create blocks
    List<List<Stmt>> blocks =
        Arrays.asList(
            Collections.singletonList(startingStmt),
            Arrays.asList(assign1tol1, assign1tol2, assign0tol3),
            Collections.singletonList(ifStmt),
            Collections.singletonList(returnStmt),
            Collections.singletonList(ifStmt2),
            Arrays.asList(assignl3tol2, assignl3plus2tol3, gotoStmt1),
            Collections.singletonList(assignl1tol2),
            Arrays.asList(assignl3plus1tol3, gotoStmt2),
            Arrays.asList(handlerStmt, assign2tol2, gotoStmt3),
            Collections.singletonList(gotoStmt));

    // create maps
    Map<BranchingStmt, List<Stmt>> successorMap = new HashMap<>();
    successorMap.put(ifStmt, Collections.singletonList(ifStmt2));
    successorMap.put(ifStmt2, Collections.singletonList(assignl1tol2));
    successorMap.put(gotoStmt1, Collections.singletonList(gotoStmt));
    successorMap.put(gotoStmt2, Collections.singletonList(gotoStmt));
    successorMap.put(gotoStmt3, Collections.singletonList(assignl3plus1tol3));
    successorMap.put(gotoStmt, Collections.singletonList(ifStmt));

    // create trap map
    Trap trap = new Trap(exceptionType, assignl1tol2, assignl3plus1tol3, handlerStmt);

    graph.initializeWith(blocks, successorMap, Collections.singletonList(trap));

    return builder;
  }
}
