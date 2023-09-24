package sootup.java.bytecode.interceptors.typeresolving;

import categories.Java8Test;
import java.util.Collection;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.types.*;
import sootup.core.util.ImmutableUtils;
import sootup.java.bytecode.interceptors.typeresolving.types.AugIntegerTypes;
import sootup.java.bytecode.interceptors.typeresolving.types.BottomType;

/** @author Zun Wang */
@Category(Java8Test.class)
public class PrimitiveHierarchyTest {

  private Type bt = BottomType.getInstance();
  private Type bt2 = BottomType.getInstance();
  private Type boo = PrimitiveType.getBoolean();
  private Type i = PrimitiveType.getInt();
  private Type i1 = AugIntegerTypes.getInteger1();
  private Type i127 = AugIntegerTypes.getInteger127();
  private Type i32767 = AugIntegerTypes.getInteger32767();
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
    Assert.assertTrue(PrimitiveHierarchy.isAncestor(bt, bt2));

    Assert.assertTrue(PrimitiveHierarchy.isAncestor(i1, bt));
    Assert.assertFalse(PrimitiveHierarchy.isAncestor(bt, i1));

    Assert.assertTrue(PrimitiveHierarchy.isAncestor(boo, i1));
    Assert.assertTrue(PrimitiveHierarchy.isAncestor(boo, bt));

    Assert.assertTrue(PrimitiveHierarchy.isAncestor(i127, i1));
    Assert.assertTrue(PrimitiveHierarchy.isAncestor(i127, bt));
    Assert.assertFalse(PrimitiveHierarchy.isAncestor(boo, i127));

    Assert.assertTrue(PrimitiveHierarchy.isAncestor(by, i127));
    Assert.assertFalse(PrimitiveHierarchy.isAncestor(by, i32767));

    Assert.assertTrue(PrimitiveHierarchy.isAncestor(c, i32767));
    Assert.assertFalse(PrimitiveHierarchy.isAncestor(c, by));
    Assert.assertFalse(PrimitiveHierarchy.isAncestor(c, s));

    Assert.assertTrue(PrimitiveHierarchy.isAncestor(s, i127));
    Assert.assertFalse(PrimitiveHierarchy.isAncestor(s, by));
    Assert.assertFalse(PrimitiveHierarchy.isAncestor(s, boo));

    Assert.assertTrue(PrimitiveHierarchy.isAncestor(i, c));
    Assert.assertTrue(PrimitiveHierarchy.isAncestor(i, s));
    Assert.assertTrue(PrimitiveHierarchy.isAncestor(i, i1));
    Assert.assertFalse(PrimitiveHierarchy.isAncestor(i, boo));

    Assert.assertFalse(PrimitiveHierarchy.isAncestor(d, i));
    Assert.assertFalse(PrimitiveHierarchy.isAncestor(d, f));
    Assert.assertFalse(PrimitiveHierarchy.isAncestor(l, c));

    Assert.assertFalse(PrimitiveHierarchy.isAncestor(arr_i, arr_i2));
    Assert.assertTrue(PrimitiveHierarchy.isAncestor(arr_i, arr_i127));
    Assert.assertTrue(PrimitiveHierarchy.isAncestor(arr_c, arr_i127));
    Assert.assertFalse(PrimitiveHierarchy.isAncestor(arr_i, arr_by));
  }

  @Test
  public void testLCA() {
    Set<Type> expect = ImmutableUtils.immutableSet(bt);
    Collection<Type> actual = PrimitiveHierarchy.getLeastCommonAncestor(bt, bt2);
    Assert.assertEquals(expect, actual);

    expect = ImmutableUtils.immutableSet(i);
    actual = PrimitiveHierarchy.getLeastCommonAncestor(c, s);
    Assert.assertEquals(expect, actual);

    actual = PrimitiveHierarchy.getLeastCommonAncestor(by, s);
    Assert.assertEquals(expect, actual);

    actual = PrimitiveHierarchy.getLeastCommonAncestor(by, i32767);
    Assert.assertEquals(expect, actual);

    actual = PrimitiveHierarchy.getLeastCommonAncestor(i1, i);
    Assert.assertEquals(expect, actual);

    expect = ImmutableUtils.immutableSet(s);
    actual = PrimitiveHierarchy.getLeastCommonAncestor(s, i32767);
    Assert.assertEquals(expect, actual);
  }
}
