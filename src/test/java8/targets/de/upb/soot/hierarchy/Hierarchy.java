package de.upb.soot.Hierarchy;

public class Hierarchy extends A {
    public static void hierarchy(String [] args) {
        Hierarchy h = new Hierarchy();
        h.methodB();
        System.out.println(h.methodA());
    }
}
