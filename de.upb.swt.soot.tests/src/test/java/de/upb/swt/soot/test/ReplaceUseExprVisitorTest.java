package de.upb.swt.soot.test;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.graph.Block;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.expr.Expr;
import de.upb.swt.soot.core.jimple.common.expr.JPhiExpr;
import de.upb.swt.soot.core.jimple.common.expr.JSpecialInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.JStaticInvokeExpr;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.visitor.ReplaceUseExprVisitor;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Zun Wang */
@Category(Java8Test.class)
public class ReplaceUseExprVisitorTest {

  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  JavaClassType intType = factory.getClassType("int");
  JavaClassType testClass = factory.getClassType("TestClass");
  JavaClassType voidType = factory.getClassType("void");
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

  Local op1 = JavaJimple.newLocal("op1", intType);
  Local op2 = JavaJimple.newLocal("op2", intType);
  Local newOp = JavaJimple.newLocal("op#", intType);

  Local base = JavaJimple.newLocal("base", testClass);
  Local arg1 = JavaJimple.newLocal("arg1", intType);
  Local arg2 = JavaJimple.newLocal("arg2", intType);
  Local arg3 = JavaJimple.newLocal("arg3", intType);
  Local newArg = JavaJimple.newLocal("argn", intType);

  Stmt stmt1 = Jimple.newAssignStmt(arg1, IntConstant.getInstance(0), noStmtPositionInfo);
  Stmt stmt2 = Jimple.newAssignStmt(arg2, IntConstant.getInstance(0), noStmtPositionInfo);
  Stmt stmt3 = Jimple.newAssignStmt(arg3, IntConstant.getInstance(0), noStmtPositionInfo);
  Stmt stmtPhi = Jimple.newAssignStmt(newArg, IntConstant.getInstance(0), noStmtPositionInfo);

  Block newBlock = new Block(stmtPhi, stmtPhi, ImmutableUtils.immutableList(stmtPhi), null);
  Block block1 = new Block(stmt1, stmt1, ImmutableUtils.immutableList(stmt1), null);
  Block block2 = new Block(stmt2, stmt2, ImmutableUtils.immutableList(stmt2), null);
  Block block3 = new Block(stmt3, stmt3, ImmutableUtils.immutableList(stmt3), null);

  MethodSignature methodeWithOutParas =
      new MethodSignature(testClass, "invokeExpr", Collections.emptyList(), voidType);

  /** Test use replacing in case BinopExpr. JaddExpr is as an example. */
  @Test
  public void testCaseBinopExpr() {

    ReplaceUseExprVisitor visitor = new ReplaceUseExprVisitor(op1, newOp);

    // replace op1 with newOp1
    Expr addExpr = JavaJimple.newAddExpr(op1, op2);
    addExpr.accept(visitor);
    Expr newExpr = visitor.getNewExpr();

    List<Value> expectedUses = new ArrayList<>();
    expectedUses.add(newOp);
    expectedUses.add(op2);
    assertTrue(newExpr.getUses().equals(expectedUses));

    // replace op1 and op1 with newOp1
    addExpr = JavaJimple.newAddExpr(op1, op1);
    addExpr.accept(visitor);
    newExpr = visitor.getNewExpr();

    expectedUses.set(1, newOp);
    assertTrue(newExpr.getUses().equals(expectedUses));

    // there's no matched op in Expr
    addExpr = JavaJimple.newAddExpr(op2, op2);
    addExpr.accept(visitor);
    newExpr = visitor.getNewExpr();
    assertTrue(newExpr.equivTo(addExpr));

    // replace op1 with phi
    JPhiExpr phi = JPhiExpr.getEmptyPhi();
    phi.addArg(arg1, block1);
    phi.addArg(arg2, block2);
    visitor = new ReplaceUseExprVisitor(op1, phi);
    addExpr = JavaJimple.newAddExpr(op1, op2);
    addExpr.accept(visitor);
    newExpr = visitor.getNewExpr();

    expectedUses.clear();
    expectedUses.addAll(ImmutableUtils.immutableList(arg1, arg2, phi, op2));
    assertTrue(newExpr.getUses().equals(expectedUses));

    // replace op1 and op1 with newOp1
    addExpr = JavaJimple.newAddExpr(op1, op1);
    addExpr.accept(visitor);
    newExpr = visitor.getNewExpr();

    expectedUses.clear();
    expectedUses.addAll(ImmutableUtils.immutableList(arg1, arg2, phi, arg1, arg2, phi));
    assertTrue(newExpr.getUses().equals(expectedUses));
  }

  /**
   * Test use replacing in case JStaticInvokeExpr JDynamicInvoke JNewMultiArrayExpr.
   * JStaticInvokeExpr is as an example.
   */
  @Test
  public void testCaseInvokeExpr() {

    List<Immediate> args = new ArrayList<Immediate>();
    args.add(arg1);
    args.add(arg2);
    args.add(arg3);
    List<Type> parameters = new ArrayList<>();
    parameters.add(intType);
    parameters.add(intType);
    parameters.add(intType);

    MethodSignature method = new MethodSignature(testClass, "invokeExpr", parameters, voidType);

    ReplaceUseExprVisitor visitor = new ReplaceUseExprVisitor(arg1, newArg);

    // replace arg1 in args with newArg
    Expr invokeExpr = new JStaticInvokeExpr(method, args);
    invokeExpr.accept(visitor);
    Expr newInvokeExpr = visitor.getNewExpr();

    List<Value> expectedUses = new ArrayList<>();
    expectedUses.add(newArg);
    expectedUses.add(arg2);
    expectedUses.add(arg3);
    assertTrue(newInvokeExpr.getUses().equals(expectedUses));

    // repalce arg1 2 times in args with newArg
    args.set(2, arg1);
    invokeExpr = new JStaticInvokeExpr(method, args);
    invokeExpr.accept(visitor);
    newInvokeExpr = visitor.getNewExpr();

    expectedUses.set(2, newArg);
    assertTrue(newInvokeExpr.getUses().equals(expectedUses));

    // There's no matched arg in args
    invokeExpr = new JStaticInvokeExpr(methodeWithOutParas, Collections.emptyList());
    invokeExpr.accept(visitor);
    assertTrue(visitor.getNewExpr().equivTo(invokeExpr));

    // replace arg1 with phi
    JPhiExpr phi = JPhiExpr.getEmptyPhi();
    phi.addArg(arg1, block1);
    phi.addArg(arg2, block2);
    args.set(2, arg3);
    visitor = new ReplaceUseExprVisitor(arg1, phi);
    invokeExpr = new JStaticInvokeExpr(method, args);
    invokeExpr.accept(visitor);
    newInvokeExpr = visitor.getNewExpr();
    expectedUses.clear();
    expectedUses.addAll(ImmutableUtils.immutableList(phi, arg2, arg3, arg1, arg2));
    assertTrue(newInvokeExpr.getUses().equals(expectedUses));
  }

  /** Test use replacing in case InstanceInvokeExpr. JSpecialInvokeExpr is as an example. */
  @Test
  public void testCaseInstanceInvokeExpr() {

    List<Immediate> args = new ArrayList<Immediate>();
    args.add(arg1);
    args.add(arg2);
    args.add(arg3);
    List<Type> parameters = new ArrayList<>();
    parameters.add(intType);
    parameters.add(intType);
    parameters.add(intType);

    MethodSignature method = new MethodSignature(testClass, "invokeExpr", parameters, voidType);

    ReplaceUseExprVisitor visitor = new ReplaceUseExprVisitor(arg1, newArg);

    // replace arg1 in case and args with newArg
    Expr invokeExpr = new JSpecialInvokeExpr(arg1, method, args);
    invokeExpr.accept(visitor);
    Expr newInvokeExpr = visitor.getNewExpr();

    List<Value> expectedUses = new ArrayList<>();
    expectedUses.add(newArg);
    expectedUses.add(arg2);
    expectedUses.add(arg3);
    expectedUses.add(newArg);
    assertTrue(newInvokeExpr.getUses().equals(expectedUses));

    // replace arg1 in args with newArg
    args.set(2, arg1);
    invokeExpr = new JSpecialInvokeExpr(base, method, args);
    invokeExpr.accept(visitor);
    newInvokeExpr = visitor.getNewExpr();

    expectedUses.set(2, newArg);
    expectedUses.set(3, base);

    assertTrue(newInvokeExpr.getUses().equals(expectedUses));

    // replace arg1=base with newArg
    invokeExpr = new JSpecialInvokeExpr(arg1, methodeWithOutParas, Collections.emptyList());
    invokeExpr.accept(visitor);
    newInvokeExpr = visitor.getNewExpr();
    expectedUses.clear();
    expectedUses.add(newArg);
    assertTrue(newInvokeExpr.getUses().equals(expectedUses));

    // There's no matched arg in args, no matched base
    invokeExpr = new JSpecialInvokeExpr(base, methodeWithOutParas, Collections.emptyList());
    invokeExpr.accept(visitor);
    newInvokeExpr = visitor.getNewExpr();
    assertTrue(newInvokeExpr.equivTo(invokeExpr));

    // replace arg1 in base and args with phi
    JPhiExpr phi = JPhiExpr.getEmptyPhi();
    phi.addArg(arg1, block1);
    phi.addArg(arg2, block2);
    visitor = new ReplaceUseExprVisitor(arg1, phi);
    args.set(2, arg3);
    invokeExpr = new JSpecialInvokeExpr(arg1, method, args);
    invokeExpr.accept(visitor);
    newInvokeExpr = visitor.getNewExpr();

    expectedUses.clear();
    expectedUses.addAll(ImmutableUtils.immutableList(phi, arg2, arg3, arg1, arg2, arg1, arg2, phi));
    assertTrue(newInvokeExpr.getUses().equals(expectedUses));
  }

  /**
   * Test use replacing in case UnopExpr, JCastExpr, JInstanceOfExpr, JNewArrayExpr. JLengthExpr is
   * as an example.
   */
  @Test
  public void testUnopExpr() {

    ReplaceUseExprVisitor visitor = new ReplaceUseExprVisitor(op1, newOp);

    // replace op1 with newOp1
    Expr lengthExpr = Jimple.newLengthExpr(op1);
    lengthExpr.accept(visitor);
    Expr newExpr = visitor.getNewExpr();

    List<Value> expectedUses = new ArrayList<>();
    expectedUses.add(newOp);
    assertTrue(newExpr.getUses().equals(expectedUses));

    // There's no matched op
    lengthExpr = Jimple.newLengthExpr(op2);
    lengthExpr.accept(visitor);
    assertTrue(visitor.getNewExpr().equivTo(lengthExpr));

    // replace op1 with phi
    JPhiExpr phi = JPhiExpr.getEmptyPhi();
    phi.addArg(arg1, block1);
    phi.addArg(arg2, block2);
    visitor = new ReplaceUseExprVisitor(op1, phi);
    lengthExpr = Jimple.newLengthExpr(op1);
    lengthExpr.accept(visitor);
    newExpr = visitor.getNewExpr();

    expectedUses.clear();
    expectedUses.addAll(ImmutableUtils.immutableList(arg1, arg2, phi));
    assertTrue(newExpr.getUses().equals(expectedUses));
  }

  /** Test use replacing in case JPhiExpr. */
  @Test
  public void testPhiExpr() {

    ReplaceUseExprVisitor visitor = new ReplaceUseExprVisitor(arg2, newArg, newBlock);

    Set<Local> argsSet = ImmutableUtils.immutableSet(arg1, arg2, arg3);
    LinkedHashSet<Local> args = new LinkedHashSet<>(argsSet);
    Map<Local, Block> argToBlock = new HashMap<>();

    argToBlock.put(arg1, block1);
    argToBlock.put(arg2, block2);
    argToBlock.put(arg3, block3);
    Expr expr = Jimple.newPhiExpr(args, argToBlock);

    expr.accept(visitor);
    JPhiExpr newExpr = (JPhiExpr) visitor.getNewExpr();

    List<Local> expectedArgs = ImmutableUtils.immutableList(arg1, newArg, arg3);
    List<Block> expectedBlocks = ImmutableUtils.immutableList(block1, newBlock, block3);

    assertListsEquiv(expectedArgs, new ArrayList<>(newExpr.getArgs()));
    assertListsEquiv(expectedBlocks, new ArrayList<>(newExpr.getBlocks()));
  }

  // assert whether two lists are equal
  public void assertListsEquiv(List expected, List actual) {

    assertNotNull(expected);
    assertNotNull(actual);
    if (expected.size() != actual.size()) {
      System.out.println("Expected size is not equal to actual size: ");
      System.out.println("expected size of list: " + expected.size());
      System.out.println("actual size of list: " + actual.size());
    }
    assertEquals(expected.size(), actual.size());
    boolean condition = true;
    for (Object o : actual) {
      int idx = actual.indexOf(o);
      if (!(expected.get(idx) == o)) {
        condition = false;
        break;
      }
    }
    if (!condition) {
      System.out.println("expected:");
      System.out.println(expected);
      System.out.println("actual:");
      System.out.println(actual);
    }
    assertTrue(condition);
  }
}
