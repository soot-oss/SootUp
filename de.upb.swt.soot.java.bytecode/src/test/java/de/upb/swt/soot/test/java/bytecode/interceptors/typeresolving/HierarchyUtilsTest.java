package de.upb.swt.soot.test.java.bytecode.interceptors.typeresolving;

import org.junit.Test;

public class HierarchyUtilsTest {

    @Test
    public void test(){
        double b = 1.0;
        float f = 2;
        long l = 1;
        int i = 1;
        int s = 2;
        b = s;
        C c = new O();
        I io = new O();

    }

    public interface I{

    }

    public interface C extends I{

    }

    public class O implements I, C {

    }

}
