package sootup.java.bytecode.frontend.interceptors.typeresolving;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sootup.core.types.*;
import sootup.core.util.ImmutableUtils;
import sootup.interceptors.typeresolving.PrimitiveHierarchy;
import sootup.interceptors.typeresolving.types.AugmentIntegerTypes;
import sootup.interceptors.typeresolving.types.BottomType;

/**
 * @author Zun Wang
 */
public class PrimitiveHierarchyTest {

  private Type bt = BottomType.getInstance();
  private Type bt2 = BottomType.getInstance();
  private Type boo = PrimitiveType.getBoolean();
  private Type i = PrimitiveType.getInt();
  private Type i1 = AugmentIntegerTypes.getInteger1();
  private Type i127 = AugmentIntegerTypes.getInteger127();
  private Type i32767 = AugmentIntegerTypes.getInteger32767();
  private Type by = PrimitiveType.getByte();
  private Type s = PrimitiveType.getShort();
  private Type c = PrimitiveType.getChar();
  private Type l = PrimitiveType.getLong();
  private Type f = PrimitiveType.getFloat();
  private Type d = PrimitiveType.getDouble();

  private Type arr_i = new ArrayType(i, 1);
  private Type arr_i2 = new ArrayType(i, 2);
  private Type arr_c = new ArrayType(c, 1);
  private Type arr_by = new ArrayType(by, 1);
  private Type arr_i127 = new ArrayType(i127, 1);

  @Test
  public void testIsAncestor() {

    // check all ancestor relationships in lattice
    assertTrue(PrimitiveHierarchy.isAncestor(bt, bt2));

    assertTrue(PrimitiveHierarchy.isAncestor(i1, bt));
    assertFalse(PrimitiveHierarchy.isAncestor(bt, i1));

    assertTrue(PrimitiveHierarchy.isAncestor(boo, i1));
    assertTrue(PrimitiveHierarchy.isAncestor(boo, bt));

    assertTrue(PrimitiveHierarchy.isAncestor(i127, i1));
    assertTrue(PrimitiveHierarchy.isAncestor(i127, bt));
    assertFalse(PrimitiveHierarchy.isAncestor(boo, i127));

    assertTrue(PrimitiveHierarchy.isAncestor(by, i127));
    assertFalse(PrimitiveHierarchy.isAncestor(by, i32767));

    assertTrue(PrimitiveHierarchy.isAncestor(c, i32767));
    assertFalse(PrimitiveHierarchy.isAncestor(c, by));
    assertFalse(PrimitiveHierarchy.isAncestor(c, s));

    assertTrue(PrimitiveHierarchy.isAncestor(s, i127));
    assertTrue(PrimitiveHierarchy.isAncestor(s, by));
    assertFalse(PrimitiveHierarchy.isAncestor(s, boo));

    assertTrue(PrimitiveHierarchy.isAncestor(i, c));
    assertTrue(PrimitiveHierarchy.isAncestor(i, s));
    assertTrue(PrimitiveHierarchy.isAncestor(i, i1));
    assertFalse(PrimitiveHierarchy.isAncestor(i, boo));

    assertFalse(PrimitiveHierarchy.isAncestor(d, i));
    assertFalse(PrimitiveHierarchy.isAncestor(d, f));
    assertFalse(PrimitiveHierarchy.isAncestor(l, c));

    assertFalse(PrimitiveHierarchy.isAncestor(arr_i, arr_i2));
    assertTrue(PrimitiveHierarchy.isAncestor(arr_i, arr_i127));
    assertTrue(PrimitiveHierarchy.isAncestor(arr_c, arr_i127));
    assertFalse(PrimitiveHierarchy.isAncestor(arr_i, arr_by));
  }

  @Test
  public void testLCA() {
    Set<Type> expect = ImmutableUtils.immutableSet(bt);
    Collection<Type> actual = PrimitiveHierarchy.getLeastCommonAncestor(bt, bt2);
    assertEquals(expect, actual);

    expect = ImmutableUtils.immutableSet(i);
    actual = PrimitiveHierarchy.getLeastCommonAncestor(c, s);
    assertEquals(expect, actual);

    actual = PrimitiveHierarchy.getLeastCommonAncestor(i1, i);
    assertEquals(expect, actual);

    expect = ImmutableUtils.immutableSet(s);
    actual = PrimitiveHierarchy.getLeastCommonAncestor(s, i32767);
    assertEquals(expect, actual);

    actual = PrimitiveHierarchy.getLeastCommonAncestor(by, s);
    assertEquals(expect, actual);

    actual = PrimitiveHierarchy.getLeastCommonAncestor(by, i32767);
    assertEquals(expect, actual);
  }
}
