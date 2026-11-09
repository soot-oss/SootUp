package pkgmain;

import jdk.internal.misc.SharedSecrets;

/**
 * This class cannot be compiled in Eclipse as compiler options --add-exports are needed.
 * See compile.sh for details.
 * 
 * But if compilation has taken place outside Eclipse with the script,
 * the modular JARs can be found in .../mlib
 * 
 * The Eclipse launch files takes the compiled code from there and can be run in Eclipse.
 */

public class Main {
    public static void main(String[] args) {
    	// Compiler and also Runtime option needed: --add-exports java.base/jdk.internal.misc=modmain
    	SharedSecrets secrets = new SharedSecrets();
        System.out.println("Do you want to know a secret: " + secrets);

    	// Compiler and also Runtime option needed: --add-exports moda/pkgainternal=modmain
        System.out.println(new pkgainternal.A().doIt());
    }
}
