
package de.upb.soot.jimple.javabytecode.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.type.BooleanType;
import de.upb.soot.jimple.common.type.IntType;
/**
*
* @author Markus Schmidt & Linghui Luo
*
*/
@Category(Java8Test.class)
public class JExitMonitorStmtTest {

  @Test
  public void test() {
    PositionInfo nop=PositionInfo.createNoPositionInfo();
    Local sandman = new Local("sandman", IntType.getInstance());
    Local night = new Local("night", BooleanType.getInstance());
    Local light = new Local("light", BooleanType.getInstance());

    IStmt stmt = new JExitMonitorStmt(sandman,nop);
    IStmt nightStmt = new JExitMonitorStmt(night,nop);
    IStmt lightStmt = new JExitMonitorStmt(light,nop);

    // toString
    assertEquals("exitmonitor sandman", stmt.toString());

    // equivTo
    assertFalse(stmt.equivTo(sandman));

    assertTrue(stmt.equivTo(stmt));
    assertFalse(stmt.equivTo(nightStmt));
    assertFalse(stmt.equivTo(lightStmt));

    assertFalse(nightStmt.equivTo(stmt));
    assertTrue(nightStmt.equivTo(nightStmt));
    assertFalse(nightStmt.equivTo(lightStmt));

    assertFalse(lightStmt.equivTo(stmt));
    assertFalse(lightStmt.equivTo(nightStmt));
    assertTrue(lightStmt.equivTo(lightStmt));

  }

}
