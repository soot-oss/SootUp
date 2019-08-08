package de.upb.soot.java6;

public class DoWhileLoop {

    public int doWhileLoop(int num) {
        int i = 0;
        do {
            i++;
        } while (num > i);
        return i;
    }
}
