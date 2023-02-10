package sootup.java.bytecode.interceptors;

import categories.Java8Test;
import java.util.*;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Trap;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.Position;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
import sootup.core.util.ImmutableUtils;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;

/** @author Zun Wang */
@Category(Java8Test.class)
@Ignore("FIXME: needs .setTraps() adapted to MutableBlockStmtGraph")
public class TrapTightenerTest {
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

  JavaClassType intType = factory.getClassType("int");
  JavaClassType classType = factory.getClassType("Test");
  MethodSignature methodSignature =
      new MethodSignature(classType, "test", Collections.emptyList(), VoidType.getInstance());
  IdentityRef identityRef = JavaJimple.newThisRef(classType);

  // build locals
  Local l0 = JavaJimple.newLocal("l0", intType);
  Local l1 = JavaJimple.newLocal("l1", intType);
  Local l2 = JavaJimple.newLocal("l2", intType);
  Local l3 = JavaJimple.newLocal("l3", intType);

  ClassType exception = factory.getClassType("java.lang.Throwable");
  JavaJimple javaJimple = JavaJimple.getInstance();
  IdentityRef caughtExceptionRef = javaJimple.newCaughtExceptionRef();
  Stmt startingStmt = JavaJimple.newIdentityStmt(l0, identityRef, noStmtPositionInfo);
  Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

  // stmts
  Stmt stmt1 = JavaJimple.newAssignStmt(l1, IntConstant.getInstance(1), noStmtPositionInfo);
  Stmt stmt2 = JavaJimple.newEnterMonitorStmt(l1, noStmtPositionInfo);
  Stmt stmt3 = JavaJimple.newAssignStmt(l2, l1, noStmtPositionInfo);
  Stmt stmt4 = JavaJimple.newExitMonitorStmt(l2, noStmtPositionInfo);
  Stmt stmt5 = JavaJimple.newGotoStmt(noStmtPositionInfo);

  Stmt stmt6 = JavaJimple.newIdentityStmt(l3, caughtExceptionRef, noStmtPositionInfo);
  Stmt stmt7 = JavaJimple.newAssignStmt(l2, IntConstant.getInstance(2), noStmtPositionInfo);
  Stmt stmt8 = JavaJimple.newExitMonitorStmt(l2, noStmtPositionInfo);
  Stmt stmt9 = JavaJimple.newThrowStmt(l3, noStmtPositionInfo);
  Stmt stmt10 = JavaJimple.newAssignStmt(l2, IntConstant.getInstance(3), noStmtPositionInfo);
  Stmt stmt11 = JavaJimple.newAssignStmt(l2, IntConstant.getInstance(4), noStmtPositionInfo);
  // trap
  Trap trap1 = new Trap(exception, stmt2, stmt5, stmt6);
  Trap trap2 = new Trap(exception, stmt1, stmt5, stmt6);
  Trap trap3 = new Trap(exception, stmt7, stmt10, stmt6);

  /**
   *
   *
   * <pre>
   *    l0 := @this Test;
   *  label1:
   *    l1 = 1;
   *    l2 = 2;
   *    l2 = 3;
   *  label2:
   *    goto label4;
   *  label3:
   *    l3 := @caughtexception;
   *    l2 = 4;
   *    throw l3;
   *  label4:
   *    return;
   *  catch Exception from label1 to label2 with label3;
   * </pre>
   *
   * after run trapTightener
   *
   * <pre>
   *    l0 := @this Test;
   *    l1 = 1;
   *  label1:
   *    l2 = 2;
   *  label2:
   *    l2 = 3;
   *    goto label4;
   *  label3:
   *    l3 := @caughtexception;
   *    l2 = 4;
   *    throw l3;
   *  label4:
   *    return;
   *  catch Exception from label1 to label2 with label3;
   * </pre>
   */
  @Test
  public void testSimpleBody() {

    Body body = createSimpleBody();
    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());

    // modify exceptionalStmtGraph
    builder.clearExceptionEdgesOf(stmt1);
    builder.clearExceptionEdgesOf(stmt10);

    TrapTightener trapTightener = new TrapTightener();
    trapTightener.interceptBody(builder, null);

    List<Trap> excepted = new ArrayList<>();
    excepted.add(trap3);
    List<Trap> actual = builder.getStmtGraph().getTraps();
    AssertUtils.assertTrapsEquiv(excepted, actual);
  }
  /**
   *
   *
   * <pre>
   *    l0 := @this Test;
   *    l1 = 1;
   *  label1:
   *    entermonitor l1;
   *    l2 = l1;
   *    exitmonitor l2;
   *  label2:
   *    goto label4;
   *  label3:
   *    l3 := @caughtexception;
   *    l2 = 2;
   *    exitmonitor l2;
   *    throw l3;
   *  label4:
   *    return;
   *  catch Exception from label1 to label2 with label3;
   * </pre>
   */
  @Test
  public void testMonitoredBody() {

    Body.BodyBuilder builder = Body.builder(creatBodyWithMonitor(), Collections.emptySet());

    // modify exceptionalStmtGraph
    builder.clearExceptionEdgesOf(stmt2);
    builder.clearExceptionEdgesOf(stmt4);
    //  builder.addFlow(, stmt6);

    TrapTightener trapTightener = new TrapTightener();
    trapTightener.interceptBody(builder, null);

    List<Trap> excepted = new ArrayList<>();
    excepted.add(trap1);
    List<Trap> actual = builder.getStmtGraph().getTraps();
    AssertUtils.assertTrapsEquiv(excepted, actual);
  }

  private Body createSimpleBody() {
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    Body.BodyBuilder builder = Body.builder(graph);
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, l3);
    builder.setLocals(locals);

    // set graph
    graph.addBlock(Arrays.asList(stmt1, stmt7, stmt10), Collections.singletonMap(exception, stmt6));
    graph.putEdge(startingStmt, stmt1);
    graph.putEdge(stmt10, stmt5);
    graph.putEdge(stmt6, stmt11);
    graph.putEdge(stmt11, stmt9);
    graph.putEdge(stmt5, ret);

    // build startingStmt
    builder.setStartingStmt(startingStmt);

    return builder.build();
  }

  private Body creatBodyWithMonitor() {
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    Body.BodyBuilder builder = Body.builder(graph);
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, l3);
    builder.setLocals(locals);

    // set graph
    graph.addBlock(Arrays.asList(stmt6, stmt7, stmt8, stmt9), Collections.emptyMap());
    graph.addBlock(Arrays.asList(startingStmt, stmt1), Collections.emptyMap());
    graph.addBlock(Arrays.asList(stmt2, stmt3, stmt4), Collections.singletonMap(exception, stmt6));
    graph.putEdge(stmt1, stmt2);
    graph.putEdge(stmt4, stmt5);
    graph.putEdge(stmt5, ret);

    // build startingStmt
    builder.setStartingStmt(startingStmt);

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    return builder.build();
  }
}
