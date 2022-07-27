package de.upb.swt.soot.test.java.bytecode.interceptors.typeresolving;

import org.junit.Test;

import java.io.*;
import java.util.HashSet;

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
        byte by = 1;

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

        object = d;
        object = f;
        object = l;
        object = i;
        object = s;
        object = c;
        object = boo;
        object = by;

        O o = new O();
        object = o;

        Double[] D = new Double[10];
        double[] dd = new double[10];

        Byte bzte = by;
        by = bzte;
        Character character = c;

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
