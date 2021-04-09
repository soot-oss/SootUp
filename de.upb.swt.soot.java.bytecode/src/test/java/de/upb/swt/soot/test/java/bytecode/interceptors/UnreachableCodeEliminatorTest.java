package de.upb.swt.soot.test.java.bytecode.interceptors;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.ref.IdentityRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.VoidType;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.bytecode.interceptors.UnreachableCodeEliminator;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Zun Wang */
@Category(Java8Test.class)
public class UnreachableCodeEliminatorTest {

  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  JavaJimple javaJimple = JavaJimple.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();
  JavaClassType classType = factory.getClassType("Test");
  MethodSignature methodSignature =
      new MethodSignature(classType, "test", Collections.emptyList(), VoidType.getInstance());
  JavaClassType refType = factory.getClassType("ref");
  ClassType exception = factory.getClassType("RuntimeException");

  IdentityRef identityRef = JavaJimple.newThisRef(classType);

  // build locals
  Local l0 = JavaJimple.newLocal("l0", refType);
  Local l1 = JavaJimple.newLocal("l1", PrimitiveType.getInt());
  Local l2 = JavaJimple.newLocal("l2", PrimitiveType.getInt());
  Local l3 = JavaJimple.newLocal("l3", PrimitiveType.getInt());
  Local l4 = JavaJimple.newLocal("l3", PrimitiveType.getInt());
  Local stack0 = JavaJimple.newLocal("stack0", refType);
  IdentityRef idRef = javaJimple.newCaughtExceptionRef();

  // build stmts
  Stmt startingStmt = JavaJimple.newIdentityStmt(l0, identityRef, noStmtPositionInfo);
  Stmt stmt1 = JavaJimple.newAssignStmt(l1, IntConstant.getInstance(1), noStmtPositionInfo);
  Stmt stmt2 = JavaJimple.newAssignStmt(l2, IntConstant.getInstance(2), noStmtPositionInfo);
  Stmt stmt3 = JavaJimple.newAssignStmt(l3, IntConstant.getInstance(3), noStmtPositionInfo);

  Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

  Stmt handlerStmt = JavaJimple.newIdentityStmt(stack0, idRef, noStmtPositionInfo);
  Stmt beginStmt = JavaJimple.newAssignStmt(l3, stack0, noStmtPositionInfo);
  Stmt endStmt = JavaJimple.newAssignStmt(l4, IntConstant.getInstance(4), noStmtPositionInfo);

  Trap trap1 = JavaJimple.newTrap(exception, beginStmt, endStmt, handlerStmt);
  Trap trap2 = JavaJimple.newTrap(exception, beginStmt, beginStmt, handlerStmt);

  @Test
  /**
   * Test the simpleBody l0:= @this Test -> l1 = 1 -> return l2 = 2 -> l3 = 3 -> return l2 = 2 and
   * l3 = 3 are unreachable
   */
  public void testSimpleBody() {

    // build an instance of BodyBuilder
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, l3);

    builder.setLocals(locals);

    // build stmtsGraph for the builder
    builder.addFlow(startingStmt, stmt1);

    builder.addFlow(stmt1, ret);
    builder.addFlow(stmt2, stmt3);
    builder.addFlow(stmt3, ret);

    // set startingStmt
    builder.setStartingStmt(startingStmt);

    // set Position
    builder.setPosition(NoPositionInformation.getInstance());

    UnreachableCodeEliminator eliminator = new UnreachableCodeEliminator();
    eliminator.interceptBody(builder);

    Set<Stmt> expectedStmtsSet = ImmutableUtils.immutableSet(startingStmt, stmt1, ret);
    AssertUtils.assertStmtsSetEquiv(expectedStmtsSet, builder.getExceptionalGraph().nodes());
  }

  @Test

  /**
   * Test the Body with unreachable trap l0:= @this Test -> l1 = 1 -> return
   *
   * <p>trap: stack0 := @caughtexception l3 = stack0 l4 = 4
   */
  public void testTrapedBody1() {

    // build an instance of BodyBuilder
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l3, l4, stack0);

    builder.setLocals(locals);

    // build stmtsGraph for the builder
    builder.addFlow(startingStmt, stmt1);
    builder.addFlow(stmt1, ret);
    builder.addFlow(beginStmt, endStmt);

    List<Trap> traps = new ArrayList<>();
    traps.add(trap1);
    builder.setTraps(traps);

    // set startingStmt
    builder.setStartingStmt(startingStmt);

    // set Position
    builder.setPosition(NoPositionInformation.getInstance());

    UnreachableCodeEliminator eliminator = new UnreachableCodeEliminator();
    eliminator.interceptBody(builder);

    assertEquals(0, builder.getTraps().size());

    Set<Stmt> expectedStmtsSet = ImmutableUtils.immutableSet(startingStmt, stmt1, ret);
    AssertUtils.assertStmtsSetEquiv(expectedStmtsSet, builder.getExceptionalGraph().nodes());
  }

  @Test

  /**
   * Test the Body with unreachable trap l0:= @this Test -> l1 = 1 -> return
   *
   * <p>trap: stack0 := @caughtexception l3 = stack0
   */
  public void testTrapedBody2() {

    // build an instance of BodyBuilder
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l3, stack0);

    builder.setLocals(locals);

    // build stmtsGraph for the builder
    builder.addFlow(startingStmt, stmt1);
    builder.addFlow(stmt1, ret);
    builder.addFlow(handlerStmt, beginStmt);

    List<Trap> traps = new ArrayList<>();
    traps.add(trap2);
    builder.setTraps(traps);

    // set startingStmt
    builder.setStartingStmt(startingStmt);

    // set Position
    builder.setPosition(NoPositionInformation.getInstance());

    UnreachableCodeEliminator eliminator = new UnreachableCodeEliminator();
    eliminator.interceptBody(builder);

    assertEquals(0, builder.getTraps().size());

    Set<Stmt> expectedStmtsSet = ImmutableUtils.immutableSet(startingStmt, stmt1, ret);
    AssertUtils.assertStmtsSetEquiv(expectedStmtsSet, builder.getExceptionalGraph().nodes());
  }
}
