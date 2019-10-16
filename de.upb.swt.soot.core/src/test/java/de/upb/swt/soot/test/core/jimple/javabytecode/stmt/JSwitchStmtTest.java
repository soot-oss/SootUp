package de.upb.swt.soot.test.core.jimple.javabytecode.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.PositionInfo;
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
    testLookupSwitchStmt();
    testTableSwitchStmt();
  }

  public void testLookupSwitchStmt(){
    PositionInfo nop = PositionInfo.createNoPositionInfo();
    ArrayList<IntConstant> lookupValues = new ArrayList<>();
    ArrayList<Stmt> targets = new ArrayList<>();

    Stmt stmt =
            new JSwitchStmt(
                    IntConstant.getInstance(42), lookupValues, targets, new JNopStmt(nop), nop);
    Stmt stmtDifferentKey =
            new JSwitchStmt(
                    IntConstant.getInstance(123), lookupValues, targets, new JNopStmt(nop), nop);
    Stmt stmtDifferentDefault =
            new JSwitchStmt(
                    IntConstant.getInstance(42),
                    lookupValues,
                    targets,
                    new JReturnStmt(IntConstant.getInstance(42), nop),
                    nop);

    // toString
    assertEquals("lookupswitch(42) {     default: goto nop; }", stmt.toString());

    targets.add(new JReturnVoidStmt(nop));
    targets.add(new JNopStmt(nop));

    lookupValues.add(IntConstant.getInstance(42));
    lookupValues.add(IntConstant.getInstance(33102));

    Stmt stmtDifferentLookupAndTarget =
            new JSwitchStmt(
                    IntConstant.getInstance(123), lookupValues, targets, new JNopStmt(nop), nop);
    assertEquals(
            "lookupswitch(123) {     case 42: goto return;     case 33102: goto nop;     default: goto nop; }",
            stmtDifferentLookupAndTarget.toString());

    // equivTo
    assertFalse(stmt.equivTo(this));
    assertTrue(stmt.equivTo(stmt));
    assertFalse(stmt.equivTo(stmtDifferentLookupAndTarget));
    assertFalse(stmt.equivTo(stmtDifferentDefault));
    assertFalse(stmt.equivTo(stmtDifferentKey));
  }

  public void testTableSwitchStmt(){
    PositionInfo nop = PositionInfo.createNoPositionInfo();
    ArrayList<Stmt> targets = new ArrayList<>();
    targets.add(new JReturnStmt(IntConstant.getInstance(1), nop));
    targets.add(new JReturnStmt(IntConstant.getInstance(2), nop));
    targets.add(new JReturnStmt(IntConstant.getInstance(3), nop));
    targets.add(new JNopStmt(nop));
    Stmt stmt =
            new JSwitchStmt(
                    IntConstant.getInstance(123),
                    1,
                    4,
                    targets,
                    new JReturnStmt(IntConstant.getInstance(666), nop),
                    nop);

    ArrayList<Stmt> targets2 = new ArrayList<>();
    targets.add(new JReturnStmt(IntConstant.getInstance(1), nop));
    targets.add(new JReturnStmt(IntConstant.getInstance(2), nop));
    targets.add(new JNopStmt(nop));
    targets.add(new JReturnStmt(IntConstant.getInstance(3), nop));
    Stmt stmt2 =
            new JSwitchStmt(
                    IntConstant.getInstance(123),
                    1,
                    4,
                    targets2,
                    new JReturnStmt(IntConstant.getInstance(666), nop),
                    nop);
    Stmt stmt3 =
            new JSwitchStmt(
                    IntConstant.getInstance(456),
                    1,
                    4,
                    targets,
                    new JReturnStmt(IntConstant.getInstance(666), nop),
                    nop);
    Stmt stmt4 =
            new JSwitchStmt(
                    IntConstant.getInstance(123),
                    2,
                    4,
                    targets,
                    new JReturnStmt(IntConstant.getInstance(666), nop),
                    nop);
    Stmt stmt5 =
            new JSwitchStmt(
                    IntConstant.getInstance(123),
                    1,
                    5,
                    targets,
                    new JReturnStmt(IntConstant.getInstance(666), nop),
                    nop);
    Stmt stmt6 =
            new JSwitchStmt(IntConstant.getInstance(123), 1, 4, targets, new JNopStmt(nop), nop);

    // toString
    assertEquals(
            "tableswitch(123) {     case 1: goto return 1;     case 2: goto return 2;     case 3: goto return 3;     case 4: goto nop;     default: goto return 666; }",
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
}
