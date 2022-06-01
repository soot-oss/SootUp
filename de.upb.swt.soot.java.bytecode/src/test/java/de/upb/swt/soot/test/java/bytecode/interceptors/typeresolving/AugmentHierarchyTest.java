package de.upb.swt.soot.test.java.bytecode.interceptors.typeresolving;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.types.*;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.AugmentHierarchy;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.types.BottomType;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.types.WeakObjectType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Category(Java8Test.class)
public class AugmentHierarchyTest {

    Type bt = BottomType.getInstance();
    Type bt2 = BottomType.getInstance();
    Type wot = new WeakObjectType("Object", new PackageName("java.lang"));
    Type boo = PrimitiveType.getBoolean();
    Type i = PrimitiveType.getInt();
    Type i1 = PrimitiveType.getInteger1();
    Type i127 = PrimitiveType.getInteger127();
    Type i32767 = PrimitiveType.getInteger32767();
    Type by = PrimitiveType.getByte();
    Type s = PrimitiveType.getShort();
    Type c = PrimitiveType.getChar();
    Type arr_i = new ArrayType(i, 1);
    Type arr_i2 = new ArrayType(i, 1);
    Type arr_c = new ArrayType(c, 1);
    Type arr_s = new ArrayType(s, 1);
    Type arr_by = new ArrayType(by, 1);
    Type arr_i127 = new ArrayType(i127, 1);

    @Test
    public void testIsAncestor(){

        Assert.assertTrue(AugmentHierarchy.isAncestor(bt, bt2));
        Assert.assertTrue(AugmentHierarchy.isAncestor(bt2, bt));

        Assert.assertTrue(AugmentHierarchy.isAncestor(i1, bt));
        Assert.assertFalse(AugmentHierarchy.isAncestor(bt, i1));

        Assert.assertTrue(AugmentHierarchy.isAncestor(boo, i1));
        Assert.assertTrue(AugmentHierarchy.isAncestor(boo, bt));

        Assert.assertTrue(AugmentHierarchy.isAncestor(i127, i1));
        Assert.assertTrue(AugmentHierarchy.isAncestor(i127, bt));
        Assert.assertFalse(AugmentHierarchy.isAncestor(boo, i127));

        Assert.assertTrue(AugmentHierarchy.isAncestor(i32767, i1));
        Assert.assertTrue(AugmentHierarchy.isAncestor(by, i127));
        Assert.assertFalse(AugmentHierarchy.isAncestor(by, i32767));

        Assert.assertTrue(AugmentHierarchy.isAncestor(c, i32767));
        Assert.assertTrue(AugmentHierarchy.isAncestor(c, i1));
        Assert.assertFalse(AugmentHierarchy.isAncestor(c, by));
        Assert.assertFalse(AugmentHierarchy.isAncestor(c, s));

        Assert.assertTrue(AugmentHierarchy.isAncestor(s, i127));
        Assert.assertTrue(AugmentHierarchy.isAncestor(s, by));
        Assert.assertFalse(AugmentHierarchy.isAncestor(s, boo));

        Assert.assertTrue(AugmentHierarchy.isAncestor(i, c));
        Assert.assertTrue(AugmentHierarchy.isAncestor(i, s));
        Assert.assertTrue(AugmentHierarchy.isAncestor(i, i1));
        Assert.assertFalse(AugmentHierarchy.isAncestor(i, boo));

        Assert.assertFalse(AugmentHierarchy.isAncestor(arr_i, i));

        Assert.assertTrue(AugmentHierarchy.isAncestor(arr_i, arr_i2));
        Assert.assertTrue(AugmentHierarchy.isAncestor(arr_i, arr_i127));
        Assert.assertTrue(AugmentHierarchy.isAncestor(arr_c, arr_i127));
        Assert.assertFalse(AugmentHierarchy.isAncestor(arr_s, arr_by));
        Assert.assertTrue(AugmentHierarchy.isAncestor(arr_s, arr_i127));
    }

    @Test
    public void testLCA(){
        Set<Type> expect = ImmutableUtils.immutableSet(bt);
        Collection<Type> actual = AugmentHierarchy.getLeastCommonAncestor(bt, bt2, false);
        Assert.assertEquals(expect, actual);

        expect = ImmutableUtils.immutableSet(wot);
        actual = AugmentHierarchy.getLeastCommonAncestor(bt, wot, false);
        Assert.assertEquals(expect, actual);

        expect = ImmutableUtils.immutableSet(i);
        actual = AugmentHierarchy.getLeastCommonAncestor(i, wot, false);
        Assert.assertEquals(expect, actual);

        expect = Collections.emptySet();
        actual = AugmentHierarchy.getLeastCommonAncestor(i, arr_i, false);
        Assert.assertEquals(expect, actual);

        expect = ImmutableUtils.immutableSet(i);
        actual = AugmentHierarchy.getLeastCommonAncestor(c, s, false);
        Assert.assertEquals(expect, actual);

        expect = Collections.emptySet();
        actual = AugmentHierarchy.getLeastCommonAncestor(boo, s, false);
        Assert.assertEquals(expect, actual);

        expect = ImmutableUtils.immutableSet(boo);
        actual = AugmentHierarchy.getLeastCommonAncestor(boo, bt, false);
        Assert.assertEquals(expect, actual);

        expect = ImmutableUtils.immutableSet(s);
        actual = AugmentHierarchy.getLeastCommonAncestor(s, by, false);
        Assert.assertEquals(expect, actual);
    }
}
