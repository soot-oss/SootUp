package pkgmain;

import pkgb.B;

/**
 * This class cannot be compiled in Eclipse as compiler options --add-reads and --add-exports are needed.
 * See compile.sh for details.
 * 
 * But if compilation has taken place outside Eclipse with the script,
 * the modular JARs can be found in .../mlib
 * 
 * The Eclipse launch files takes the compiled code from there and can be run in Eclipse.
 */
public class Main {
    public static void main(String[] args) {
        Main mymain = new Main();
        
        // Compiler and also Runtime option needed, see compile.sh and run.sh
        B myb = new B();

        Object myc = myb.getMyC();
        System.out.println("Main: " + mymain.toString() + ", B: " + myb.doIt() + ", C: " + myc.toString());

        // does not compile, as type C not visible here
        // C myc = myb.getMyC();
    }
}
