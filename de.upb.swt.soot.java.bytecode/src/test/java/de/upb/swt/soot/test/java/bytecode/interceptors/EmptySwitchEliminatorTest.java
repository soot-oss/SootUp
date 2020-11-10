package de.upb.swt.soot.test.java.bytecode.interceptors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import com.google.common.collect.Lists;
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.NoPositionInformation;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.ref.IdentityRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.VoidType;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.bytecode.interceptors.EmptySwitchEliminator;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Zun Wang */
@Category(Java8Test.class)
public class EmptySwitchEliminatorTest {

  // Preparation
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

  // build Stmts
  // l0 := @this Test
  Stmt startingStmt = JavaJimple.newIdentityStmt(l0, identityRef, noStmtPositionInfo);
  // l1 = 3
  Stmt stmt1 = JavaJimple.newAssignStmt(l1, IntConstant.getInstance(3), noStmtPositionInfo);
  // l2 = 0
  Stmt defaultStmt = JavaJimple.newAssignStmt(l2, IntConstant.getInstance(0), noStmtPositionInfo);
  // return
  Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

  @Test
  public void testEmptySwitch() {

    Body body = createEmptySwitchBody();
    List<Modifier> modifiers = new ArrayList<>();
    Body.BodyBuilder builder = Body.builder(body, modifiers);
    EmptySwitchEliminator eliminator = new EmptySwitchEliminator();
    eliminator.interceptBody(builder);

    Body expectedBody = createExpectedEmptySwitchBody();
    assertStmtGraphEquiv(expectedBody.getStmtGraph(), builder.getStmtGraph());
  }

  private Body createEmptySwitchBody() {
    // build an empty instance of SwitchStmt
    List<IntConstant> values = new ArrayList<>();
    Stmt sw = JavaJimple.newLookupSwitchStmt(l1, values, noStmtPositionInfo);

    // build an instance of BodyBuilder
    List<Modifier> modifiers = new ArrayList<>();
    Body.BodyBuilder builder = Body.builder(modifiers);
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2);
    builder.setLocals(locals);

    // build stmtsGraph for the builder
    builder.addFlow(startingStmt, stmt1);
    builder.addFlow(stmt1, sw);
    builder.addFlow(sw, defaultStmt);
    builder.addFlow(defaultStmt, ret);

    // set startingStmt
    builder.setStartingStmt(startingStmt);

    // set Position
    builder.setPosition(NoPositionInformation.getInstance());

    return builder.build();
  }

  private Body createExpectedEmptySwitchBody() {
    // build a new instance of JGotoStmt
    Stmt gotoStmt = JavaJimple.newGotoStmt(noStmtPositionInfo);

    // build an instance of BodyBuilder
    List<Modifier> modifiers = new ArrayList<>();
    Body.BodyBuilder builder = Body.builder(modifiers);
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2);
    builder.setLocals(locals);

    // build stmtsGraph for the builder
    builder.addFlow(startingStmt, stmt1);
    builder.addFlow(stmt1, gotoStmt);
    builder.addFlow(gotoStmt, defaultStmt);
    builder.addFlow(defaultStmt, ret);

    // set startingStmt
    builder.setStartingStmt(startingStmt);

    // set Position
    builder.setPosition(NoPositionInformation.getInstance());

    return builder.build();
  }

  private void assertStmtGraphEquiv(StmtGraph expected, StmtGraph actual) {
    assertNotNull(expected);
    assertNotNull(actual);
    final boolean condition = expected.equivTo(actual);
    if (!condition) {
      System.out.println("expected:");
      System.out.println(Lists.newArrayList(expected.iterator()));
      System.out.println("actual:");
      System.out.println(Lists.newArrayList(actual.iterator()) + "\n");

      for (Stmt s : expected) {
        System.out.println(s + " => " + expected.successors(s));
      }
      System.out.println();
      for (Stmt s : actual) {
        System.out.println(s + " => " + actual.successors(s));
      }
    }
    assertTrue(condition);
  }
}
