package pkgb;

import pkgc.*;

/**
 * This class cannot be compiled in Eclipse as compiler options --add-reads and --add-exports are needed.
 * See compile.sh for details.
 * 
 * But if compilation has taken place outside Eclipse with the script,
 * the modular JARs can be found in .../mlib
 * 
 * The Eclipse launch files takes the compiled code from there and can be run in Eclipse.
 */
public class B {
    public String doIt() {
        return "from B";
    }

    // Compiler and also Runtime option needed, see compile.sh and run.sh
    public C getMyC() {
        return new C();
    }
}
