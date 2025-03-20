package sootup.tests;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.ClassFileBasedAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

public class InsertBeforeAfterTest {

  String location =
      Paths.get(System.getProperty("user.dir")).getParent()
          + File.separator
          + "shared-test-resources/bugfixes/";
  final Path path = Paths.get(location + "TrapBlockCheck.class");
  PathBasedAnalysisInputLocation inputLocation =
      new ClassFileBasedAnalysisInputLocation(
          path, "", SourceType.Application, Collections.emptyList());
  JavaView view = new JavaView(inputLocation);
  JavaIdentifierFactory factory = view.getIdentifierFactory();
  ClassType clazzType = factory.getClassType("TrapBlockCheck");
  MethodSignature methodSignature =
      factory.getMethodSignature(clazzType, "test", "void", Collections.emptyList());
  Body body = view.getMethod(methodSignature).get().getBody();
  Set<Local> locals = body.getLocals();

  // inserted Stmts
  JavaClassType intType = factory.getClassType("int");
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();
  Local l5 = JavaJimple.newLocal("l5", intType);
  Local l6 = JavaJimple.newLocal("l6", intType);
  FallsThroughStmt assign1tol5 =
      JavaJimple.newAssignStmt(l5, IntConstant.getInstance(1), noStmtPositionInfo);
  FallsThroughStmt assign2tol6 =
      JavaJimple.newAssignStmt(l6, IntConstant.getInstance(2), noStmtPositionInfo);

  @Test
  public void testInsertBeforeBlockHead1() {

    MutableBlockStmtGraph graph = new MutableBlockStmtGraph(body.getStmtGraph());
    List<Stmt> stmts = graph.getStmts();
    // head of a block
    String s = "l2 = 0";
    Stmt beforeStmt = null;
    for (Stmt stmt : stmts) {
      if (stmt.toString().equals(s)) {
        beforeStmt = stmt;
        break;
      }
    }

    Set<Local> newLocals = new HashSet<>(locals);
    newLocals.add(l5);
    newLocals.add(l6);

    BasicBlock<?> newBlock =
        graph.insertBefore(
            beforeStmt, Arrays.asList(assign1tol5, assign2tol6), Collections.emptyMap());
    Body.BodyBuilder builder = Body.builder(graph);
    builder.setLocals(newLocals);
    builder.setMethodSignature(methodSignature);
    Assertions.assertEquals(8, graph.getBlocks().size());
    Assertions.assertEquals(2, newBlock.getStmtCount());
    Assertions.assertEquals(2, graph.getBlockOf(beforeStmt).getStmtCount());
    Assertions.assertTrue(newBlock.getHead() == assign1tol5);
    Assertions.assertTrue(newBlock.getTail() == assign2tol6);
    Assertions.assertEquals(0, newBlock.getExceptionalSuccessors().size());
    String expectedBody =
        "{\n"
            + "    TrapBlockCheck this;\n"
            + "    int l5, l6;\n"
            + "    unknown $stack3, $stack4, l1, l2;\n"
            + "\n"
            + "\n"
            + "    this := @this: TrapBlockCheck;\n"
            + "    l1 = 0;\n"
            + "    l5 = 1;\n"
            + "    l6 = 2;\n"
            + "\n"
            + "  label1:\n"
            + "    l2 = 0;\n"
            + "\n"
            + "    if l1 != l2 goto label3;\n"
            + "    l1 = l1 + 1;\n"
            + "\n"
            + "    goto label4;\n"
            + "\n"
            + "  label2:\n"
            + "    $stack3 := @caughtexception;\n"
            + "    l2 = $stack3;\n"
            + "    $stack4 = new java.lang.RuntimeException;\n"
            + "    specialinvoke $stack4.<java.lang.RuntimeException: void <init>(java.lang.String)>(\"error rises!\");\n"
            + "\n"
            + "    throw $stack4;\n"
            + "\n"
            + "  label3:\n"
            + "    l2 = l2 + 1;\n"
            + "\n"
            + "  label4:\n"
            + "    goto label5;\n"
            + "\n"
            + "  label5:\n"
            + "    return;\n"
            + "\n"
            + " catch java.lang.Exception from label1 to label2 with label2;\n"
            + " catch java.lang.Exception from label3 to label4 with label2;\n"
            + "}\n";
    Assertions.assertEquals(expectedBody, builder.build().toString());
  }

  @Test
  public void testInsertBeforeBlockHead2() {

    MutableBlockStmtGraph graph = new MutableBlockStmtGraph(body.getStmtGraph());
    List<Stmt> stmts = graph.getStmts();
    // head of a block
    String s = "l2 = 0";
    Stmt beforeStmt = null;
    for (Stmt stmt : stmts) {
      if (stmt.toString().equals(s)) {
        beforeStmt = stmt;
        break;
      }
    }
    Map<ClassType, Stmt> trapMap = graph.exceptionalSuccessors(beforeStmt);
    BasicBlock<?> newBlock =
        graph.insertBefore(beforeStmt, Arrays.asList(assign1tol5, assign2tol6), trapMap);

    Set<Local> newLocals = new HashSet<>(locals);
    newLocals.add(l5);
    newLocals.add(l6);
    Body.BodyBuilder builder = Body.builder(graph);
    builder.setLocals(newLocals);
    builder.setMethodSignature(methodSignature);

    Assertions.assertEquals(7, graph.getBlocks().size());
    Assertions.assertEquals(4, newBlock.getStmtCount());
    Assertions.assertTrue(graph.getBlockOf(beforeStmt) == newBlock);
    Assertions.assertTrue(newBlock.getHead() == assign1tol5);
    Assertions.assertTrue(newBlock.getTail().toString().equals("if l1 != l2"));
    Assertions.assertEquals(1, newBlock.getExceptionalSuccessors().size());
    String exceptedBody =
        "{\n"
            + "    TrapBlockCheck this;\n"
            + "    int l5, l6;\n"
            + "    unknown $stack3, $stack4, l1, l2;\n"
            + "\n"
            + "\n"
            + "    this := @this: TrapBlockCheck;\n"
            + "    l1 = 0;\n"
            + "\n"
            + "  label1:\n"
            + "    l5 = 1;\n"
            + "    l6 = 2;\n"
            + "    l2 = 0;\n"
            + "\n"
            + "    if l1 != l2 goto label3;\n"
            + "    l1 = l1 + 1;\n"
            + "\n"
            + "    goto label4;\n"
            + "\n"
            + "  label2:\n"
            + "    $stack3 := @caughtexception;\n"
            + "    l2 = $stack3;\n"
            + "    $stack4 = new java.lang.RuntimeException;\n"
            + "    specialinvoke $stack4.<java.lang.RuntimeException: void <init>(java.lang.String)>(\"error rises!\");\n"
            + "\n"
            + "    throw $stack4;\n"
            + "\n"
            + "  label3:\n"
            + "    l2 = l2 + 1;\n"
            + "\n"
            + "  label4:\n"
            + "    goto label5;\n"
            + "\n"
            + "  label5:\n"
            + "    return;\n"
            + "\n"
            + " catch java.lang.Exception from label1 to label2 with label2;\n"
            + " catch java.lang.Exception from label3 to label4 with label2;\n"
            + "}\n";
    Assertions.assertEquals(exceptedBody, builder.build().toString());
  }

  @Test
  public void testInsertBeforeBlockMiddle1() {

    MutableBlockStmtGraph graph = new MutableBlockStmtGraph(body.getStmtGraph());
    List<Stmt> stmts = graph.getStmts();
    // middle stmt of a block
    String s = "if l1 != l2";
    Stmt beforeStmt = null;
    for (Stmt stmt : stmts) {
      if (stmt.toString().equals(s)) {
        beforeStmt = stmt;
        break;
      }
    }

    Set<Local> newLocals = new HashSet<>(locals);
    newLocals.add(l5);
    newLocals.add(l6);
    Map<ClassType, Stmt> trapMap = graph.exceptionalSuccessors(beforeStmt);
    BasicBlock<?> newBlock =
        graph.insertBefore(beforeStmt, Arrays.asList(assign1tol5, assign2tol6), trapMap);
    Body.BodyBuilder builder = Body.builder(graph);
    builder.setLocals(newLocals);
    builder.setMethodSignature(methodSignature);

    Assertions.assertEquals(7, graph.getBlocks().size());
    Assertions.assertEquals(4, newBlock.getStmtCount());
    Assertions.assertTrue(graph.getBlockOf(beforeStmt) == newBlock);
    Assertions.assertTrue(newBlock.getHead().toString().equals("l2 = 0"));
    Assertions.assertTrue(newBlock.getTail().toString().equals("if l1 != l2"));
    Assertions.assertEquals(1, newBlock.getExceptionalSuccessors().size());

    String exceptedBody =
        "{\n"
            + "    TrapBlockCheck this;\n"
            + "    int l5, l6;\n"
            + "    unknown $stack3, $stack4, l1, l2;\n"
            + "\n"
            + "\n"
            + "    this := @this: TrapBlockCheck;\n"
            + "    l1 = 0;\n"
            + "\n"
            + "  label1:\n"
            + "    l2 = 0;\n"
            + "    l5 = 1;\n"
            + "    l6 = 2;\n"
            + "\n"
            + "    if l1 != l2 goto label3;\n"
            + "    l1 = l1 + 1;\n"
            + "\n"
            + "    goto label4;\n"
            + "\n"
            + "  label2:\n"
            + "    $stack3 := @caughtexception;\n"
            + "    l2 = $stack3;\n"
            + "    $stack4 = new java.lang.RuntimeException;\n"
            + "    specialinvoke $stack4.<java.lang.RuntimeException: void <init>(java.lang.String)>(\"error rises!\");\n"
            + "\n"
            + "    throw $stack4;\n"
            + "\n"
            + "  label3:\n"
            + "    l2 = l2 + 1;\n"
            + "\n"
            + "  label4:\n"
            + "    goto label5;\n"
            + "\n"
            + "  label5:\n"
            + "    return;\n"
            + "\n"
            + " catch java.lang.Exception from label1 to label2 with label2;\n"
            + " catch java.lang.Exception from label3 to label4 with label2;\n"
            + "}\n";
    Assertions.assertEquals(exceptedBody, builder.build().toString());
  }

  @Test
  public void testInsertBeforeBlockMiddle2() {

    MutableBlockStmtGraph graph = new MutableBlockStmtGraph(body.getStmtGraph());
    List<Stmt> stmts = graph.getStmts();
    // middle stmt of a block
    String s = "if l1 != l2";
    Stmt beforeStmt = null;
    for (Stmt stmt : stmts) {
      if (stmt.toString().equals(s)) {
        beforeStmt = stmt;
        break;
      }
    }

    Set<Local> newLocals = new HashSet<>(locals);
    newLocals.add(l5);
    newLocals.add(l6);
    BasicBlock<?> newBlock =
        graph.insertBefore(
            beforeStmt, Arrays.asList(assign1tol5, assign2tol6), Collections.emptyMap());
    Body.BodyBuilder builder = Body.builder(graph);
    builder.setLocals(newLocals);
    builder.setMethodSignature(methodSignature);

    Assertions.assertEquals(9, graph.getBlocks().size());
    Assertions.assertEquals(2, newBlock.getStmtCount());
    Assertions.assertTrue(graph.getBlockOf(beforeStmt) != graph.getBlockOf(assign1tol5));
    Assertions.assertTrue(newBlock.getHead().toString().equals("l5 = 1"));
    Assertions.assertTrue(newBlock.getTail().toString().equals("l6 = 2"));
    Assertions.assertEquals(0, newBlock.getExceptionalSuccessors().size());

    String exceptedBody =
        "{\n"
            + "    TrapBlockCheck this;\n"
            + "    int l5, l6;\n"
            + "    unknown $stack3, $stack4, l1, l2;\n"
            + "\n"
            + "\n"
            + "    this := @this: TrapBlockCheck;\n"
            + "    l1 = 0;\n"
            + "\n"
            + "  label1:\n"
            + "    l2 = 0;\n"
            + "\n"
            + "  label2:\n"
            + "    l5 = 1;\n"
            + "    l6 = 2;\n"
            + "\n"
            + "  label3:\n"
            + "    if l1 != l2 goto label5;\n"
            + "    l1 = l1 + 1;\n"
            + "\n"
            + "    goto label6;\n"
            + "\n"
            + "  label4:\n"
            + "    $stack3 := @caughtexception;\n"
            + "    l2 = $stack3;\n"
            + "    $stack4 = new java.lang.RuntimeException;\n"
            + "    specialinvoke $stack4.<java.lang.RuntimeException: void <init>(java.lang.String)>(\"error rises!\");\n"
            + "\n"
            + "    throw $stack4;\n"
            + "\n"
            + "  label5:\n"
            + "    l2 = l2 + 1;\n"
            + "\n"
            + "  label6:\n"
            + "    goto label7;\n"
            + "\n"
            + "  label7:\n"
            + "    return;\n"
            + "\n"
            + " catch java.lang.Exception from label1 to label2 with label4;\n"
            + " catch java.lang.Exception from label3 to label4 with label4;\n"
            + " catch java.lang.Exception from label5 to label6 with label4;\n"
            + "}\n";
    Assertions.assertEquals(exceptedBody, builder.build().toString());
  }

  @Test
  public void testInsertAfterBlockTail1() {

    MutableBlockStmtGraph graph = new MutableBlockStmtGraph(body.getStmtGraph());
    List<Stmt> stmts = graph.getStmts();
    // head of a block
    String s = "l2 = l2 + 1";
    Stmt afterStmt = null;
    for (Stmt stmt : stmts) {
      if (stmt.toString().equals(s)) {
        afterStmt = stmt;
        break;
      }
    }

    Set<Local> newLocals = new HashSet<>(locals);
    newLocals.add(l5);
    newLocals.add(l6);

    BasicBlock<?> newBlock =
        graph.insertAfter(
            afterStmt, Arrays.asList(assign1tol5, assign2tol6), Collections.emptyMap());
    Body.BodyBuilder builder = Body.builder(graph);
    builder.setLocals(newLocals);
    builder.setMethodSignature(methodSignature);

    Assertions.assertEquals(8, graph.getBlocks().size());
    Assertions.assertEquals(2, newBlock.getStmtCount());
    Assertions.assertEquals(1, graph.getBlockOf(afterStmt).getStmtCount());
    Assertions.assertTrue(newBlock.getHead() == assign1tol5);
    Assertions.assertTrue(newBlock.getTail() == assign2tol6);
    Assertions.assertEquals(0, newBlock.getExceptionalSuccessors().size());
    String expectedBody =
        "{\n"
            + "    TrapBlockCheck this;\n"
            + "    int l5, l6;\n"
            + "    unknown $stack3, $stack4, l1, l2;\n"
            + "\n"
            + "\n"
            + "    this := @this: TrapBlockCheck;\n"
            + "    l1 = 0;\n"
            + "\n"
            + "  label1:\n"
            + "    l2 = 0;\n"
            + "\n"
            + "    if l1 != l2 goto label3;\n"
            + "    l1 = l1 + 1;\n"
            + "\n"
            + "    goto label5;\n"
            + "\n"
            + "  label2:\n"
            + "    $stack3 := @caughtexception;\n"
            + "    l2 = $stack3;\n"
            + "    $stack4 = new java.lang.RuntimeException;\n"
            + "    specialinvoke $stack4.<java.lang.RuntimeException: void <init>(java.lang.String)>(\"error rises!\");\n"
            + "\n"
            + "    throw $stack4;\n"
            + "\n"
            + "  label3:\n"
            + "    l2 = l2 + 1;\n"
            + "\n"
            + "  label4:\n"
            + "    l5 = 1;\n"
            + "    l6 = 2;\n"
            + "\n"
            + "  label5:\n"
            + "    goto label6;\n"
            + "\n"
            + "  label6:\n"
            + "    return;\n"
            + "\n"
            + " catch java.lang.Exception from label1 to label2 with label2;\n"
            + " catch java.lang.Exception from label3 to label4 with label2;\n"
            + "}\n";
    Assertions.assertEquals(expectedBody, builder.build().toString());
  }

  @Test
  public void testInsertAfterBlockTail2() {

    MutableBlockStmtGraph graph = new MutableBlockStmtGraph(body.getStmtGraph());
    List<Stmt> stmts = graph.getStmts();
    // head of a block
    String s = "l2 = l2 + 1";
    Stmt afterStmt = null;
    for (Stmt stmt : stmts) {
      if (stmt.toString().equals(s)) {
        afterStmt = stmt;
        break;
      }
    }
    Map<ClassType, Stmt> trapMap = graph.exceptionalSuccessors(afterStmt);

    Set<Local> newLocals = new HashSet<>(locals);
    newLocals.add(l5);
    newLocals.add(l6);

    BasicBlock<?> newBlock =
        graph.insertAfter(afterStmt, Arrays.asList(assign1tol5, assign2tol6), trapMap);
    Body.BodyBuilder builder = Body.builder(graph);
    builder.setLocals(newLocals);
    builder.setMethodSignature(methodSignature);

    Assertions.assertEquals(7, graph.getBlocks().size());
    Assertions.assertEquals(3, newBlock.getStmtCount());
    Assertions.assertEquals(3, graph.getBlockOf(afterStmt).getStmtCount());
    Assertions.assertTrue(newBlock.getHead() == afterStmt);
    Assertions.assertTrue(newBlock.getTail() == assign2tol6);
    Assertions.assertEquals(1, newBlock.getExceptionalSuccessors().size());
    String expectedBody =
        "{\n"
            + "    TrapBlockCheck this;\n"
            + "    int l5, l6;\n"
            + "    unknown $stack3, $stack4, l1, l2;\n"
            + "\n"
            + "\n"
            + "    this := @this: TrapBlockCheck;\n"
            + "    l1 = 0;\n"
            + "\n"
            + "  label1:\n"
            + "    l2 = 0;\n"
            + "\n"
            + "    if l1 != l2 goto label3;\n"
            + "    l1 = l1 + 1;\n"
            + "\n"
            + "    goto label4;\n"
            + "\n"
            + "  label2:\n"
            + "    $stack3 := @caughtexception;\n"
            + "    l2 = $stack3;\n"
            + "    $stack4 = new java.lang.RuntimeException;\n"
            + "    specialinvoke $stack4.<java.lang.RuntimeException: void <init>(java.lang.String)>(\"error rises!\");\n"
            + "\n"
            + "    throw $stack4;\n"
            + "\n"
            + "  label3:\n"
            + "    l2 = l2 + 1;\n"
            + "    l5 = 1;\n"
            + "    l6 = 2;\n"
            + "\n"
            + "  label4:\n"
            + "    goto label5;\n"
            + "\n"
            + "  label5:\n"
            + "    return;\n"
            + "\n"
            + " catch java.lang.Exception from label1 to label2 with label2;\n"
            + " catch java.lang.Exception from label3 to label4 with label2;\n"
            + "}\n";
    Assertions.assertEquals(expectedBody, builder.build().toString());
  }

  @Test
  public void testInsertAfterBlockMiddle1() {

    MutableBlockStmtGraph graph = new MutableBlockStmtGraph(body.getStmtGraph());
    List<Stmt> stmts = graph.getStmts();
    // middle stmt of a block
    String s = "l2 = 0";
    Stmt afterStmt = null;
    for (Stmt stmt : stmts) {
      if (stmt.toString().equals(s)) {
        afterStmt = stmt;
        break;
      }
    }

    Set<Local> newLocals = new HashSet<>(locals);
    newLocals.add(l5);
    newLocals.add(l6);
    Map<ClassType, Stmt> trapMap = graph.exceptionalSuccessors(afterStmt);
    BasicBlock<?> newBlock =
        graph.insertAfter(afterStmt, Arrays.asList(assign1tol5, assign2tol6), trapMap);
    Body.BodyBuilder builder = Body.builder(graph);
    builder.setLocals(newLocals);
    builder.setMethodSignature(methodSignature);

    Assertions.assertEquals(7, graph.getBlocks().size());
    Assertions.assertEquals(4, newBlock.getStmtCount());
    Assertions.assertTrue(graph.getBlockOf(afterStmt) == newBlock);
    Assertions.assertTrue(newBlock.getHead().toString().equals("l2 = 0"));
    Assertions.assertTrue(newBlock.getTail().toString().equals("if l1 != l2"));
    Assertions.assertEquals(1, newBlock.getExceptionalSuccessors().size());

    String exceptedBody =
        "{\n"
            + "    TrapBlockCheck this;\n"
            + "    int l5, l6;\n"
            + "    unknown $stack3, $stack4, l1, l2;\n"
            + "\n"
            + "\n"
            + "    this := @this: TrapBlockCheck;\n"
            + "    l1 = 0;\n"
            + "\n"
            + "  label1:\n"
            + "    l2 = 0;\n"
            + "    l5 = 1;\n"
            + "    l6 = 2;\n"
            + "\n"
            + "    if l1 != l2 goto label3;\n"
            + "    l1 = l1 + 1;\n"
            + "\n"
            + "    goto label4;\n"
            + "\n"
            + "  label2:\n"
            + "    $stack3 := @caughtexception;\n"
            + "    l2 = $stack3;\n"
            + "    $stack4 = new java.lang.RuntimeException;\n"
            + "    specialinvoke $stack4.<java.lang.RuntimeException: void <init>(java.lang.String)>(\"error rises!\");\n"
            + "\n"
            + "    throw $stack4;\n"
            + "\n"
            + "  label3:\n"
            + "    l2 = l2 + 1;\n"
            + "\n"
            + "  label4:\n"
            + "    goto label5;\n"
            + "\n"
            + "  label5:\n"
            + "    return;\n"
            + "\n"
            + " catch java.lang.Exception from label1 to label2 with label2;\n"
            + " catch java.lang.Exception from label3 to label4 with label2;\n"
            + "}\n";
    Assertions.assertEquals(exceptedBody, builder.build().toString());
  }

  @Test
  public void testInsertAfterBlockMiddle2() {

    MutableBlockStmtGraph graph = new MutableBlockStmtGraph(body.getStmtGraph());
    List<Stmt> stmts = graph.getStmts();
    // middle stmt of a block
    String s = "l2 = 0";
    Stmt afterStmt = null;
    for (Stmt stmt : stmts) {
      if (stmt.toString().equals(s)) {
        afterStmt = stmt;
        break;
      }
    }

    Set<Local> newLocals = new HashSet<>(locals);
    newLocals.add(l5);
    newLocals.add(l6);
    BasicBlock<?> newBlock =
        graph.insertAfter(
            afterStmt, Arrays.asList(assign1tol5, assign2tol6), Collections.emptyMap());
    Body.BodyBuilder builder = Body.builder(graph);
    builder.setLocals(newLocals);
    builder.setMethodSignature(methodSignature);

    Assertions.assertEquals(9, graph.getBlocks().size());
    Assertions.assertEquals(2, newBlock.getStmtCount());
    Assertions.assertTrue(graph.getBlockOf(afterStmt) != graph.getBlockOf(assign1tol5));
    Assertions.assertTrue(newBlock.getHead().toString().equals("l5 = 1"));
    Assertions.assertTrue(newBlock.getTail().toString().equals("l6 = 2"));
    Assertions.assertEquals(0, newBlock.getExceptionalSuccessors().size());

    String exceptedBody =
        "{\n"
            + "    TrapBlockCheck this;\n"
            + "    int l5, l6;\n"
            + "    unknown $stack3, $stack4, l1, l2;\n"
            + "\n"
            + "\n"
            + "    this := @this: TrapBlockCheck;\n"
            + "    l1 = 0;\n"
            + "\n"
            + "  label1:\n"
            + "    l2 = 0;\n"
            + "\n"
            + "  label2:\n"
            + "    l5 = 1;\n"
            + "    l6 = 2;\n"
            + "\n"
            + "  label3:\n"
            + "    if l1 != l2 goto label5;\n"
            + "    l1 = l1 + 1;\n"
            + "\n"
            + "    goto label6;\n"
            + "\n"
            + "  label4:\n"
            + "    $stack3 := @caughtexception;\n"
            + "    l2 = $stack3;\n"
            + "    $stack4 = new java.lang.RuntimeException;\n"
            + "    specialinvoke $stack4.<java.lang.RuntimeException: void <init>(java.lang.String)>(\"error rises!\");\n"
            + "\n"
            + "    throw $stack4;\n"
            + "\n"
            + "  label5:\n"
            + "    l2 = l2 + 1;\n"
            + "\n"
            + "  label6:\n"
            + "    goto label7;\n"
            + "\n"
            + "  label7:\n"
            + "    return;\n"
            + "\n"
            + " catch java.lang.Exception from label1 to label2 with label4;\n"
            + " catch java.lang.Exception from label3 to label4 with label4;\n"
            + " catch java.lang.Exception from label5 to label6 with label4;\n"
            + "}\n";
    Assertions.assertEquals(exceptedBody, builder.build().toString());
  }
}
