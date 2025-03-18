package sootup.core.jimple.common.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import sootup.core.TestUtil;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;

public class JAssignStmtTest {

  @Test
  public void testInvokesStaticInitializer() {
    JStaticFieldRef staticFieldRef = TestUtil.createDummyStaticFieldRef();
    JInstanceFieldRef instanceFieldRef = TestUtil.createDummyInstanceFieldRef();
    Local local = TestUtil.createDummyLocalForInt();

    assertTrue(
        TestUtil.createDummyAssignStmtWithExpr(TestUtil.createDummyStaticInvokeExpr())
            .invokesStaticInitializer());
    assertFalse(
        TestUtil.createDummyAssignStmtWithExpr(TestUtil.createDummyVirtualInvokeExpr())
            .invokesStaticInitializer());
    assertFalse(
        TestUtil.createDummyAssignStmtWithExpr(TestUtil.createDummySpecialInvokeExpr())
            .invokesStaticInitializer());
    assertFalse(
        TestUtil.createDummyAssignStmtWithExpr(TestUtil.createDummyInterfaceInvokeExpr())
            .invokesStaticInitializer());

    assertTrue(TestUtil.createDummyAssignStmt(staticFieldRef, local).invokesStaticInitializer());
    assertTrue(TestUtil.createDummyAssignStmt(local, staticFieldRef).invokesStaticInitializer());
    assertTrue(
        TestUtil.createDummyAssignStmt(staticFieldRef, staticFieldRef).invokesStaticInitializer());
    assertTrue(
        TestUtil.createDummyAssignStmt(instanceFieldRef, staticFieldRef)
            .invokesStaticInitializer());
    assertTrue(
        TestUtil.createDummyAssignStmt(staticFieldRef, instanceFieldRef)
            .invokesStaticInitializer());

    assertFalse(TestUtil.createDummyAssignStmt(instanceFieldRef, local).invokesStaticInitializer());
    assertFalse(TestUtil.createDummyAssignStmt(local, instanceFieldRef).invokesStaticInitializer());
    assertFalse(
        TestUtil.createDummyAssignStmt(instanceFieldRef, instanceFieldRef)
            .invokesStaticInitializer());
    assertFalse(TestUtil.createDummyAssignStmtWithLocals().invokesStaticInitializer());
  }

  @Test
  public void testContainsInvokeExpr() {
    JStaticFieldRef staticFieldRef = TestUtil.createDummyStaticFieldRef();
    JInstanceFieldRef instanceFieldRef = TestUtil.createDummyInstanceFieldRef();
    Local local = TestUtil.createDummyLocalForInt();

    assertTrue(
        TestUtil.createDummyAssignStmtWithExpr(TestUtil.createDummyStaticInvokeExpr())
            .containsInvokeExpr());

    assertFalse(TestUtil.createDummyAssignStmt(staticFieldRef, local).containsInvokeExpr());
    assertFalse(TestUtil.createDummyAssignStmt(local, staticFieldRef).containsInvokeExpr());
    assertFalse(
        TestUtil.createDummyAssignStmt(staticFieldRef, staticFieldRef).containsInvokeExpr());
    assertFalse(
        TestUtil.createDummyAssignStmt(instanceFieldRef, staticFieldRef).containsInvokeExpr());
    assertFalse(
        TestUtil.createDummyAssignStmt(staticFieldRef, instanceFieldRef).containsInvokeExpr());
    assertFalse(TestUtil.createDummyAssignStmt(instanceFieldRef, local).containsInvokeExpr());
    assertFalse(TestUtil.createDummyAssignStmt(local, instanceFieldRef).containsInvokeExpr());
    assertFalse(
        TestUtil.createDummyAssignStmt(instanceFieldRef, instanceFieldRef).containsInvokeExpr());
    assertFalse(TestUtil.createDummyAssignStmtWithLocals().containsInvokeExpr());
  }

  @Test
  public void testGetInvokeExpr() {
    JStaticFieldRef staticFieldRef = TestUtil.createDummyStaticFieldRef();
    JInstanceFieldRef instanceFieldRef = TestUtil.createDummyInstanceFieldRef();
    Local local = TestUtil.createDummyLocalForInt();

    assertEquals(
        TestUtil.createDummyStaticInvokeExpr().toString(),
        TestUtil.createDummyAssignStmtWithExpr(TestUtil.createDummyStaticInvokeExpr())
            .getInvokeExpr()
            .get()
            .toString());

    assertFalse(TestUtil.createDummyAssignStmt(staticFieldRef, local).getInvokeExpr().isPresent());
    assertFalse(TestUtil.createDummyAssignStmt(local, staticFieldRef).getInvokeExpr().isPresent());
    assertFalse(
        TestUtil.createDummyAssignStmt(staticFieldRef, staticFieldRef).getInvokeExpr().isPresent());
    assertFalse(
        TestUtil.createDummyAssignStmt(instanceFieldRef, staticFieldRef)
            .getInvokeExpr()
            .isPresent());
    assertFalse(
        TestUtil.createDummyAssignStmt(staticFieldRef, instanceFieldRef)
            .getInvokeExpr()
            .isPresent());
    assertFalse(
        TestUtil.createDummyAssignStmt(instanceFieldRef, local).getInvokeExpr().isPresent());
    assertFalse(
        TestUtil.createDummyAssignStmt(local, instanceFieldRef).getInvokeExpr().isPresent());
    assertFalse(
        TestUtil.createDummyAssignStmt(instanceFieldRef, instanceFieldRef)
            .getInvokeExpr()
            .isPresent());
    assertFalse(TestUtil.createDummyAssignStmtWithLocals().getInvokeExpr().isPresent());
  }
}
