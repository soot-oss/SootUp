package sootup.core.jimple.common.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import sootup.core.TestUtil;
import sootup.core.jimple.basic.SimpleStmtPositionInfo;
import sootup.core.jimple.common.expr.JInterfaceInvokeExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;

public class JInvokeStmtTest {

  @Test
  public void testContainsInvokeExpr() {
    SimpleStmtPositionInfo pos = new SimpleStmtPositionInfo(1);

    JInvokeStmt invokeStmt = new JInvokeStmt(TestUtil.createDummyStaticInvokeExpr(), pos);
    assertTrue(invokeStmt.containsInvokeExpr());

    JInvokeStmt invokeStmt1 = new JInvokeStmt(TestUtil.createDummyInterfaceInvokeExpr(), pos);
    assertTrue(invokeStmt1.containsInvokeExpr());

    JInvokeStmt invokeStmt2 = new JInvokeStmt(TestUtil.createDummySpecialInvokeExpr(), pos);
    assertTrue(invokeStmt2.containsInvokeExpr());

    JInvokeStmt invokeStmt3 = new JInvokeStmt(TestUtil.createDummyVirtualInvokeExpr(), pos);
    assertTrue(invokeStmt3.containsInvokeExpr());
  }

  @Test
  public void testInvokesStaticInitializer() {
    SimpleStmtPositionInfo pos = new SimpleStmtPositionInfo(1);

    JInvokeStmt invokeStmt = new JInvokeStmt(TestUtil.createDummyStaticInvokeExpr(), pos);
    assertTrue(invokeStmt.invokesStaticInitializer());

    JInvokeStmt invokeStmt1 = new JInvokeStmt(TestUtil.createDummyInterfaceInvokeExpr(), pos);
    assertFalse(invokeStmt1.invokesStaticInitializer());

    JInvokeStmt invokeStmt2 = new JInvokeStmt(TestUtil.createDummySpecialInvokeExpr(), pos);
    assertFalse(invokeStmt2.invokesStaticInitializer());

    JInvokeStmt invokeStmt3 = new JInvokeStmt(TestUtil.createDummyVirtualInvokeExpr(), pos);
    assertFalse(invokeStmt3.invokesStaticInitializer());
  }

  @Test
  public void testGetInvokeExpr() {
    SimpleStmtPositionInfo pos = new SimpleStmtPositionInfo(1);

    JStaticInvokeExpr staticExpr = TestUtil.createDummyStaticInvokeExpr();
    JInvokeStmt invokeStmt = new JInvokeStmt(staticExpr, pos);
    assertEquals(staticExpr, invokeStmt.getInvokeExpr().get());

    JVirtualInvokeExpr virtualExpr = TestUtil.createDummyVirtualInvokeExpr();
    JInvokeStmt invokeStmt1 = new JInvokeStmt(virtualExpr, pos);
    assertEquals(virtualExpr, invokeStmt1.getInvokeExpr().get());

    JSpecialInvokeExpr specialExpr = TestUtil.createDummySpecialInvokeExpr();
    JInvokeStmt invokeStmt2 = new JInvokeStmt(specialExpr, pos);
    assertEquals(specialExpr, invokeStmt2.getInvokeExpr().get());

    JInterfaceInvokeExpr interfaceExpr = TestUtil.createDummyInterfaceInvokeExpr();
    JInvokeStmt invokeStmt3 = new JInvokeStmt(interfaceExpr, pos);
    assertEquals(interfaceExpr.toString(), invokeStmt3.getInvokeExpr().get().toString());
  }
}
