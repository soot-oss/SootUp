package de.upb.swt.soot.test.core.jimple.javabytecode.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JReturnStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JReturnVoidStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JSwitchStmt;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class JSwitchStmtTest {

  @Test
  public void test() {
    // TODO: [ms] incorporate Printer i.e. Body+Targets
    testLookupSwitchStmt();
    testTableSwitchStmt();
  }

  public void testLookupSwitchStmt() {
    StmtPositionInfo nop = StmtPositionInfo.createNoStmtPositionInfo();
    ArrayList<IntConstant> lookupValues = new ArrayList<>();
    ArrayList<Stmt> targets = new ArrayList<>();

    Stmt stmt = new JSwitchStmt(IntConstant.getInstance(42), lookupValues, nop);
    Stmt stmtDifferentKey = new JSwitchStmt(IntConstant.getInstance(123), lookupValues, nop);
    Stmt stmtDifferentDefault = new JSwitchStmt(IntConstant.getInstance(42), lookupValues, nop);

    // toString
    assertEquals("switch(42) {     default:  }", stmt.toString());

    targets.add(new JReturnVoidStmt(nop));
    targets.add(new JNopStmt(nop));

    lookupValues.add(IntConstant.getInstance(42));
    lookupValues.add(IntConstant.getInstance(33102));

    Stmt switchWithDefault = new JSwitchStmt(IntConstant.getInstance(123), lookupValues, nop);
    assertEquals(
        "switch(123) {     case 42:     case 33102:     default:  }", switchWithDefault.toString());

    // equivTo
    assertFalse(stmt.equivTo(this));
    assertTrue(stmt.equivTo(stmt));
    assertFalse(stmt.equivTo(switchWithDefault));
    assertFalse(stmt.equivTo(stmtDifferentDefault));
    assertFalse(stmt.equivTo(stmtDifferentKey));
  }

  public void testTableSwitchStmt() {
    StmtPositionInfo nop = StmtPositionInfo.createNoStmtPositionInfo();
    ArrayList<Stmt> targets = new ArrayList<>();
    targets.add(new JReturnStmt(IntConstant.getInstance(1), nop));
    targets.add(new JReturnStmt(IntConstant.getInstance(2), nop));
    targets.add(new JReturnStmt(IntConstant.getInstance(3), nop));
    targets.add(new JNopStmt(nop));
    Stmt stmt = new JSwitchStmt(IntConstant.getInstance(123), 1, 4, nop);

    ArrayList<Stmt> targets2 = new ArrayList<>();
    targets.add(new JReturnStmt(IntConstant.getInstance(1), nop));
    targets.add(new JReturnStmt(IntConstant.getInstance(2), nop));
    targets.add(new JNopStmt(nop));
    targets.add(new JReturnStmt(IntConstant.getInstance(3), nop));
    Stmt stmt2 = new JSwitchStmt(IntConstant.getInstance(123), 1, 4, nop);
    Stmt stmt3 = new JSwitchStmt(IntConstant.getInstance(456), 1, 4, nop);
    Stmt stmt4 = new JSwitchStmt(IntConstant.getInstance(123), 2, 4, nop);
    Stmt stmt5 = new JSwitchStmt(IntConstant.getInstance(123), 1, 5, nop);
    Stmt stmt6 = new JSwitchStmt(IntConstant.getInstance(123), 1, 4, nop);

    // toString
    assertEquals(
        "switch(123) {     case 1:     case 2:     case 3:     case 4:     default:  }",
        stmt.toString());

    // equivTo
    assertFalse(stmt.equivTo(new Integer(666)));
    assertTrue(stmt.equivTo(stmt));
    assertFalse(stmt.equivTo(stmt2));

    assertFalse(stmt.equivTo(stmt2));
    assertFalse(stmt.equivTo(stmt3));
    assertFalse(stmt.equivTo(stmt4));
    assertFalse(stmt.equivTo(stmt5));
    assertFalse(stmt.equivTo(stmt6));
  }

  @Test
  public void testTableSwitch() {
    StmtPositionInfo nop = StmtPositionInfo.createNoStmtPositionInfo();
    Stmt stmt = new JSwitchStmt(IntConstant.getInstance(123), 1, 4, nop);
    Stmt diffSwitch = new JSwitchStmt(IntConstant.getInstance(123), 0, 4, nop);
    Stmt diffKeySwitch = new JSwitchStmt(IntConstant.getInstance(42), 1, 4, nop);

    // toString
    assertEquals(
        "switch(123) {     case 1:     case 2:     case 3:     case 4:     default:  }",
        stmt.toString());

    // equivTo
    assertFalse(stmt.equivTo(new Integer(666)));
    assertTrue(stmt.equivTo(stmt));
    assertFalse(stmt.equivTo(diffSwitch));
    assertFalse(stmt.equivTo(diffKeySwitch));
  }

  @Test
  public void testLookupSwitch() {
    StmtPositionInfo nop = StmtPositionInfo.createNoStmtPositionInfo();
    ArrayList<IntConstant> lookupValues = new ArrayList<>();

    Stmt switchStmt = new JSwitchStmt(IntConstant.getInstance(42), lookupValues, nop);
    Stmt stmtDifferentKey = new JSwitchStmt(IntConstant.getInstance(123), lookupValues, nop);
    Stmt stmtDifferentDefault = new JSwitchStmt(IntConstant.getInstance(42), lookupValues, nop);

    // toString
    assertEquals("switch(42) {     default:  }", switchStmt.toString());

    lookupValues.add(IntConstant.getInstance(42));
    lookupValues.add(IntConstant.getInstance(33102));

    Stmt differentSwitchStmt = new JSwitchStmt(IntConstant.getInstance(123), lookupValues, nop);
    assertEquals(
        "switch(123) {     case 42:     case 33102:     default:  }",
        differentSwitchStmt.toString());

    // equivTo
    assertFalse(switchStmt.equivTo(this));
    assertTrue(switchStmt.equivTo(switchStmt));
    assertFalse(switchStmt.equivTo(differentSwitchStmt));
    assertFalse(switchStmt.equivTo(stmtDifferentDefault));
    assertFalse(switchStmt.equivTo(stmtDifferentKey));
  }
}
