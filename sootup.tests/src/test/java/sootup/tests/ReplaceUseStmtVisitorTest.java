package sootup.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.Expr;
import sootup.core.jimple.common.ref.Ref;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.visitor.ReplaceUseStmtVisitor;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;

/** @author Zun Wang */
@Tag("Java8")
public class ReplaceUseStmtVisitorTest {
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  JavaJimple javaJimple = JavaJimple.getInstance();
  JavaClassType intType = factory.getClassType("int");
  JavaClassType testClass = factory.getClassType("TestClass");
  JavaClassType voidType = factory.getClassType("void");

  Local op1 = JavaJimple.newLocal("op1", intType);
  Local op2 = JavaJimple.newLocal("op2", intType);
  Local newOp = JavaJimple.newLocal("op#", intType);
  Local var = JavaJimple.newLocal("var", intType);

  Local base = JavaJimple.newLocal("base", testClass);

  MethodSignature methodeWithOutParas =
      new MethodSignature(testClass, "invokeExpr", Collections.emptyList(), voidType);

  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();

  /** Test use replacing in case JAssignStmt. */
  @Test
  public void testcaseAssignStmt() {

    ReplaceUseStmtVisitor visitor = new ReplaceUseStmtVisitor(op1, newOp);

    // rValue is a Expr
    Expr addExpr = JavaJimple.newAddExpr(op1, op2);
    Stmt stmt = JavaJimple.newAssignStmt(var, addExpr, noStmtPositionInfo);
    stmt.accept(visitor);
    Stmt newStmt = visitor.getResult();

    List<Value> expectedUses = new ArrayList<>();

    expectedUses.add(JavaJimple.newAddExpr(newOp, op2));
    expectedUses.add(newOp);
    expectedUses.add(op2);

    boolean isExpected = false;
    List<Value> collect = newStmt.getUses().collect(Collectors.toList());
    for (int i = 0; i < 3; i++) {
      isExpected = collect.get(i).equivTo(expectedUses.get(i));
      if (!isExpected) {
        break;
      }
    }
    assertTrue(isExpected);

    // rValue is a Ref
    Ref ref = javaJimple.newArrayRef(op1, op2);
    stmt = JavaJimple.newAssignStmt(var, ref, noStmtPositionInfo);
    stmt.accept(visitor);
    newStmt = visitor.getResult();

    expectedUses.set(0, javaJimple.newArrayRef(newOp, op2));
    expectedUses.set(1, newOp);

    isExpected = false;
    collect = newStmt.getUses().collect(Collectors.toList());
    for (int i = 0; i < 3; i++) {
      isExpected = collect.get(i).equivTo(expectedUses.get(i));
      if (!isExpected) {
        break;
      }
    }
    assertTrue(isExpected);

    // rValue is a Local, use local to replace it
    stmt = JavaJimple.newAssignStmt(var, op1, noStmtPositionInfo);
    stmt.accept(visitor);
    stmt = visitor.getResult();

    expectedUses.clear();
    expectedUses.add(newOp);

    assertEquals(expectedUses, stmt.getUses().collect(Collectors.toList()));
  }

  /** Test use replacing in case JInvokeStmt and JIfStmt Here JInvokeStmt is as an example */
  @Test
  public void testcaseInvokeStmt() {

    ReplaceUseStmtVisitor visitor = new ReplaceUseStmtVisitor(base, newOp);

    // invokeExpr
    AbstractInvokeExpr invokeExpr =
        JavaJimple.newSpecialInvokeExpr(base, methodeWithOutParas, Collections.emptyList());
    Stmt stmt = JavaJimple.newInvokeStmt(invokeExpr, noStmtPositionInfo);
    stmt.accept(visitor);
    Stmt newStmt = visitor.getResult();

    List<Value> expectedUses = new ArrayList<>();
    expectedUses.add(newOp);
    expectedUses.add(
        JavaJimple.newSpecialInvokeExpr(newOp, methodeWithOutParas, Collections.emptyList()));

    boolean isExpected = false;
    List<Value> collect = newStmt.getUses().collect(Collectors.toList());
    for (int i = 0; i < 2; i++) {
      isExpected = collect.get(i).equivTo(expectedUses.get(i));
      if (!isExpected) {
        break;
      }
    }
    assertTrue(isExpected);
  }

  /** Test use replacing in other cases Here JReturnStmt is as an example */
  @Test
  public void testCaseReturnStmt() {
    ReplaceUseStmtVisitor visitor = new ReplaceUseStmtVisitor(op1, newOp);
    Stmt stmt = JavaJimple.newRetStmt(op1, noStmtPositionInfo);
    stmt.accept(visitor);
    Stmt newStmt = visitor.getResult();

    List<Value> expectedUses = new ArrayList<>();
    expectedUses.add(newOp);
    assertEquals(expectedUses, newStmt.getUses().collect(Collectors.toList()));
  }
}
