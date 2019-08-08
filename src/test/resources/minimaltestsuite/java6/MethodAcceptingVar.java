package de.upb.soot.java6;

public class MethodAcceptingVar {
    public void shortVariable(short a) {
        System.out.println(a);
        a++;
        System.out.println(a);
    }

    public void byteVariable(byte b) {
        System.out.println(b);
        b++;
        System.out.println(b);
    }

    public void charVariable(char c) {
        System.out.println(c);
        c = 'b';
        System.out.println(c);
    }

    public void intVariable(int d) {
        System.out.println(d);
        d++;
        System.out.println(d);
    }

    public void longVariable(long e) {
        System.out.println(e);
        e = 123456777;
        System.out.println(e);

    }

    public void floatVariable(float f) {
        System.out.println(f);
        f = 7.77f;
        System.out.println(f);
    }

    public void doubleVariable(double g) {
        System.out.println(g);
        g = 1.787777777;
        System.out.println(g);
    }

}
