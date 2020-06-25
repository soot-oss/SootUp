package de.upb.swt.soot.test.java.bytecode.interceptors;

import static org.junit.Assert.*;

import categories.Java8Test;
import com.google.common.graph.*;
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.NoPositionInformation;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.VoidType;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.bytecode.interceptors.LocalSplitter;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Zun Wang */
@Category(Java8Test.class)
public class LocalSplitterTest {

  // Preparation
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  // JavaJimple javaJimple = JavaJimple.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

  JavaClassType intType = factory.getClassType("int");
  JavaClassType booleanType = factory.getClassType("boolean");
  JavaClassType classType = factory.getClassType("Test");
  MethodSignature methodSignature = new MethodSignature(classType, "test", Collections.emptyList(), VoidType.getInstance());

  // build locals
  Local l0 = JavaJimple.newLocal("l0", intType);
  Local l1 = JavaJimple.newLocal("l1", intType);
  Local l2 = JavaJimple.newLocal("l2", intType);
  Local l3 = JavaJimple.newLocal("l3", intType);
  Local l4 = JavaJimple.newLocal("l4", booleanType);
  Local l0hash1 = JavaJimple.newLocal("l0#1", intType);
  Local l0hash2 = JavaJimple.newLocal("l0#2", intType);
  Local l0hash3 = JavaJimple.newLocal("l0#3", intType);
  Local l1hash2 = JavaJimple.newLocal("l1#2", intType);
  Local l1hash4 = JavaJimple.newLocal("l1#4", intType);

  /**
   * int a = 0; int b = 0; a = a + 1; b = b + 1;
   *
   * <pre>
   * 1. l0 = 0
   * 2. l1 = 1
   * 3. l0 = l0 + 1
   * 4. l1 = l1 + 1
   * 5. return
   * </pre>
   *
   * to:
   *
   * <pre>
   * 1. l0#1 = 0
   * 2. l1#2 = 1
   * 3. l0#3 = l0#1 + 1
   * 4. l1#4 = l1#2 + 1
   * 5. return
   * </pre>
   */
  @Test
  public void testLocalSplitterForMultilocals() {

    Body body = createMultilocalsBody();
    //Fixme: entryPoint in ImmutableStmtGraph is not copied.
    //System.out.println(body.getStmtGraph().getEntryPoint());

    LocalSplitter localSplitter = new LocalSplitter();
    Body newBody = localSplitter.interceptBody(body);
    Body expectedBody = createExpectedMuiltilocalsBody();

    // check newBody's locals
    assertLocalsEquiv(expectedBody.getLocals(), newBody.getLocals());

    // check newBody's first stmt
    // Fixme: entryPoint
    //assertTrue(expectedBody.getFirstStmt().equivTo(newBody.getFirstStmt()));

    // check newBody's stmtGraph
    assertStmtGraphEquiv(expectedBody.getStmtGraph(), newBody.getStmtGraph());
  }



  /**
   * int a = 0; if(a<0) a = a + 1; else {a = a+ 1; a = a +1;} return a
   *
   * <pre>
   * 1. l0 = 0
   * 2. if l0 >= 0 goto l0 = l0 -1
   * 3. l0 = l0 + 1
   * 4. goto  [?= return l0#2]
   * 5. l0 = l0 - 1
   * 6. l0 = l0 + 2
   * 7. return l0
   * </pre>
   *
   * to:
   * alternation1
   * <pre>
   * 1. l0#1 = 0
   * 2. if l0#1 >= 0 goto l0#3 = l0#1 -1
   * 3. l0#2 = l0#1 + 1
   * 4. goto  [?= return l0]
   * 5. l0#3 = l0#1 - 1
   * 6. l0#2 = l0#3 + 2
   * 7. return l0#2
   * </pre>
   *
   */
  @Test
  public void testLocalSplitterForBinaryBranches() {

    Body body = createBBBody();
    LocalSplitter localSplitter = new LocalSplitter();
    Body newBody = localSplitter.interceptBody(body);
    Body expectedBody = createExpectedBBBody();

     // check newBody's locals
    assertLocalsEquiv(expectedBody.getLocals(), newBody.getLocals());

     // check newBody's first stmt
    //Fixme: entryPoint
    //assertTrue(expectedBody.getFirstStmt().equivTo(newBody.getFirstStmt()));

     // check newBody's stmtGraph
    assertStmtGraphEquiv(expectedBody.getStmtGraph(), newBody.getStmtGraph());
  }

  /**
   * for(int i = 0; i < 10; i++){ i = i + 1 } transform:
   *
   * <pre>
   * 1. l0 = 0
   * 2. l4 = l0 < 10
   * 3. if l4 == 0 goto return
   * 4. l1 = l0 + 1
   * 5. l0 = l1
   * 6. l2 = l0
   * 7. l3 = l0 + 1
   * 8. l0 = l3
   * 9. goto [?= l4 = l0 < 10]
   * 10. return
   * </pre>
   *
   * to:
   *
   * <pre>
   * 1. l0#1 = 0
   * 2. l4 = l0#1 < 10
   * 3. if l4 == 0 goto return
   * 4. l1 = l0#1 + 1
   * 5. l0#2 = l1
   * 6. l2 = l0#2
   * 7. l3 = l0#2 + 1
   * 8. l0#1 = l3
   * 9. goto [?= l4 = l0#1 < 10]
   * 10. return
   * </pre>
   */

  @Test
  public void testLocalSplitterForLoop() {

    Body body = createLoopBody();
    LocalSplitter localSplitter = new LocalSplitter();
    Body newBody = localSplitter.interceptBody(body);
    Body expectedBody = createExpectedLoopBody();

    //fixme: bodyPinter print false order
    //System.out.println(newBody);

    // check newBody's locals
    assertLocalsEquiv(expectedBody.getLocals(), newBody.getLocals());

    //fixme: entryPoint
    // check newBody's first stmt
    //assertTrue(expectedBody.getFirstStmt().equivTo(newBody.getFirstStmt()));

    // check newBody's stmtGraph
    assertStmtGraphEquiv(expectedBody.getStmtGraph(), newBody.getStmtGraph());
  }


  /** bodycreater for BinaryBranches */
  private Body createBBBody() {
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0);
    builder.setLocals(locals);

    // build traps (an empty list)
    List<Trap> traps = Collections.emptyList();
    builder.setTraps(traps);

    Stmt stmt1 = JavaJimple.newAssignStmt(l0, IntConstant.getInstance(0), noStmtPositionInfo);
    Stmt stmt2 =
            JavaJimple.newIfStmt(
                    JavaJimple.newGeExpr(l0, IntConstant.getInstance(0)),
                    noStmtPositionInfo);
    Stmt stmt3 =
            JavaJimple.newAssignStmt(
                    l0, JavaJimple.newAddExpr(l0, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt4 = JavaJimple.newGotoStmt(noStmtPositionInfo);
    Stmt stmt5 =
            JavaJimple.newAssignStmt(
                    l0, JavaJimple.newSubExpr(l0, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt6 =
            JavaJimple.newAssignStmt(
                    l0, JavaJimple.newAddExpr(l0, IntConstant.getInstance(2)), noStmtPositionInfo);
    Stmt ret = JavaJimple.newReturnStmt(l0, noStmtPositionInfo);

    //set graph-nodes
    builder.addStmt(stmt1);
    builder.addStmt(stmt2);
    builder.addStmt(stmt3);
    builder.addStmt(stmt4);
    builder.addStmt(stmt5);
    builder.addStmt(stmt6);
    builder.addStmt(ret);

    // set graph-edges
    builder.addFlow(stmt1, stmt2);
    builder.addFlow(stmt2, stmt3);
    builder.addFlow(stmt3, stmt4);
    builder.addFlow(stmt4, ret);
    builder.addFlow(stmt2, stmt5);
    builder.addFlow(stmt5, stmt6);
    builder.addFlow(stmt6, ret);

    // build startingStmt
    builder.setFirstStmt(stmt1);

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    Body body = builder.build();
    return body;
  }

  private Body createExpectedBBBody() {

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l0hash1, l0hash2, l0hash3);
    builder.setLocals(locals);

    // build traps (an empty list)
    List<Trap> traps = Collections.emptyList();
    builder.setTraps(traps);

    Stmt stmt1 = JavaJimple.newAssignStmt(l0hash1, IntConstant.getInstance(0), noStmtPositionInfo);
    Stmt stmt2 =
            JavaJimple.newIfStmt(
                    JavaJimple.newGeExpr(l0hash1, IntConstant.getInstance(0)),
                    noStmtPositionInfo);
    Stmt stmt3 =
            JavaJimple.newAssignStmt(
                    l0hash2, JavaJimple.newAddExpr(l0hash1, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt4 = JavaJimple.newGotoStmt(noStmtPositionInfo);
    Stmt stmt5 =
            JavaJimple.newAssignStmt(
                    l0hash3, JavaJimple.newSubExpr(l0hash1, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt6 =
            JavaJimple.newAssignStmt(
                    l0hash2, JavaJimple.newAddExpr(l0hash3, IntConstant.getInstance(2)), noStmtPositionInfo);
    Stmt ret = JavaJimple.newReturnStmt(l0hash2, noStmtPositionInfo);

    //set graph-nodes
    builder.addStmt(stmt1);
    builder.addStmt(stmt2);
    builder.addStmt(stmt3);
    builder.addStmt(stmt4);
    builder.addStmt(stmt5);
    builder.addStmt(stmt6);
    builder.addStmt(ret);

    // set graph-edges
    builder.addFlow(stmt1, stmt2);
    builder.addFlow(stmt2, stmt3);
    builder.addFlow(stmt3, stmt4);
    builder.addFlow(stmt4, ret);
    builder.addFlow(stmt2, stmt5);
    builder.addFlow(stmt5, stmt6);
    builder.addFlow(stmt6, ret);

    // build startingStmt
    builder.setFirstStmt(stmt1);

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    Body body = builder.build();
    return body;
  }

  /** bodycreater for Loop */
  private Body createLoopBody() {

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, l3, l4);
    builder.setLocals(locals);

    // build traps (an empty list)
    List<Trap> traps = Collections.emptyList();
    builder.setTraps(traps);

    Stmt stmt1 = JavaJimple.newAssignStmt(l0, IntConstant.getInstance(0), noStmtPositionInfo);
    Stmt stmt2 =
        JavaJimple.newAssignStmt(
            l4, JavaJimple.newLtExpr(l0, IntConstant.getInstance(10)), noStmtPositionInfo);
    Stmt stmt3 =
        JavaJimple.newIfStmt(
            JavaJimple.newEqExpr(l4, IntConstant.getInstance(0)),
            noStmtPositionInfo); // goto stmt10_loop
    Stmt stmt4 =
        JavaJimple.newAssignStmt(
            l1, JavaJimple.newAddExpr(l0, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt5 = JavaJimple.newAssignStmt(l0, l1, noStmtPositionInfo);
    Stmt stmt6 = JavaJimple.newAssignStmt(l2, l0, noStmtPositionInfo);
    Stmt stmt7 =
        JavaJimple.newAssignStmt(
            l3, JavaJimple.newAddExpr(l0, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt8 = JavaJimple.newAssignStmt(l0, l3, noStmtPositionInfo);
    Stmt stmt9 = JavaJimple.newGotoStmt(noStmtPositionInfo); // goto stmt2
    Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

    //set graph-nodes
    builder.addStmt(stmt1);
    builder.addStmt(stmt2);
    builder.addStmt(stmt3);
    builder.addStmt(stmt4);
    builder.addStmt(stmt5);
    builder.addStmt(stmt6);
    builder.addStmt(stmt7);
    builder.addStmt(stmt8);
    builder.addStmt(stmt9);
    builder.addStmt(ret);

    // set graph-edges
    builder.addFlow(stmt1, stmt2);
    builder.addFlow(stmt2, stmt3);
    builder.addFlow(stmt3, stmt4);
    builder.addFlow(stmt3, ret);
    builder.addFlow(stmt4, stmt5);
    builder.addFlow(stmt5, stmt6);
    builder.addFlow(stmt6, stmt7);
    builder.addFlow(stmt7, stmt8);
    builder.addFlow(stmt8, stmt9);
    builder.addFlow(stmt9, stmt2);

    // build startingStmt
    builder.setFirstStmt(stmt1);

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    Body body = builder.build();
    return body;
  }

  private Body createExpectedLoopBody() {

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, l3, l4, l0hash1, l0hash2);
    builder.setLocals(locals);

    // build traps (an empty list)
    List<Trap> traps = Collections.emptyList();
    builder.setTraps(traps);

    // build Stmts
    Stmt stmt1 = JavaJimple.newAssignStmt(l0hash1, IntConstant.getInstance(0), noStmtPositionInfo);
    Stmt stmt2 =
        JavaJimple.newAssignStmt(
            l4, JavaJimple.newLtExpr(l0hash1, IntConstant.getInstance(10)), noStmtPositionInfo);
    Stmt stmt3 =
        JavaJimple.newIfStmt(
            JavaJimple.newEqExpr(l4, IntConstant.getInstance(0)),
            noStmtPositionInfo); // goto stmt4 and ret
    Stmt stmt4 =
        JavaJimple.newAssignStmt(
            l1, JavaJimple.newAddExpr(l0hash1, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt5 = JavaJimple.newAssignStmt(l0hash2, l1, noStmtPositionInfo);
    Stmt stmt6 = JavaJimple.newAssignStmt(l2, l0hash2, noStmtPositionInfo);
    Stmt stmt7 =
        JavaJimple.newAssignStmt(
            l3, JavaJimple.newAddExpr(l0hash2, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt8 = JavaJimple.newAssignStmt(l0hash1, l3, noStmtPositionInfo);
    Stmt stmt9 = JavaJimple.newGotoStmt(noStmtPositionInfo); // goto stmt2
    Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

    //set graph-nodes
    builder.addStmt(stmt1);
    builder.addStmt(stmt2);
    builder.addStmt(stmt3);
    builder.addStmt(stmt4);
    builder.addStmt(stmt5);
    builder.addStmt(stmt6);
    builder.addStmt(stmt7);
    builder.addStmt(stmt8);
    builder.addStmt(stmt9);
    builder.addStmt(ret);

    // set graph-edges
    builder.addFlow(stmt1, stmt2);
    builder.addFlow(stmt2, stmt3);
    builder.addFlow(stmt3, stmt4);
    builder.addFlow(stmt3, ret);
    builder.addFlow(stmt4, stmt5);
    builder.addFlow(stmt5, stmt6);
    builder.addFlow(stmt6, stmt7);
    builder.addFlow(stmt7, stmt8);
    builder.addFlow(stmt8, stmt9);
    builder.addFlow(stmt9, stmt2);

    // build startingStmt
    builder.setFirstStmt(stmt1);

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    Body body = builder.build();
    return body;
  }

  /** bodycreater for multilocals */
  private Body createMultilocalsBody() {

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1);
    builder.setLocals(locals);

    // build traps (an empty list)
    List<Trap> traps = Collections.emptyList();
    builder.setTraps(traps);

    Stmt stmt1 = JavaJimple.newAssignStmt(l0, IntConstant.getInstance(0), noStmtPositionInfo);
    Stmt stmt2 = JavaJimple.newAssignStmt(l1, IntConstant.getInstance(1), noStmtPositionInfo);
    Stmt stmt3 =
            JavaJimple.newAssignStmt(
                    l0, JavaJimple.newAddExpr(l0, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt4 =
            JavaJimple.newAssignStmt(
                    l1, JavaJimple.newAddExpr(l1, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

    //set graph-nodes
    builder.addStmt(stmt1);
    builder.addStmt(stmt2);
    builder.addStmt(stmt3);
    builder.addStmt(stmt4);
    builder.addStmt(ret);

    // set graph-edges
    builder.addFlow(stmt1, stmt2);
    builder.addFlow(stmt2, stmt3);
    builder.addFlow(stmt3, stmt4);
    builder.addFlow(stmt4, ret);

    //set first stmt
    builder.setFirstStmt(stmt1);

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    Body body = builder.build();
    return body;
  }

  private Body createExpectedMuiltilocalsBody() {

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l0hash1, l1hash2, l0hash3, l1hash4);
    builder.setLocals(locals);

    // build traps (an empty list)
    List<Trap> traps = Collections.emptyList();

    Stmt stmt1 = JavaJimple.newAssignStmt(l0hash1, IntConstant.getInstance(0), noStmtPositionInfo);
    Stmt stmt2 = JavaJimple.newAssignStmt(l1hash2, IntConstant.getInstance(1), noStmtPositionInfo);
    Stmt stmt3 =
            JavaJimple.newAssignStmt(
                    l0hash3,
                    JavaJimple.newAddExpr(l0hash1, IntConstant.getInstance(1)),
                    noStmtPositionInfo);
    Stmt stmt4 =
            JavaJimple.newAssignStmt(
                    l1hash4,
                    JavaJimple.newAddExpr(l1hash2, IntConstant.getInstance(1)),
                    noStmtPositionInfo);
    Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

    //set graph-nodes
    builder.addStmt(stmt1);
    builder.addStmt(stmt2);
    builder.addStmt(stmt3);
    builder.addStmt(stmt4);
    builder.addStmt(ret);

    // set graph-edges
    builder.addFlow(stmt1, stmt2);
    builder.addFlow(stmt2, stmt3);
    builder.addFlow(stmt3, stmt4);
    builder.addFlow(stmt4, ret);

    //set first stmt
    builder.setFirstStmt(stmt1);;

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    Body body = builder.build();
    return body;
  }

  private static void assertLocalsEquiv(Set<Local> expected, Set<Local> actual) {
    assertNotNull(expected);
    assertNotNull(actual);
    assertEquals(expected.size(), actual.size());
    boolean isEqual = true;
    for (Local local : actual) {
      if (!expected.contains(local)) {
        isEqual = false;
        break;
      }
    }
    assertTrue(isEqual);
  }

  private void assertStmtGraphEquiv(StmtGraph expected, StmtGraph actual) {
    assertNotNull(expected);
    assertNotNull(actual);

    Set<Stmt> actualStmts = actual.nodes();
    Set<Stmt> expectedStmts = expected.nodes();
    assertEquals(expectedStmts.size(), actualStmts.size());

    boolean isEqual = true;
    boolean isInclusiv = false;
    for (Stmt stmt : actualStmts) {
      for (Stmt expectedStmt : expectedStmts) {
        if (stmt.equivTo(expectedStmt)) {
          List<Stmt> actualPreds = actual.predecessors(stmt);
          List<Stmt> expectedPreds = expected.predecessors(expectedStmt);
          List<Stmt> actualSuccs = actual.successors(stmt);
          List<Stmt> expectedSuccs = expected.successors(expectedStmt);
          if(areEqualLists(actualPreds,expectedPreds) && areEqualLists(actualSuccs, expectedSuccs)){
            isInclusiv = true;
          }
        }
      }
      if (!isInclusiv) {
        isEqual = false;
        break;
      }
      isInclusiv = false;
    }
    assertTrue(isEqual);
  }

  private boolean areEqualLists(List<Stmt> list1, List<Stmt> list2){
    boolean isEqual = true;
    boolean isInclusive = false;
    if(list1.size() != list2.size()){
      isEqual = false;
    }else{
      for(Stmt stmt1 : list1){
        for(Stmt stmt2 : list2){
          if(stmt1.equivTo(stmt2)){
            isInclusive = true;
          }
        }
        if(!isInclusive){
          isEqual = false;
          break;
        }
        isInclusive = false;
      }
    }
    return isEqual;
  }

  private void printGraph(Body body){
    System.out.println("Body: ");
    for(Stmt node: body.getStmtGraph().nodes()){
      System.out.println("predecessor: " + body.getStmtGraph().predecessors(node));
      System.out.println(node);
      System.out.println("successor: " + body.getStmtGraph().successors(node));
      System.out.println("_______________________________________________________");
    }
  }

}
