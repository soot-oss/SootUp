package de.upb.swt.soot.test.java.bytecode.interceptors.typeresolving;

import org.junit.Test;

import java.io.*;

public class HierarchyUtilsTest {

    @Test
    public void test(){

        //test isSubType-ArrayType part
        I[][] iiarr = new O[10][10];
        Object[] obarr = new Object[0][0];

        obarr = new double[1][1][1][1];
        obarr = new Serializable[1][1][1];


        Cloneable[][] ssarr = new Serializable[1][1][1];


        double b = 1.0;
        float f = 2;
        long l = 1;
        int i = 1;
        int s = 2;
        b = s;
        C c = new O();
        I io = new O();
        E e = E.A;
        e = null;
        O[] oa = new O[10];
        double[][] daa = new double[10][10];
        double[] da = new double[10];


        b = i;

    }

    public interface I{

    }

    public interface C extends I{

    }

    public class O implements I, C {

    }


    public enum E{
        A, B;
    }


}
