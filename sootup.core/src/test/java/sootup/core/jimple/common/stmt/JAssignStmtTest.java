package sootup.core.jimple.common.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.util.AssignStmtUtil;
import sootup.util.FieldRefUtil;
import sootup.util.InvokeExprUtil;
import sootup.util.LocalUtil;

@Tag("Java8")
public class JAssignStmtTest {

  @Test
  public void testInvokesStaticInitializer() {
    JStaticFieldRef staticFieldRef = FieldRefUtil.createDummyStaticFieldRef();
    JInstanceFieldRef instanceFieldRef = FieldRefUtil.createDummyInstanceFieldRef();
    Local local = LocalUtil.createDummyLocalForInt();

    assertTrue(
        AssignStmtUtil.createDummyAssignStmtWithExpr(InvokeExprUtil.createDummyStaticInvokeExpr())
            .invokesStaticInitializer());
    assertFalse(
        AssignStmtUtil.createDummyAssignStmtWithExpr(InvokeExprUtil.createDummyVirtualInvokeExpr())
            .invokesStaticInitializer());
    assertFalse(
        AssignStmtUtil.createDummyAssignStmtWithExpr(InvokeExprUtil.createDummySpecialInvokeExpr())
            .invokesStaticInitializer());
    assertFalse(
        AssignStmtUtil.createDummyAssignStmtWithExpr(
                InvokeExprUtil.createDummyInterfaceInvokeExpr())
            .invokesStaticInitializer());

    assertTrue(
        AssignStmtUtil.createDummyAssignStmt(staticFieldRef, local).invokesStaticInitializer());
    assertTrue(
        AssignStmtUtil.createDummyAssignStmt(local, staticFieldRef).invokesStaticInitializer());
    assertTrue(
        AssignStmtUtil.createDummyAssignStmt(staticFieldRef, staticFieldRef)
            .invokesStaticInitializer());
    assertTrue(
        AssignStmtUtil.createDummyAssignStmt(instanceFieldRef, staticFieldRef)
            .invokesStaticInitializer());
    assertTrue(
        AssignStmtUtil.createDummyAssignStmt(staticFieldRef, instanceFieldRef)
            .invokesStaticInitializer());

    assertFalse(
        AssignStmtUtil.createDummyAssignStmt(instanceFieldRef, local).invokesStaticInitializer());
    assertFalse(
        AssignStmtUtil.createDummyAssignStmt(local, instanceFieldRef).invokesStaticInitializer());
    assertFalse(
        AssignStmtUtil.createDummyAssignStmt(instanceFieldRef, instanceFieldRef)
            .invokesStaticInitializer());
    assertFalse(AssignStmtUtil.createDummyAssignStmtWithLocals().invokesStaticInitializer());
  }

  @Test
  public void testContainsInvokeExpr() {
    JStaticFieldRef staticFieldRef = FieldRefUtil.createDummyStaticFieldRef();
    JInstanceFieldRef instanceFieldRef = FieldRefUtil.createDummyInstanceFieldRef();
    Local local = LocalUtil.createDummyLocalForInt();

    assertTrue(
        AssignStmtUtil.createDummyAssignStmtWithExpr(InvokeExprUtil.createDummyStaticInvokeExpr())
            .containsInvokeExpr());

    assertFalse(AssignStmtUtil.createDummyAssignStmt(staticFieldRef, local).containsInvokeExpr());
    assertFalse(AssignStmtUtil.createDummyAssignStmt(local, staticFieldRef).containsInvokeExpr());
    assertFalse(
        AssignStmtUtil.createDummyAssignStmt(staticFieldRef, staticFieldRef).containsInvokeExpr());
    assertFalse(
        AssignStmtUtil.createDummyAssignStmt(instanceFieldRef, staticFieldRef)
            .containsInvokeExpr());
    assertFalse(
        AssignStmtUtil.createDummyAssignStmt(staticFieldRef, instanceFieldRef)
            .containsInvokeExpr());
    assertFalse(AssignStmtUtil.createDummyAssignStmt(instanceFieldRef, local).containsInvokeExpr());
    assertFalse(AssignStmtUtil.createDummyAssignStmt(local, instanceFieldRef).containsInvokeExpr());
    assertFalse(
        AssignStmtUtil.createDummyAssignStmt(instanceFieldRef, instanceFieldRef)
            .containsInvokeExpr());
    assertFalse(AssignStmtUtil.createDummyAssignStmtWithLocals().containsInvokeExpr());
  }

  @Test
  public void testGetInvokeExpr() {
    JStaticFieldRef staticFieldRef = FieldRefUtil.createDummyStaticFieldRef();
    JInstanceFieldRef instanceFieldRef = FieldRefUtil.createDummyInstanceFieldRef();
    Local local = LocalUtil.createDummyLocalForInt();

    assertEquals(
        InvokeExprUtil.createDummyStaticInvokeExpr().toString(),
        AssignStmtUtil.createDummyAssignStmtWithExpr(InvokeExprUtil.createDummyStaticInvokeExpr())
            .getInvokeExpr()
            .get()
            .toString());

    assertFalse(
        AssignStmtUtil.createDummyAssignStmt(staticFieldRef, local).getInvokeExpr().isPresent());
    assertFalse(
        AssignStmtUtil.createDummyAssignStmt(local, staticFieldRef).getInvokeExpr().isPresent());
    assertFalse(
        AssignStmtUtil.createDummyAssignStmt(staticFieldRef, staticFieldRef)
            .getInvokeExpr()
            .isPresent());
    assertFalse(
        AssignStmtUtil.createDummyAssignStmt(instanceFieldRef, staticFieldRef)
            .getInvokeExpr()
            .isPresent());
    assertFalse(
        AssignStmtUtil.createDummyAssignStmt(staticFieldRef, instanceFieldRef)
            .getInvokeExpr()
            .isPresent());
    assertFalse(
        AssignStmtUtil.createDummyAssignStmt(instanceFieldRef, local).getInvokeExpr().isPresent());
    assertFalse(
        AssignStmtUtil.createDummyAssignStmt(local, instanceFieldRef).getInvokeExpr().isPresent());
    assertFalse(
        AssignStmtUtil.createDummyAssignStmt(instanceFieldRef, instanceFieldRef)
            .getInvokeExpr()
            .isPresent());
    assertFalse(AssignStmtUtil.createDummyAssignStmtWithLocals().getInvokeExpr().isPresent());
  }
}
