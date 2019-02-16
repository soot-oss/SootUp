package de.upb.soot.jimple.javabytecode.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.common.stmt.IStmt;
/**
*
* @author Markus Schmidt & Linghui Luo
*
*/
@Category(Java8Test.class)
public class JBreakpointStmtTest {

  @Test
  public void test() {
    PositionInfo nop=PositionInfo.createNoPositionInfo();
    IStmt stmt = new JBreakpointStmt(nop);
    IStmt stmt2 = new JBreakpointStmt(nop);

    // toString
    assertEquals("breakpoint", stmt.toString());

    // equivTo
    assertTrue(stmt.equivTo(stmt2));

  }

}
