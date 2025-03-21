package sootup.tests;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.ClassFileBasedAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

public class NestedTryCatchFlowTest {

  String location =
      Paths.get(System.getProperty("user.dir")).getParent()
          + File.separator
          + "shared-test-resources/bugfixes/";
  final Path path = Paths.get(location + "NestedTryCatchFlow.class");
  PathBasedAnalysisInputLocation inputLocation =
      new ClassFileBasedAnalysisInputLocation(
          path, "", SourceType.Application, Collections.emptyList());
  JavaView view = new JavaView(inputLocation);
  JavaIdentifierFactory factory = view.getIdentifierFactory();
  ClassType clazzType = factory.getClassType("NestedTryCatchFlow");
  MethodSignature methodSignature =
      factory.getMethodSignature(
          clazzType, "test_nested_try_catch_2", "int", Collections.singletonList("int"));
  Body body = view.getMethod(methodSignature).get().getBody();

  @Test
  public void testNestedTryCatchFlow1() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph(body.getStmtGraph());
    Map<Integer, BasicBlock<?>> returnValToBlockMap = new HashMap<>();
    for (BasicBlock<?> block : graph.getBlocks()) {
      for (Stmt stmt : block.getStmts()) {
        if (!(stmt instanceof JReturnStmt)) continue;
        JReturnStmt returnStmt = (JReturnStmt) stmt;
        int retVal = getRetVal(block, returnStmt);
        returnValToBlockMap.put(retVal, block);
      }
    }

    boolean anyMatch0_1 =
        containsExceptionalBlock(returnValToBlockMap.get(0), returnValToBlockMap.get(1));
    boolean anyMatch0_2 =
        containsExceptionalBlock(returnValToBlockMap.get(0), returnValToBlockMap.get(2));
    boolean anyMatch0_3 =
        containsExceptionalBlock(returnValToBlockMap.get(0), returnValToBlockMap.get(3));
    boolean anyMatch0_4 =
        containsExceptionalBlock(returnValToBlockMap.get(0), returnValToBlockMap.get(4));
    boolean anyMatch0_5 =
        containsExceptionalBlock(returnValToBlockMap.get(0), returnValToBlockMap.get(5));
    boolean anyMatch0_6 =
        containsExceptionalBlock(returnValToBlockMap.get(0), returnValToBlockMap.get(6));
    Assertions.assertFalse(anyMatch0_1);
    Assertions.assertTrue(anyMatch0_2);
    Assertions.assertTrue(anyMatch0_3);
    Assertions.assertFalse(anyMatch0_4);
    Assertions.assertTrue(anyMatch0_5);
    Assertions.assertTrue(anyMatch0_6);

    boolean anyMatch1_0 =
        containsExceptionalBlock(returnValToBlockMap.get(1), returnValToBlockMap.get(0));
    boolean anyMatch1_2 =
        containsExceptionalBlock(returnValToBlockMap.get(1), returnValToBlockMap.get(2));
    boolean anyMatch1_3 =
        containsExceptionalBlock(returnValToBlockMap.get(1), returnValToBlockMap.get(3));
    boolean anyMatch1_4 =
        containsExceptionalBlock(returnValToBlockMap.get(1), returnValToBlockMap.get(4));
    boolean anyMatch1_5 =
        containsExceptionalBlock(returnValToBlockMap.get(1), returnValToBlockMap.get(5));
    boolean anyMatch1_6 =
        containsExceptionalBlock(returnValToBlockMap.get(1), returnValToBlockMap.get(6));
    Assertions.assertFalse(anyMatch1_0);
    Assertions.assertTrue(anyMatch1_2);
    Assertions.assertTrue(anyMatch1_3);
    Assertions.assertFalse(anyMatch1_4);
    Assertions.assertTrue(anyMatch1_5);
    Assertions.assertTrue(anyMatch1_6);

    boolean anyMatch2_0 =
        containsExceptionalBlock(returnValToBlockMap.get(2), returnValToBlockMap.get(0));
    boolean anyMatch2_1 =
        containsExceptionalBlock(returnValToBlockMap.get(2), returnValToBlockMap.get(1));
    boolean anyMatch2_3 =
        containsExceptionalBlock(returnValToBlockMap.get(2), returnValToBlockMap.get(3));
    boolean anyMatch2_4 =
        containsExceptionalBlock(returnValToBlockMap.get(2), returnValToBlockMap.get(4));
    boolean anyMatch2_5 =
        containsExceptionalBlock(returnValToBlockMap.get(2), returnValToBlockMap.get(5));
    boolean anyMatch2_6 =
        containsExceptionalBlock(returnValToBlockMap.get(2), returnValToBlockMap.get(6));
    Assertions.assertFalse(anyMatch2_0);
    Assertions.assertFalse(anyMatch2_1);
    Assertions.assertFalse(anyMatch2_3);
    Assertions.assertTrue(anyMatch2_4);
    Assertions.assertTrue(anyMatch2_5);
    Assertions.assertTrue(anyMatch2_6);

    boolean anyMatch3_0 =
        containsExceptionalBlock(returnValToBlockMap.get(3), returnValToBlockMap.get(0));
    boolean anyMatch3_1 =
        containsExceptionalBlock(returnValToBlockMap.get(3), returnValToBlockMap.get(1));
    boolean anyMatch3_2 =
        containsExceptionalBlock(returnValToBlockMap.get(3), returnValToBlockMap.get(2));
    boolean anyMatch3_4 =
        containsExceptionalBlock(returnValToBlockMap.get(3), returnValToBlockMap.get(4));
    boolean anyMatch3_5 =
        containsExceptionalBlock(returnValToBlockMap.get(3), returnValToBlockMap.get(5));
    boolean anyMatch3_6 =
        containsExceptionalBlock(returnValToBlockMap.get(3), returnValToBlockMap.get(6));
    Assertions.assertFalse(anyMatch3_0);
    Assertions.assertFalse(anyMatch3_1);
    Assertions.assertFalse(anyMatch3_2);
    Assertions.assertTrue(anyMatch3_4);
    Assertions.assertTrue(anyMatch3_5);
    Assertions.assertTrue(anyMatch3_6);

    boolean anyMatch4_0 =
        containsExceptionalBlock(returnValToBlockMap.get(4), returnValToBlockMap.get(0));
    boolean anyMatch4_1 =
        containsExceptionalBlock(returnValToBlockMap.get(4), returnValToBlockMap.get(1));
    boolean anyMatch4_2 =
        containsExceptionalBlock(returnValToBlockMap.get(4), returnValToBlockMap.get(2));
    boolean anyMatch4_3 =
        containsExceptionalBlock(returnValToBlockMap.get(4), returnValToBlockMap.get(3));
    boolean anyMatch4_5 =
        containsExceptionalBlock(returnValToBlockMap.get(4), returnValToBlockMap.get(5));
    boolean anyMatch4_6 =
        containsExceptionalBlock(returnValToBlockMap.get(4), returnValToBlockMap.get(6));
    Assertions.assertFalse(anyMatch4_0);
    Assertions.assertFalse(anyMatch4_1);
    Assertions.assertFalse(anyMatch4_2);
    Assertions.assertFalse(anyMatch4_3);
    Assertions.assertTrue(anyMatch4_5);
    Assertions.assertTrue(anyMatch4_6);

    boolean anyMatch5_0 =
        containsExceptionalBlock(returnValToBlockMap.get(5), returnValToBlockMap.get(0));
    boolean anyMatch5_1 =
        containsExceptionalBlock(returnValToBlockMap.get(5), returnValToBlockMap.get(1));
    boolean anyMatch5_2 =
        containsExceptionalBlock(returnValToBlockMap.get(5), returnValToBlockMap.get(2));
    boolean anyMatch5_3 =
        containsExceptionalBlock(returnValToBlockMap.get(5), returnValToBlockMap.get(3));
    boolean anyMatch5_4 =
        containsExceptionalBlock(returnValToBlockMap.get(5), returnValToBlockMap.get(4));
    boolean anyMatch5_6 =
        containsExceptionalBlock(returnValToBlockMap.get(5), returnValToBlockMap.get(6));
    Assertions.assertFalse(anyMatch5_0);
    Assertions.assertFalse(anyMatch5_1);
    Assertions.assertFalse(anyMatch5_2);
    Assertions.assertFalse(anyMatch5_3);
    Assertions.assertFalse(anyMatch5_4);
    Assertions.assertFalse(anyMatch5_6);

    boolean anyMatch6_0 =
        containsExceptionalBlock(returnValToBlockMap.get(6), returnValToBlockMap.get(0));
    boolean anyMatch6_1 =
        containsExceptionalBlock(returnValToBlockMap.get(6), returnValToBlockMap.get(1));
    boolean anyMatch6_2 =
        containsExceptionalBlock(returnValToBlockMap.get(6), returnValToBlockMap.get(2));
    boolean anyMatch6_3 =
        containsExceptionalBlock(returnValToBlockMap.get(6), returnValToBlockMap.get(3));
    boolean anyMatch6_4 =
        containsExceptionalBlock(returnValToBlockMap.get(6), returnValToBlockMap.get(4));
    boolean anyMatch6_5 =
        containsExceptionalBlock(returnValToBlockMap.get(6), returnValToBlockMap.get(5));
    Assertions.assertFalse(anyMatch6_0);
    Assertions.assertFalse(anyMatch6_1);
    Assertions.assertFalse(anyMatch6_2);
    Assertions.assertFalse(anyMatch6_3);
    Assertions.assertFalse(anyMatch6_4);
    Assertions.assertFalse(anyMatch6_5);
  }

  private int getRetVal(BasicBlock<?> block, JReturnStmt returnStmt) {
    Immediate op = returnStmt.getOp();
    if (op instanceof IntConstant) return ((IntConstant) op).getValue();
    String ref = returnStmt.toString();
    return searchRef(block, ref);
  }

  private int searchRef(BasicBlock<?> block, String ref) {
    for (int i = block.getStmts().size() - 1; i >= 0; i--) {
      Stmt stmt = block.getStmts().get(i);
      if (!(stmt instanceof JAssignStmt)) continue;
      Value op = ((JAssignStmt) stmt).getRightOp();
      if (op instanceof IntConstant) return ((IntConstant) op).getValue();
      ref = op.toString();
    }
    for (BasicBlock<?> predecessor : block.getPredecessors()) {
      return searchRef(predecessor, ref);
    }
    throw new IllegalStateException();
  }

  private boolean containsExceptionalBlock(BasicBlock<?> block, BasicBlock<?> query) {
    if (block.getExceptionalSuccessors().isEmpty()) {
      if (block.getPredecessors().size() > 1) return false;
      block = block.getPredecessors().get(0);
    }
    boolean anyMatch = false;
    for (BasicBlock<?> es : block.getExceptionalSuccessors().values()) {
      anyMatch = anyMatch || containsBlock(es, query);
    }
    return anyMatch;
  }

  private boolean containsBlock(BasicBlock<?> block, BasicBlock<?> query) {
    if (block == query) return true;
    boolean anyMatch = false;
    for (int i = 0; i < block.getSuccessors().size() && !anyMatch; i++) {
      anyMatch = containsBlock(block.getSuccessors().get(i), query);
    }
    return anyMatch;
  }
}
