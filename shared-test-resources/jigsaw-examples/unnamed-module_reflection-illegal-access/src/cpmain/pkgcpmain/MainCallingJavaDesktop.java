package pkgcpmain;

import java.lang.reflect.Constructor;

/**
 * This class is on the classpath, i.e. in the unnamed module.
 */
public class MainCallingJavaDesktop {
	public static void main(String[] args) throws Exception {
		try {
			Class<?> clazz = Class.forName("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"); // from module java.desktop
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
