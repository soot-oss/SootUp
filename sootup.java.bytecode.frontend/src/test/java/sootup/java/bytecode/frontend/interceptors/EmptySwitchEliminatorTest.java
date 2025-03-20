package sootup.java.bytecode.frontend.interceptors;

import java.util.*;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.VoidType;
import sootup.core.util.ImmutableUtils;
import sootup.interceptors.EmptySwitchEliminator;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

/**
 * @author Zun Wang
 */
public class EmptySwitchEliminatorTest {

  // Preparation
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();
  JavaClassType intType = factory.getClassType("int");
  JavaClassType classType = factory.getClassType("Test");
  MethodSignature methodSignature =
      new MethodSignature(classType, "test", Collections.emptyList(), VoidType.getInstance());
  IdentityRef identityRef = JavaJimple.newThisRef(classType);

  // build locals
  Local l0 = JavaJimple.newLocal("l0", intType);
  Local l1 = JavaJimple.newLocal("l1", intType);
  Local l2 = JavaJimple.newLocal("l2", intType);

  // build Stmts
  // l0 := @this Test
  FallsThroughStmt startingStmt = JavaJimple.newIdentityStmt(l0, identityRef, noStmtPositionInfo);
  // l1 = 3
  FallsThroughStmt stmt1 =
      JavaJimple.newAssignStmt(l1, IntConstant.getInstance(3), noStmtPositionInfo);
  // l2 = 0
  FallsThroughStmt defaultStmt =
      JavaJimple.newAssignStmt(l2, IntConstant.getInstance(0), noStmtPositionInfo);
  // return
  Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

  @Test
  public void testEmptySwitch() {

    Body body = createEmptySwitchBody();

    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());
    EmptySwitchEliminator eliminator = new EmptySwitchEliminator();
    eliminator.interceptBody(builder, new JavaView(Collections.emptyList()));

    Body expectedBody = createExpectedEmptySwitchBody();
    AssertUtils.assertStmtGraphEquiv(expectedBody, builder.build());
  }

  private Body createEmptySwitchBody() {
    // build an empty instance of SwitchStmt
    List<IntConstant> values = new ArrayList<>();
    BranchingStmt sw = JavaJimple.newLookupSwitchStmt(l1, values, noStmtPositionInfo);

    // build an instance of BodyBuilder
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2);

    builder.setLocals(locals);
    final MutableStmtGraph stmtGraph = builder.getStmtGraph();
    // build stmtsGraph for the builder
    stmtGraph.putEdge(startingStmt, stmt1);
    stmtGraph.putEdge(stmt1, sw);
    stmtGraph.putEdge(sw, 0, defaultStmt);
    stmtGraph.putEdge(defaultStmt, ret);

    // set startingStmt
    stmtGraph.setStartingStmt(startingStmt);

    // set Position
    builder.setPosition(NoPositionInformation.getInstance());

    return builder.build();
  }

  private Body createExpectedEmptySwitchBody() {
    // build a new instance of JGotoStmt
    BranchingStmt gotoStmt = JavaJimple.newGotoStmt(noStmtPositionInfo);

    // build an instance of BodyBuilder
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2);

    builder.setLocals(locals);

    // build stmtsGraph for the builder
    final MutableStmtGraph stmtGraph = builder.getStmtGraph();
    stmtGraph.putEdge(startingStmt, stmt1);
    stmtGraph.putEdge(stmt1, gotoStmt);
    stmtGraph.putEdge(gotoStmt, JGotoStmt.BRANCH_IDX, defaultStmt);
    stmtGraph.putEdge(defaultStmt, ret);

    // set startingStmt
    stmtGraph.setStartingStmt(startingStmt);

    // set Position
    builder.setPosition(NoPositionInformation.getInstance());

    return builder.build();
  }
}
