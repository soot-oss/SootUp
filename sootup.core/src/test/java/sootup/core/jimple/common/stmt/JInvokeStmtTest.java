package sootup.core.jimple.common.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.basic.SimpleStmtPositionInfo;
import sootup.core.jimple.common.expr.JInterfaceInvokeExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.util.InvokeExprUtil;

@Tag("Java8")
public class JInvokeStmtTest {

  @Test
  public void testContainsInvokeExpr() {
    SimpleStmtPositionInfo pos = new SimpleStmtPositionInfo(1);

    JInvokeStmt invokeStmt = new JInvokeStmt(InvokeExprUtil.createDummyStaticInvokeExpr(), pos);
    assertTrue(invokeStmt.containsInvokeExpr());

    JInvokeStmt invokeStmt1 = new JInvokeStmt(InvokeExprUtil.createDummyInterfaceInvokeExpr(), pos);
    assertTrue(invokeStmt1.containsInvokeExpr());

    JInvokeStmt invokeStmt2 = new JInvokeStmt(InvokeExprUtil.createDummySpecialInvokeExpr(), pos);
    assertTrue(invokeStmt2.containsInvokeExpr());

    JInvokeStmt invokeStmt3 = new JInvokeStmt(InvokeExprUtil.createDummyVirtualInvokeExpr(), pos);
    assertTrue(invokeStmt3.containsInvokeExpr());
  }

  @Test
  public void testGetInvokeExpr() {
    SimpleStmtPositionInfo pos = new SimpleStmtPositionInfo(1);

    JStaticInvokeExpr staticExpr = InvokeExprUtil.createDummyStaticInvokeExpr();
    JInvokeStmt invokeStmt = new JInvokeStmt(staticExpr, pos);
    assertEquals(staticExpr, invokeStmt.getInvokeExpr().get());

    JVirtualInvokeExpr virtualExpr = InvokeExprUtil.createDummyVirtualInvokeExpr();
    JInvokeStmt invokeStmt1 = new JInvokeStmt(virtualExpr, pos);
    assertEquals(virtualExpr, invokeStmt1.getInvokeExpr().get());

    JSpecialInvokeExpr specialExpr = InvokeExprUtil.createDummySpecialInvokeExpr();
    JInvokeStmt invokeStmt2 = new JInvokeStmt(specialExpr, pos);
    assertEquals(specialExpr, invokeStmt2.getInvokeExpr().get());

    JInterfaceInvokeExpr interfaceExpr = InvokeExprUtil.createDummyInterfaceInvokeExpr();
    JInvokeStmt invokeStmt3 = new JInvokeStmt(staticExpr, pos);
    assertEquals(interfaceExpr, invokeStmt3.getInvokeExpr().get());
  }
}
