package de.upb.soot.jimple.javabytecode.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import de.upb.soot.jimple.common.stmt.IStmt;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

@Category(Java8Test.class)
public class JBreakpointStmtTest {

  @Test
  public void test() {

    IStmt stmt = new JBreakpointStmt();
    IStmt stmt2 = new JBreakpointStmt();

    // toString
    assertEquals("breakpoint", stmt.toString());

    // equivTo
    assertTrue(stmt.equivTo(stmt2));

  }

}
