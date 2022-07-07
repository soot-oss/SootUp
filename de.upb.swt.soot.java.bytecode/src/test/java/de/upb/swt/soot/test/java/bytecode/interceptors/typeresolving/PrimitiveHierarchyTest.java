package de.upb.swt.soot.test.java.bytecode.interceptors.typeresolving;

import categories.Java8Test;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.jimple.visitor.TypeVisitor;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.types.*;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.PrimitiveHierarchy;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.types.BottomType;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.types.WeakObjectType;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.views.JavaView;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.annotation.Nonnull;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

@Category(Java8Test.class)
public class PrimitiveHierarchyTest {

    private Type bt = BottomType.getInstance();
    private Type bt2 = BottomType.getInstance();
    private Type boo = PrimitiveType.getBoolean();
    private Type boo2 = PrimitiveType.getBoolean();
    private Type i = PrimitiveType.getInt();
    private Type i1 = PrimitiveType.getInteger1();
    private Type i127 = PrimitiveType.getInteger127();
    private Type i32767 = PrimitiveType.getInteger32767();
    private Type by = PrimitiveType.getByte();
    private Type s = PrimitiveType.getShort();
    private Type c = PrimitiveType.getChar();
    private Type l = PrimitiveType.getLong();
    private Type f = PrimitiveType.getFloat();
    private Type d = PrimitiveType.getDouble();
    private Type wot = new WeakObjectType("Object", new PackageName("java.lang"));

    private Type arr_i = new ArrayType(i, 1);
    private Type arr_i2 = new ArrayType(i, 1);
    private Type arr_c = new ArrayType(c, 1);
    private Type arr_s = new ArrayType(s, 1);
    private Type arr_by = new ArrayType(by, 1);
    private Type arr_i127 = new ArrayType(i127, 1);

    @Test
    public void testIsAncestor(){

        //check all ancestor relationships in lattice
        Assert.assertTrue(PrimitiveHierarchy.isAncestor(bt, bt2));
        Assert.assertTrue(PrimitiveHierarchy.isAncestor(boo2, boo));

        Assert.assertTrue(PrimitiveHierarchy.isAncestor(i1, bt));
        Assert.assertFalse(PrimitiveHierarchy.isAncestor(bt, i1));


        Assert.assertTrue(PrimitiveHierarchy.isAncestor(boo, i1));
        Assert.assertTrue(PrimitiveHierarchy.isAncestor(boo, bt));

        Assert.assertTrue(PrimitiveHierarchy.isAncestor(i127, i1));
        Assert.assertTrue(PrimitiveHierarchy.isAncestor(i127, bt));
        Assert.assertFalse(PrimitiveHierarchy.isAncestor(boo, i127));

        Assert.assertTrue(PrimitiveHierarchy.isAncestor(i32767, i1));
        Assert.assertTrue(PrimitiveHierarchy.isAncestor(by, i127));
        Assert.assertFalse(PrimitiveHierarchy.isAncestor(by, i32767));

        Assert.assertTrue(PrimitiveHierarchy.isAncestor(c, i32767));
        Assert.assertTrue(PrimitiveHierarchy.isAncestor(c, i1));
        Assert.assertFalse(PrimitiveHierarchy.isAncestor(c, by));
        Assert.assertFalse(PrimitiveHierarchy.isAncestor(c, s));

        Assert.assertTrue(PrimitiveHierarchy.isAncestor(s, i127));
        Assert.assertTrue(PrimitiveHierarchy.isAncestor(s, by));
        Assert.assertFalse(PrimitiveHierarchy.isAncestor(s, boo));

        Assert.assertTrue(PrimitiveHierarchy.isAncestor(i, c));
        Assert.assertTrue(PrimitiveHierarchy.isAncestor(i, s));
        Assert.assertTrue(PrimitiveHierarchy.isAncestor(i, i1));
        Assert.assertFalse(PrimitiveHierarchy.isAncestor(i, boo));

        Assert.assertFalse(PrimitiveHierarchy.isAncestor(wot, i));
        Assert.assertTrue(PrimitiveHierarchy.isAncestor(d, i));
        Assert.assertTrue(PrimitiveHierarchy.isAncestor(d, f));
        Assert.assertTrue(PrimitiveHierarchy.isAncestor(l, c));
        Assert.assertFalse(PrimitiveHierarchy.isAncestor(f, d));

        Assert.assertFalse(PrimitiveHierarchy.isAncestor(arr_i, i));
        Assert.assertTrue(PrimitiveHierarchy.isAncestor(arr_i, arr_i2));
        Assert.assertTrue(PrimitiveHierarchy.isAncestor(arr_i, arr_i127));
        Assert.assertTrue(PrimitiveHierarchy.isAncestor(arr_c, arr_i127));
        Assert.assertFalse(PrimitiveHierarchy.isAncestor(arr_s, arr_by));
        Assert.assertTrue(PrimitiveHierarchy.isAncestor(arr_s, arr_i127));
    }

    @Test
    public void testLCA(){
        Set<Type> expect = ImmutableUtils.immutableSet(bt);
        Collection<Type> actual = PrimitiveHierarchy.getLeastCommonAncestor(bt, bt2);
        Assert.assertEquals(expect, actual);

        expect = Collections.emptySet();
        actual = PrimitiveHierarchy.getLeastCommonAncestor(bt, wot);
        Assert.assertEquals(expect, actual);

        expect = ImmutableUtils.immutableSet(i);
        actual = PrimitiveHierarchy.getLeastCommonAncestor(c, s);
        Assert.assertEquals(expect, actual);

        actual = PrimitiveHierarchy.getLeastCommonAncestor(by, c);
        Assert.assertEquals(expect, actual);

        expect = ImmutableUtils.immutableSet(s);
        actual = PrimitiveHierarchy.getLeastCommonAncestor(by, i32767);
        Assert.assertEquals(expect, actual);

        expect = ImmutableUtils.immutableSet(d);
        actual = PrimitiveHierarchy.getLeastCommonAncestor(d, i1);
        Assert.assertEquals(expect, actual);
    }
}
