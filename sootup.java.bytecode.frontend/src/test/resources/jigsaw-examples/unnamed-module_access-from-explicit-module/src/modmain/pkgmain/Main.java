package pkgmain;

/**
 * This class cannot be compiled in Eclipse as compiler option '--add-reads modmain=ALL-UNNAMED' is needed.
 * See compile.sh for details. 
 */
public class Main {
    public static void main(String[] args) {
        // ---------------------------------------------------------------
    	// accessing from modmain a class on the classpath, in cpb
        
    	Main.class.getModule().addReads(Main.class.getClassLoader().getUnnamedModule());	// needed for runtime access to the unnamed module (or alternatively run with launcher option '--add-reads modmain=ALL-UNNAMED')
    	System.out.println(new pkgboncp.BFromClasspath().doIt());   // Only compiles (outside Eclipse) with command line option --add-reads modmain=ALL-UNNAMED 
    	
        // ---------------------------------------------------------------
        // accessing from modmain a class on the module path, in module modb        
        System.out.println(new pkgb.B().doIt());					// The class is taken from modb, not from cpb!

        // ---------------------------------------------------------------
        // accessing from modmain some classes whose package is both on the classpath and on the module path in modb
        // Note that only the package contents in modb can be seen here!

        System.out.println(new pkgb.BFromModule().doIt());
        // System.out.println(new pkgb.BFromClasspath().doIt());    // Does not compile because pkgb in cpb is hidden by pkgb of modb (and the latter does not contain pkgb.BFromClasspath)
    }
}
