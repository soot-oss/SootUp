
package de.upb.soot.jimple.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.upb.soot.jimple.basic.EquivTo;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.common.type.BooleanType;
import de.upb.soot.jimple.common.type.IntType;

import java.util.Comparator;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import soot.jimple.internal.JBreakpointStmt;

import categories.Java8Test;

@Category(Java8Test.class)
public class LocalTest {

  Comparator comparator = new Comparator<Object>() {
    @Override
    public int compare(Object o1, Object o2) {
      // don't compare the name of locals, in any other case use the shipped equivTo method
      if (o1 instanceof Local && o2 instanceof Local) {
        return (((Local) o1).getType().equals(((Local) o2).getType())) ? 0 : 1;
      } else if (o1 instanceof EquivTo) {
        // call the default comparator
        return ((EquivTo) o1).equivTo(o2) ? 0 : 1;
      }
      return -1;
    }
  };

  @Test
  public void testEquivTo() {

    Local l1 = new Local("$i1", IntType.getInstance());
    Local l2 = new Local("$i2", IntType.getInstance());
    Local l3 = new Local("$i1", BooleanType.getInstance());

    assertTrue(l1.equivTo(l1));
    assertTrue(l1.equivTo(l1, comparator));

    assertFalse(l1.equivTo(l2));
    assertTrue(l1.equivTo(l2, comparator));

    assertFalse(l1.equivTo(l3));
    assertFalse(l1.equivTo(l3, comparator));

    assertFalse(l2.equivTo(l3));
    assertFalse(l2.equivTo(l3, comparator));

    assertFalse(l1.equivTo(new JBreakpointStmt(), comparator));

  }
}
