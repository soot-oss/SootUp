package pkgmain;

import pkgb1.B1;
import pkgb2.B2;

public class Main {
    public static void main(String[] args) {
        B1 myb1 = new B1();		
        B2 myb2 = new B2();

        System.out.println("B1: " + myb1.doIt() + ", B2: " + myb2.doIt());
    }
}
