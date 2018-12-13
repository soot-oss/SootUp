
package de.upb.soot.jimple.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.upb.soot.jimple.IgnoreLocalNameComparator;
import de.upb.soot.jimple.basic.JimpleComparator;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.common.type.BooleanType;
import de.upb.soot.jimple.common.type.IntType;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import soot.jimple.internal.JBreakpointStmt;

import categories.Java8Test;

@Category(Java8Test.class)
public class LocalTest {

  @Test
  public void testEquivTo() {

    JimpleComparator comparator = new IgnoreLocalNameComparator();

    Local l1 = new Local("$i1", IntType.getInstance());
    Local l2 = new Local("$i2", IntType.getInstance());
    Local l3 = new Local("$i1", BooleanType.getInstance());

    assertTrue(l1.equivTo(l1));
    assertTrue(l1.equivTo(l1, comparator ));

    assertFalse(l1.equivTo(l2));
    assertTrue(l1.equivTo(l2, comparator ));

    assertFalse(l1.equivTo(l3));
    assertFalse(l1.equivTo(l3, comparator ));

    assertFalse(l2.equivTo(l3));
    assertFalse(l2.equivTo(l3, comparator ));

    assertFalse(l1.equivTo(new JBreakpointStmt(), comparator));

  }
}
