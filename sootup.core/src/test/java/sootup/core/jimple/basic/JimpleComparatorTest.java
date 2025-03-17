package sootup.core.jimple.basic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.types.PrimitiveType;

public class JimpleComparatorTest {

  @Test
  public void test() {
    // l1 and l2 are equal, l1 and l3 are not
    Local l1 = Jimple.newLocal("l", PrimitiveType.getInt());
    Local l2 = Jimple.newLocal("l", PrimitiveType.getInt());
    Local l3 = Jimple.newLocal("l", PrimitiveType.getBoolean());

    List<IntConstant> lookup1 = new ArrayList<>();
    lookup1.add(IntConstant.getInstance(3));
    lookup1.add(IntConstant.getInstance(5));
    lookup1.add(IntConstant.getInstance(999));

    List<IntConstant> lookup2 = new ArrayList<>();
    lookup2.add(IntConstant.getInstance(3));
    lookup2.add(IntConstant.getInstance(5));
    lookup2.add(IntConstant.getInstance(999));

    JSwitchStmt switch1 = Jimple.newLookupSwitchStmt(l1, lookup1, StmtPositionInfo.NOPOSITION);
    JSwitchStmt switch2 = Jimple.newLookupSwitchStmt(l2, lookup2, StmtPositionInfo.NOPOSITION);
    JSwitchStmt switch3 = Jimple.newLookupSwitchStmt(l3, lookup2, StmtPositionInfo.NOPOSITION);

    assertTrue(switch1.equivTo(switch2));
    assertFalse(switch1.equivTo(switch3));
  }
}
