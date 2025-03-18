package sootup.java.bytecode.frontend.interceptors;

import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.Position;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.VoidType;
import sootup.core.util.ImmutableUtils;
import sootup.interceptors.LocalLivenessAnalyser;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;

/**
 * @author Zun Wang
 */
public class LocalLivenessAnalyserTest {

  // Preparation
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();

  JavaClassType intType = factory.getClassType("int");
  JavaClassType classType = factory.getClassType("Test");
  MethodSignature methodSignature =
      new MethodSignature(classType, "test", Collections.emptyList(), VoidType.getInstance());

  // build locals
  Local a = JavaJimple.newLocal("a", intType);
  Local b = JavaJimple.newLocal("b", intType);
  Local c = JavaJimple.newLocal("c", intType);

  FallsThroughStmt aeq0 =
      JavaJimple.newAssignStmt(a, IntConstant.getInstance(0), noStmtPositionInfo);
  FallsThroughStmt beqaplus1 =
      JavaJimple.newAssignStmt(
          b, JavaJimple.newAddExpr(a, IntConstant.getInstance(0)), noStmtPositionInfo);
  FallsThroughStmt ceqcplusb =
      JavaJimple.newAssignStmt(c, JavaJimple.newAddExpr(c, b), noStmtPositionInfo);
  FallsThroughStmt aeqbplus2 =
      JavaJimple.newAssignStmt(
          a, JavaJimple.newAddExpr(b, IntConstant.getInstance(2)), noStmtPositionInfo);
  BranchingStmt ifalt9 =
      JavaJimple.newIfStmt(JavaJimple.newGtExpr(IntConstant.getInstance(9), a), noStmtPositionInfo);
  Stmt ret = JavaJimple.newReturnStmt(c, noStmtPositionInfo);

  /**
   * a = 0;
   *
   * <p>label1: b = a + 0;
   *
   * <p>c = c + b;
   *
   * <p>a = b + 2;
   *
   * <p>if 9 > a goto label1;
   *
   * <p>return c;
   */
  @Test
  public void testLivenessAnalyser() {
    Body body = createBody();
    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());
    LocalLivenessAnalyser analyser = new LocalLivenessAnalyser(builder.getStmtGraph());
    Set<Local> cSet = ImmutableUtils.immutableSet(c);
    Set<Local> ac = ImmutableUtils.immutableSet(a, c);
    Set<Local> bc = ImmutableUtils.immutableSet(b, c);
    AssertUtils.assertSetsEquiv(cSet, analyser.getLiveLocalsBeforeStmt(aeq0));
    AssertUtils.assertSetsEquiv(ac, analyser.getLiveLocalsAfterStmt(aeq0));
    AssertUtils.assertSetsEquiv(ac, analyser.getLiveLocalsBeforeStmt(beqaplus1));
    AssertUtils.assertSetsEquiv(bc, analyser.getLiveLocalsAfterStmt(beqaplus1));
    AssertUtils.assertSetsEquiv(bc, analyser.getLiveLocalsBeforeStmt(ceqcplusb));
    AssertUtils.assertSetsEquiv(bc, analyser.getLiveLocalsAfterStmt(ceqcplusb));
    AssertUtils.assertSetsEquiv(bc, analyser.getLiveLocalsBeforeStmt(aeqbplus2));
    AssertUtils.assertSetsEquiv(ac, analyser.getLiveLocalsAfterStmt(aeqbplus2));
    AssertUtils.assertSetsEquiv(ac, analyser.getLiveLocalsBeforeStmt(ifalt9));
    AssertUtils.assertSetsEquiv(ac, analyser.getLiveLocalsAfterStmt(ifalt9));
    AssertUtils.assertSetsEquiv(cSet, analyser.getLiveLocalsBeforeStmt(ret));
    AssertUtils.assertSetsEquiv(Collections.emptySet(), analyser.getLiveLocalsAfterStmt(ret));
  }

  private Body createBody() {

    Body.BodyBuilder builder = Body.builder();
    final MutableStmtGraph stmtGraph = builder.getStmtGraph();
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(a, b, c);

    builder.setLocals(locals);

    // set graph
    stmtGraph.putEdge(aeq0, beqaplus1);
    stmtGraph.putEdge(beqaplus1, ceqcplusb);
    stmtGraph.putEdge(ceqcplusb, aeqbplus2);
    stmtGraph.putEdge(aeqbplus2, ifalt9);
    stmtGraph.putEdge(ifalt9, JIfStmt.FALSE_BRANCH_IDX, ret);
    stmtGraph.putEdge(ifalt9, JIfStmt.TRUE_BRANCH_IDX, beqaplus1);

    // set first stmt
    stmtGraph.setStartingStmt(aeq0);

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    return builder.build();
  }
}
