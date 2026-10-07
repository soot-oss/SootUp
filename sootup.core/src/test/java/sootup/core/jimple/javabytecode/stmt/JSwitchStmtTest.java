package sootup.core.jimple.javabytecode.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.SimpleStmtPositionInfo;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.types.PrimitiveType;

class JSwitchStmtTest {

  /**
   * Reproduces loss of isTableSwitch property during AST cloning (withKey / withPositionInfo).
   *
   * <p>Subject pattern: Any Java method containing a dense switch statement compiled as a
   * tableswitch opcode: switch (x) { case 0: return "A"; case 1: return "B"; case 2: return "C"; }
   *
   * <p>When an optimization or interceptor pass transforms the key (e.g. constant propagation or
   * local remapping) or updates statement positions using `switchStmt.withKey(...)` or
   * `switchStmt.withPositionInfo(...)`, the previous implementation called the List constructor,
   * silently flipping isTableSwitch from true to false (degrading it into lookupswitch).
   */
  @Test
  void testTableSwitchPreservedOnWithKeyAndWithPositionInfo() {
    Local key = Jimple.newLocal("k", PrimitiveType.getInt());
    Local newKey = Jimple.newLocal("k2", PrimitiveType.getInt());
    StmtPositionInfo pos1 = StmtPositionInfo.getNoStmtPositionInfo();
    StmtPositionInfo pos2 = new SimpleStmtPositionInfo(42);

    JSwitchStmt tableSwitch = new JSwitchStmt(key, 0, 5, pos1);
    assertTrue(tableSwitch.isTableSwitch());
    assertEquals(6, tableSwitch.getValues().size());

    JSwitchStmt withKey = tableSwitch.withKey(newKey);
    assertTrue(withKey.isTableSwitch());
    assertEquals(newKey, withKey.getKey());
    assertEquals(0, withKey.getValue(0));
    assertEquals(5, withKey.getValue(5));
    assertEquals(pos1, withKey.getPositionInfo());

    JSwitchStmt withPos = tableSwitch.withPositionInfo(pos2);
    assertTrue(withPos.isTableSwitch());
    assertEquals(key, withPos.getKey());
    assertEquals(0, withPos.getValue(0));
    assertEquals(5, withPos.getValue(5));
    assertEquals(pos2, withPos.getPositionInfo());
  }

  @Test
  void testLookupSwitchPreservedOnWithKeyAndWithPositionInfo() {
    Local key = Jimple.newLocal("k", PrimitiveType.getInt());
    Local newKey = Jimple.newLocal("k2", PrimitiveType.getInt());
    StmtPositionInfo pos1 = StmtPositionInfo.getNoStmtPositionInfo();
    StmtPositionInfo pos2 = new SimpleStmtPositionInfo(42);
    List<IntConstant> values =
        Arrays.asList(IntConstant.getInstance(1), IntConstant.getInstance(10));

    JSwitchStmt lookupSwitch = new JSwitchStmt(key, values, pos1);
    assertFalse(lookupSwitch.isTableSwitch());
    assertEquals(2, lookupSwitch.getValues().size());

    JSwitchStmt withKey = lookupSwitch.withKey(newKey);
    assertFalse(withKey.isTableSwitch());
    assertEquals(newKey, withKey.getKey());
    assertEquals(values, withKey.getValues());

    JSwitchStmt withPos = lookupSwitch.withPositionInfo(pos2);
    assertFalse(withPos.isTableSwitch());
    assertEquals(key, withPos.getKey());
    assertEquals(values, withPos.getValues());
    assertEquals(pos2, withPos.getPositionInfo());
  }
}
