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
  private PrimitiveHierarchy primitiveHierarchy = new PrimitiveHierarchy();

  @Test
  public void testIsAncestor() {

    // check all ancestor relationships in lattice
    Assert.assertTrue(primitiveHierarchy.isAncestor(bt, bt2));

    Assert.assertTrue(primitiveHierarchy.isAncestor(i1, bt));
    Assert.assertFalse(primitiveHierarchy.isAncestor(bt, i1));

    Assert.assertTrue(primitiveHierarchy.isAncestor(boo, i1));
    Assert.assertTrue(primitiveHierarchy.isAncestor(boo, bt));

    Assert.assertTrue(primitiveHierarchy.isAncestor(i127, i1));
    Assert.assertTrue(primitiveHierarchy.isAncestor(i127, bt));
    Assert.assertFalse(primitiveHierarchy.isAncestor(boo, i127));

    Assert.assertTrue(primitiveHierarchy.isAncestor(by, i127));
    Assert.assertFalse(primitiveHierarchy.isAncestor(by, i32767));

    Assert.assertTrue(primitiveHierarchy.isAncestor(c, i32767));
    Assert.assertFalse(primitiveHierarchy.isAncestor(c, by));
    Assert.assertFalse(primitiveHierarchy.isAncestor(c, s));

    Assert.assertTrue(primitiveHierarchy.isAncestor(s, i127));
    Assert.assertFalse(primitiveHierarchy.isAncestor(s, by));
    Assert.assertFalse(primitiveHierarchy.isAncestor(s, boo));

    Assert.assertTrue(primitiveHierarchy.isAncestor(i, c));
    Assert.assertTrue(primitiveHierarchy.isAncestor(i, s));
    Assert.assertTrue(primitiveHierarchy.isAncestor(i, i1));
    Assert.assertFalse(primitiveHierarchy.isAncestor(i, boo));

    Assert.assertFalse(primitiveHierarchy.isAncestor(d, i));
    Assert.assertFalse(primitiveHierarchy.isAncestor(d, f));
    Assert.assertFalse(primitiveHierarchy.isAncestor(l, c));

    Assert.assertFalse(primitiveHierarchy.isAncestor(arr_i, arr_i2));
    Assert.assertTrue(primitiveHierarchy.isAncestor(arr_i, arr_i127));
    Assert.assertTrue(primitiveHierarchy.isAncestor(arr_c, arr_i127));
    Assert.assertFalse(primitiveHierarchy.isAncestor(arr_i, arr_by));
  }

  @Test
  public void testLCA() {
    Set<Type> expect = ImmutableUtils.immutableSet(bt);
    Collection<Type> actual = primitiveHierarchy.getLeastCommonAncestor(bt, bt2);
    Assert.assertEquals(expect, actual);

    expect = ImmutableUtils.immutableSet(i);
    actual = primitiveHierarchy.getLeastCommonAncestor(c, s);
    Assert.assertEquals(expect, actual);

    actual = primitiveHierarchy.getLeastCommonAncestor(by, s);
    Assert.assertEquals(expect, actual);

    actual = primitiveHierarchy.getLeastCommonAncestor(by, i32767);
    Assert.assertEquals(expect, actual);

    actual = primitiveHierarchy.getLeastCommonAncestor(i1, i);
    Assert.assertEquals(expect, actual);

    expect = ImmutableUtils.immutableSet(s);
    actual = primitiveHierarchy.getLeastCommonAncestor(s, i32767);
    Assert.assertEquals(expect, actual);
  }
}
