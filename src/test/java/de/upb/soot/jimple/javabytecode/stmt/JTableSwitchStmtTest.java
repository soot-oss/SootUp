package de.upb.soot.jimple.javabytecode.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.upb.soot.jimple.common.constant.IntConstant;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.stmt.JNopStmt;
import de.upb.soot.jimple.common.stmt.JReturnStmt;
import de.upb.soot.jimple.common.stmt.JReturnVoidStmt;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

@Category(Java8Test.class)
public class JTableSwitchStmtTest {

  @Test
  public void test() {

    ArrayList<IStmt> targets = new ArrayList<>();
    targets.add(new JReturnStmt( IntConstant.getInstance(1)));
    targets.add(new JReturnStmt( IntConstant.getInstance(2)));
    targets.add(new JReturnStmt( IntConstant.getInstance(3)));
    targets.add(new JNopStmt());
    IStmt stmt = new JTableSwitchStmt(IntConstant.getInstance(123),1, 4, targets, new JReturnStmt( IntConstant.getInstance(666)) );

    ArrayList<IStmt> targets2 = new ArrayList<>();
    targets.add(new JReturnStmt( IntConstant.getInstance(1)));
    targets.add(new JReturnStmt( IntConstant.getInstance(2)));
    targets.add(new JNopStmt());
    targets.add(new JReturnStmt( IntConstant.getInstance(3)));
    IStmt stmt2 = new JTableSwitchStmt(IntConstant.getInstance(123),1, 4, targets2, new JReturnStmt( IntConstant.getInstance(666)) );

    IStmt stmt3 = new JTableSwitchStmt(IntConstant.getInstance(456),1, 4, targets, new JReturnStmt( IntConstant.getInstance(666)) );
    IStmt stmt4 = new JTableSwitchStmt(IntConstant.getInstance(123),2, 4, targets, new JReturnStmt( IntConstant.getInstance(666)) );
    IStmt stmt5 = new JTableSwitchStmt(IntConstant.getInstance(123),1, 5, targets, new JReturnStmt( IntConstant.getInstance(666)) );
    IStmt stmt6 = new JTableSwitchStmt(IntConstant.getInstance(123),1, 4, targets, new JNopStmt() );


    // toString
    assertEquals("tableswitch(123) {     case 1: goto return 1;     case 2: goto return 2;     case 3: goto return 3;     case 4: goto nop;     default: goto return 666; }",
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
