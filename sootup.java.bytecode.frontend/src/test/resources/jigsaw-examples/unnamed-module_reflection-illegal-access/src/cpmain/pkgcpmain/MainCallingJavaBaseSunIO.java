package pkgcpmain;

import java.lang.reflect.Constructor;

/**
 * This class is on the classpath, i.e. in the unnamed module.
 */
public class MainCallingJavaBaseSunIO {
    public static void main(String[] args) throws Exception {
    	try {
	        Class<?> clazz = Class.forName("sun.io.Win32ErrorMode");				// from module java.base, but other package than jdk.internal.*
	        Constructor<?> con = clazz.getDeclaredConstructor();
	        con.setAccessible(true);
	        Object o = con.newInstance();
	        System.out.println(o.toString());
    	}
    	catch (Throwable t) {
    		t.printStackTrace(System.out);
    	}
    }
}
