package pkgcpmain;

/**
 * This class is on the classpath, i.e. in the unnamed module.
 */

/**
 * This class cannot be compiled in Eclipse because of missing dependencies, might be an Eclipse problem
 * as cpmain cannot access modb.
 */
public class Main {
    public static void main(String[] args) {

        System.out.println("We are calling various classes from " + Main.class + " (which is on the classpath, i.e. in the unnamed module)");

        // ----------------------------------------------------------------------------------------------------------------------------------

        // We access a class on the module path, in module modb - class's package is exported, should work
        System.out.println("\n1. Classpath code calling code in an explicit module on the module path");
        System.out.println("a. ... calling an exported class B which is in module modb: " + new pkgb.BFromModule().doIt());

        // ----------------------------------------------------------------------------------------------------------------------------------

        // We access a class on the module path, in module modb - class's package is not exported, results in an java.lang.IllegalAccessError
        try {
            System.out.println("b. ... calling an internal, non-exported class which is in module modb - "
            		+ "results in a java.lang.IllegalAccessError:");
            System.out.println(new pkgbinternal.BFromModuleButInternal().doIt());
        }
        catch (Throwable ex) {
            ex.printStackTrace(System.err);     // we expect a java.lang.IllegalAccessError
        }

        // ----------------------------------------------------------------------------------------------------------------------------------

    	// We access a class which is only on the classpath, should always work
        System.out.println("\n2. Classpath code calling a class which is also on the classpath");
        System.out.println("... calling BFromClasspath which is on the classpath: " + new pkgboncp.BFromClasspath().doIt());

        // ----------------------------------------------------------------------------------------------------------------------------------

    	// We access a class which is on the classpath and whose package is also in modb, results in a java.lang.ClassNotFoundException
        try {
             System.out.println("\n3. Classpath code calling a class which is on the classpath, "
             		+ "but whose package name is 'covered' by a package in a module on the module path - "
             		+ "results in a java.lang.ClassNotFoundException");

    	     // We access a class which is on the classpath - will not work, but only because the package name "pkgb" is covered by the same class in the same package in modb
          	 pkgb.BFromClasspath myB4 = new pkgb.BFromClasspath();
             System.out.println("ERROR: Calling BFromClasspath whose package is both on the module path and on the classpath - SHOULD NOT WORK: " + myB4.doIt());
        }
        catch (Throwable ex) {
            ex.printStackTrace(System.err);     // we expect a java.lang.ClassNotFoundException
        }

        // ----------------------------------------------------------------------------------------------------------------------------------
                
    	// We access a class which is both on the classpath and in modb, will use the class in modb
        System.out.println("\n4. Classpath code calling a class which is both on the classpath and in a module on the module path - will use the latter from module modb:");

        // We access a class which is on the classpath - will not work, but only because the package name "pkgb" is covered by the same class in the same package in modb
        System.out.println("... calling B which is both on the module path and on the classpath: " + new pkgb.B().doIt());
    }
}
