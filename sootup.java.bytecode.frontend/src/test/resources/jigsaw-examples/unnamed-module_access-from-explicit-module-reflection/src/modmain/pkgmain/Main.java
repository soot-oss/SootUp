package pkgmain;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * This class cannot be compiled in Eclipse as compiler option '--add-reads modmain=ALL-UNNAMED' is needed.
 * See compile.sh for details. 
 */
public class Main {
    public static void main(String[] args) throws Exception {
        // ---------------------------------------------------------------
    	// accessing from modmain a class on the classpath, in cpb, per reflection
        Class<?> myBFromClasspath = Class.forName("pkgboncp.BFromClasspath");
        Constructor<?> con = myBFromClasspath.getDeclaredConstructor();
        Object myBCp = con.newInstance();

        Method m = myBCp.getClass().getMethod("doIt");
        System.out.println("Main: " + m.invoke(myBCp));
        
        // ---------------------------------------------------------------
    	// accessing from modmain a class on the classpath, in cpb, per reflection
        // in a package that already exists in modb
        try {
        	Class.forName("pkgb.BFromClasspath");
        } catch (ClassNotFoundException ex) {
        	// pkgb.BFromClasspath cannot be read, as the package is hidden by modb
        	System.out.println("ClassNotFoundException: pkgb.BFromClasspath can't be found, as it is hidden by pkgb in modb!");
        }
        
        // ---------------------------------------------------------------
    	// accessing pkgb.B from modmain per reflection. The class from the module is found, not the class on the claspath
        Class<?> mayB = Class.forName("pkgb.B");
        Constructor<?> conB = mayB.getDeclaredConstructor();
        Object mayBObj = conB.newInstance();

        Method mB = mayBObj.getClass().getMethod("doIt");
        System.out.println("Main: " + mB.invoke(mayBObj));
    }
}
