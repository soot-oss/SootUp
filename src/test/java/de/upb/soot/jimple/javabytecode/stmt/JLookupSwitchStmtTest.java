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
public class JLookupSwitchStmtTest {

  @Test
  public void test() {

    ArrayList<IntConstant> lookupValues = new ArrayList<>();
    ArrayList<IStmt> targets = new ArrayList<>();

    IStmt stmt = new JLookupSwitchStmt(IntConstant.getInstance(42), lookupValues, targets, new JNopStmt());
    IStmt stmtDifferentKey= new JLookupSwitchStmt(IntConstant.getInstance(123), lookupValues, targets, new JNopStmt());
    IStmt stmtDifferentDefault = new JLookupSwitchStmt(IntConstant.getInstance(42), lookupValues, targets, new JReturnStmt(IntConstant.getInstance(42)));

    // toString
    assertEquals("lookupswitch(42) {     default: goto nop; }", stmt.toString());

    targets.add(new JReturnVoidStmt());
    targets.add(new JNopStmt());

    lookupValues.add(IntConstant.getInstance(42));
    lookupValues.add(IntConstant.getInstance(33102));

    IStmt stmtDifferentLookupAndTarget = new JLookupSwitchStmt(IntConstant.getInstance(123), lookupValues, targets, new JNopStmt());
    assertEquals("lookupswitch(123) {     case 42: goto return;     case 33102: goto nop;     default: goto nop; }",
            stmtDifferentLookupAndTarget.toString());



      // equivTo
    assertFalse(stmt.equivTo( this ));
    assertTrue(stmt.equivTo(stmt));
    assertFalse(stmt.equivTo(stmtDifferentLookupAndTarget));
    assertFalse( stmt.equivTo(stmtDifferentDefault));
    assertFalse( stmt.equivTo(stmtDifferentKey));

  }

}

