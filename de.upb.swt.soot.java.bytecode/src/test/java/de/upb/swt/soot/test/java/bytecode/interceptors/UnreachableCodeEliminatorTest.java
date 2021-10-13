package de.upb.swt.soot.test.java.bytecode.interceptors;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.ref.IdentityRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.Position;
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
  Local l3 = JavaJimple.newLocal("l3", exception);
  Local stack0 = JavaJimple.newLocal("stack0", refType);
  IdentityRef idRef = javaJimple.newCaughtExceptionRef();

  // build stmts
  Stmt startingStmt = JavaJimple.newIdentityStmt(l0, identityRef, noStmtPositionInfo);
  Stmt stmt1 = JavaJimple.newAssignStmt(l1, IntConstant.getInstance(1), noStmtPositionInfo);
  Stmt stmt2 = JavaJimple.newAssignStmt(l2, IntConstant.getInstance(2), noStmtPositionInfo);

  Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

  Stmt handlerStmt = JavaJimple.newIdentityStmt(stack0, idRef, noStmtPositionInfo);
  Stmt throwStmt = JavaJimple.newThrowStmt(l3, noStmtPositionInfo);

  Trap trap1 = JavaJimple.newTrap(exception, stmt1, stmt2, handlerStmt);

  @Test
  /**
   * Test the simpleBody l0:= @this Test -> l1 = 1 -> l2 = 2 -> return remove l2 = 2 then return
   * should be unreachable
   */
  public void testSimpleBody() {

    Body body = createSimpleBody();
    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());
    builder.removeStmt(stmt2);

    UnreachableCodeEliminator eliminator = new UnreachableCodeEliminator();
    eliminator.interceptBody(builder);

    Set<Stmt> expectedStmtsSet = ImmutableUtils.immutableSet(startingStmt, stmt1);
    AssertUtils.assertSetsEquiv(expectedStmtsSet, builder.getStmtGraph().nodes());
  }

  @Test
  /**
   * Test the Body with unreachable trap l0:= @this Test -> l1 = 1 -> l2 = 2 -> return trap: stack0
   * := @caughtexception; throw l4; trapped stmt: l1 = 1 remove l1 = 1 and add flow from l0:=@this
   * Test to l2 = 2 then the trap is empty, should be removed.
   */
  public void testTrapedBody() {

    Body body = createTrappedBody();
    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());

    builder.removeStmt(stmt1);
    builder.addFlow(startingStmt, stmt2);

    UnreachableCodeEliminator eliminator = new UnreachableCodeEliminator();
    eliminator.interceptBody(builder);

    assertEquals(0, builder.getTraps().size());

    Set<Stmt> expectedStmtsSet = ImmutableUtils.immutableSet(startingStmt, stmt2, ret);
    AssertUtils.assertSetsEquiv(expectedStmtsSet, builder.getStmtGraph().nodes());
  }

  private Body createSimpleBody() {

    Body.BodyBuilder builder = Body.builder();

    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2);
    builder.setLocals(locals);

    // build stmtsGraph for the builder
    builder.addFlow(startingStmt, stmt1);
    builder.addFlow(stmt1, stmt2);
    builder.addFlow(stmt2, ret);

    // set startingStmt
    builder.setStartingStmt(startingStmt);

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    return builder.build();
  }

  private Body createTrappedBody() {

    Body.BodyBuilder builder = Body.builder();

    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, l3, stack0);
    builder.setLocals(locals);

    // build stmtsGraph for the builder
    builder.addFlow(startingStmt, stmt1);
    builder.addFlow(stmt1, stmt2);
    builder.addFlow(stmt2, ret);
    builder.addFlow(handlerStmt, throwStmt);

    List<Trap> traps = new ArrayList<>();
    traps.add(trap1);
    builder.setTraps(traps);

    // set startingStmt
    builder.setStartingStmt(startingStmt);

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    return builder.build();
  }
}
