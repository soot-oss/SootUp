package pkgmain;

import pkgb.B;

public class Main {
    public static void main(String[] args) {
        Main mymain = new Main();		
        B myb = new B();

        // Compiles, even though type C not visible here - note that modc is not required from modmain (and modb1/2 do not "required transitive" modc)
        // Using type C as Object is working, though - only type C is not visible.
        Object myc = myb.getMyC();
        System.out.println("Main: " + mymain.toString() + ", B: " + myb.doIt() + ", C: " + myc.toString());

        // Does not compile, as type C not visible here
        // C myc1 = myb.getMyC(); 
        // System.out.println("Main: " + mymain.toString() + ", B: " + myb.doIt() + ", C: " + myc1.doIt());
    }
}
