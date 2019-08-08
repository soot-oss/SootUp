package de.upb.soot.java6;

public class VariableDecleration {
    //short/byte/char/int/long/float/double

    public void shortVariable() {
        short a = 10;
        System.out.println(a);
        a++;
        System.out.println(a);
    }

    public void byteVariable() {
        byte b = 0;
        System.out.println(b);
        b++;
        System.out.println(b);
    }

    public void charVariable() {
        char c = 'a';
        System.out.println(c);
        c = 'b';
        System.out.println(c);
    }

    public void intVariable() {
        int d = 512;
        System.out.println(d);
        d++;
        System.out.println(d);
    }

    public void longVariable() {
        long e = 123456789;
        System.out.println(e);
        e = 123456777;
        System.out.println(e);

    }

    public void floatVariable() {
        float f = 3.14f;
        System.out.println(f);
        f = 7.77f;
        System.out.println(f);
    }

    public void doubleVariable() {
        double g = 1.96969654d;
        System.out.println(g);
        g = 1.787777777;
        System.out.println(g);
    }

}
