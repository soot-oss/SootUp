package sootup.java.core.jimple.javabytecode.stmt;

import static org.junit.Assert.*;

import categories.Java8Test;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.JNopStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;

@Category(Java8Test.class)
public class JSwitchStmtTest {

  @Test
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

  @Test
  public void testTableSwitchStmt() {
    StmtPositionInfo nop = StmtPositionInfo.createNoStmtPositionInfo();
    ArrayList<Stmt> targets = new ArrayList<>();
    targets.add(new JReturnStmt(IntConstant.getInstance(1), nop));
    targets.add(new JReturnStmt(IntConstant.getInstance(2), nop));
    targets.add(new JReturnStmt(IntConstant.getInstance(3), nop));
    targets.add(new JNopStmt(nop));
    JSwitchStmt stmt = new JSwitchStmt(IntConstant.getInstance(123), 1, 4, nop);

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
    assertFalse(stmt.equivTo(666));
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
    JSwitchStmt stmt = new JSwitchStmt(IntConstant.getInstance(123), 1, 4, nop);
    JSwitchStmt diffSwitch = new JSwitchStmt(IntConstant.getInstance(123), 0, 4, nop);
    JSwitchStmt diffKeySwitch = new JSwitchStmt(IntConstant.getInstance(42), 1, 4, nop);

    // toString
    assertEquals(
        "switch(123) {     case 1:     case 2:     case 3:     case 4:     default:  }",
        stmt.toString());

    // equivTo
    assertFalse(stmt.equivTo(666));
    assertTrue(stmt.equivTo(stmt));
    assertFalse(stmt.equivTo(diffSwitch));
    assertFalse(stmt.equivTo(diffKeySwitch));

    assertTrue(stmt.isTableSwitch());
    assertEquals(5, stmt.getValueCount());
    List<IntConstant> values = stmt.getValues();

    assertEquals(4, values.size());

    assertFalse(values.contains(IntConstant.getInstance(0)));
    assertTrue(values.contains(IntConstant.getInstance(1)));
    assertTrue(values.contains(IntConstant.getInstance(2)));
    assertTrue(values.contains(IntConstant.getInstance(3)));
    assertTrue(values.contains(IntConstant.getInstance(4)));
    assertFalse(values.contains(IntConstant.getInstance(5)));

    try {
      values.get(-1);
      fail("should be outoufbounds");
    } catch (IndexOutOfBoundsException ignored) {
    }
    assertEquals(IntConstant.getInstance(1), values.get(0));
    assertEquals(IntConstant.getInstance(2), values.get(1));
    assertEquals(IntConstant.getInstance(3), values.get(2));
    assertEquals(IntConstant.getInstance(4), values.get(3));
    try {
      values.get(4);
      fail("should be outoufbounds");
    } catch (IndexOutOfBoundsException ignored) {
    }

    assertArrayEquals(
        new Object[] {
          IntConstant.getInstance(1),
          IntConstant.getInstance(2),
          IntConstant.getInstance(3),
          IntConstant.getInstance(4)
        },
        values.toArray());
    assertArrayEquals(
        new IntConstant[] {
          IntConstant.getInstance(1),
          IntConstant.getInstance(2),
          IntConstant.getInstance(3),
          IntConstant.getInstance(4)
        },
        values.toArray(new IntConstant[0]));

    assertTrue(
        values.containsAll(
            Arrays.asList(
                IntConstant.getInstance(1),
                IntConstant.getInstance(2),
                IntConstant.getInstance(3),
                IntConstant.getInstance(4))));
    assertFalse(
        values.containsAll(
            Arrays.asList(
                IntConstant.getInstance(42),
                IntConstant.getInstance(2),
                IntConstant.getInstance(3),
                IntConstant.getInstance(4))));
    assertFalse(values.containsAll(Arrays.asList(IntConstant.getInstance(0))));
    assertTrue(values.containsAll(Arrays.asList(IntConstant.getInstance(1))));
    assertTrue(values.containsAll(Arrays.asList(IntConstant.getInstance(4))));
    assertFalse(values.containsAll(Arrays.asList(IntConstant.getInstance(5))));

    assertFalse(
        values.containsAll(
            Arrays.asList(
                IntConstant.getInstance(42),
                IntConstant.getInstance(2),
                IntConstant.getInstance(3),
                IntConstant.getInstance(4))));

    ListIterator<IntConstant> listIt = values.listIterator();

    try {
      listIt.previous();
      fail("should be outoufbounds");
    } catch (IndexOutOfBoundsException ignored) {
    }
    assertEquals(IntConstant.getInstance(1), listIt.next());
    assertEquals(IntConstant.getInstance(2), listIt.next());
    assertEquals(IntConstant.getInstance(1), listIt.previous());
    assertEquals(IntConstant.getInstance(2), listIt.next());
    assertEquals(IntConstant.getInstance(3), listIt.next());
    assertEquals(IntConstant.getInstance(4), listIt.next());
    try {
      listIt.next();
      fail("should be outoufbounds");
    } catch (IndexOutOfBoundsException ignored) {
    }
    assertEquals(IntConstant.getInstance(3), listIt.previous());

    List<IntConstant> sublist = values.subList(1, 3);
    assertEquals(3, sublist.size());
    assertEquals(IntConstant.getInstance(2), sublist.get(0));
    assertEquals(IntConstant.getInstance(3), sublist.get(1));
    assertEquals(IntConstant.getInstance(4), sublist.get(2));
    try {
      sublist.get(3);
      fail("should be outoufbounds");
    } catch (IndexOutOfBoundsException ignored) {
    }

    try {
      sublist.clear();
      fail("should be unsupported");
    } catch (UnsupportedOperationException ignored) {
    }
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
