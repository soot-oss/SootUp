package de.upb.swt.soot.test.java.bytecode.interceptors;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.NoPositionInformation;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.common.constant.DoubleConstant;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.ref.IdentityRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.VoidType;
import de.upb.swt.soot.core.util.ImmutableUtils;
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
public class LocalPackerTest {
  // Preparation
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

  JavaClassType classType = factory.getClassType("Test");
  JavaClassType intType = factory.getClassType("int");
  JavaClassType doubleType = factory.getClassType("double");

  IdentityRef identityRef = JavaJimple.newThisRef(classType);

  IdentityRef identityRef0 = JavaJimple.newParameterRef(intType, 0);
  IdentityRef identityRef1 = JavaJimple.newParameterRef(doubleType, 1);

  // build locals
  Local l0 = JavaJimple.newLocal("l0", classType);
  Local l1 = JavaJimple.newLocal("l1", intType);
  Local l1hash1 = JavaJimple.newLocal("l1#1", intType);
  Local l2 = JavaJimple.newLocal("l2", intType);
  Local l2hash2 = JavaJimple.newLocal("l2#2", intType);
  Local l3 = JavaJimple.newLocal("l3", intType);
  Local l1hash3 = JavaJimple.newLocal("l1#3", intType);
  Local l2hash4 = JavaJimple.newLocal("l2#4", intType);
  Local l1hash5 = JavaJimple.newLocal("l1#5", intType);

  // build stmts
  Stmt startingStmt = JavaJimple.newIdentityStmt(l0, identityRef, noStmtPositionInfo);
  Stmt identityStmt0 = JavaJimple.newIdentityStmt(l1hash1, identityRef0, noStmtPositionInfo);
  Stmt identityStmt1 = JavaJimple.newIdentityStmt(l2hash2, identityRef1, noStmtPositionInfo);
  Stmt stmt1 = JavaJimple.newAssignStmt(l3, IntConstant.getInstance(10), noStmtPositionInfo);
  Stmt stmt2 = JavaJimple.newAssignStmt(l1hash3, IntConstant.getInstance(1), noStmtPositionInfo);
  Stmt stmt3 =
      JavaJimple.newAssignStmt(l2hash4, DoubleConstant.getInstance(2.0), noStmtPositionInfo);
  Stmt stmt4 =
      JavaJimple.newAssignStmt(
          l1hash5, JavaJimple.newAddExpr(l1hash3, IntConstant.getInstance(1)), noStmtPositionInfo);
  Stmt stmt5 =
      JavaJimple.newAssignStmt(
          l1hash5, JavaJimple.newAddExpr(l1hash5, IntConstant.getInstance(1)), noStmtPositionInfo);
  Stmt stmt6 = JavaJimple.newIfStmt(JavaJimple.newGtExpr(l1hash5, l3), noStmtPositionInfo);
  Stmt gt = JavaJimple.newGotoStmt(noStmtPositionInfo);
  Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

  @Test
  public void testLocalPacker() {
    Body body = createBody();
    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());
    // System.out.println(body);

    // LocalPacker localPacker = new LocalPacker();
    // localPacker.interceptBody(builder);
  }

  private Body createBody() {

    Body.BodyBuilder builder = Body.builder();

    List<Type> parameters = new ArrayList<>();
    parameters.add(intType);
    // parameters.add(doubleType);
    MethodSignature methodSignature =
        new MethodSignature(classType, "test", parameters, VoidType.getInstance());
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals =
        ImmutableUtils.immutableSet(l0, l1, l2, l3, l1hash1, l2hash2, l1hash3, l2hash4, l1hash5);
    builder.setLocals(locals);

    // build stmtGraph
    builder.addFlow(startingStmt, identityStmt0);
    builder.addFlow(identityStmt0, identityStmt1);
    builder.addFlow(identityStmt1, stmt1);
    builder.addFlow(stmt1, stmt2);
    builder.addFlow(stmt2, stmt3);
    builder.addFlow(stmt3, stmt4);
    builder.addFlow(stmt4, stmt5);
    builder.addFlow(stmt5, stmt6);
    builder.addFlow(stmt6, gt);
    builder.addFlow(gt, stmt5);
    builder.addFlow(stmt6, ret);

    builder.setStartingStmt(startingStmt);

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    return builder.build();
  }
}
