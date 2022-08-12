package de.upb.swt.soot.test.java.bytecode.interceptors.typeresolving;

import org.junit.Test;

import java.awt.*;
import java.io.*;
import java.util.HashSet;
import java.util.Hashtable;

public class HierarchyUtilsTest {

    @Test
    public void test(){

        //test isSubType-ArrayType par

        Object object = new Object();
        Serializable serializable = new String();
        Cloneable cloneable = new HashSet<>();
        double d = 1.0;
        float f = 2;
        long l = 1;
        int i = 1;
        short s = 2;
        char c = 'a';
        boolean boo = true;
        Boolean BOO = true;
        boo = BOO;
        BOO = BOO;
        byte by = 1;
        double[] darr = new double[10];
        Double[] Darr = new Double[10];
        serializable = darr;
        cloneable = darr;
        d = c;
        object = c;
        serializable = d;
        serializable = f;
        serializable = l;
        serializable = i;
        serializable = s;
        serializable = c;
        serializable = boo;
        serializable = by;
        serializable = darr;
        d = new Double(1.0);
        Double D = new Double(1.0);
        D = null;




        object = d;
        object = f;
        object = l;
        object = i;
        object = s;
        object = c;
        object = boo;
        object = by;

        C1 c1 = new C1();
        C2 c2 = new C2();
        C3 c3 = new C3();
        I1 i1 = c1;
        I2 i2 = c2;
        I3 i3 = c2;
        i1 = c2;
        i2 = c2;
        c1 = c2;
        i3 = c3;
        C4 c4 = new C4();
        C1[] c1ar = new C1[10];
        C2[] c2arr = new C2[100];
        c1ar = c2arr;
        C1[][] c1arr = new C1[10][10];
        Cloneable[] cloneables = c1arr;
        object = serializable;





    }

    public interface I1{
        public void m1();
    }

    public interface I2 extends I1{
        public void m2();
    }

    public interface I3 {
        public void m3();
    }

    public class C1 implements I1, I2{
        public void m2(){}

        @Override
        public void m1() {}
    }

    public class C2 extends C1 implements I3{

        @Override
        public void m3() {}
    }

    public class C3 extends C2{
    }

    public class C4{
    }

    public enum E{
        A, B;
    }


}
