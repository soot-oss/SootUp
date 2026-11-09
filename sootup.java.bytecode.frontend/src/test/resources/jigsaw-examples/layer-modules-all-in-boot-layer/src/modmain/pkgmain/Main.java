package pkgmain;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import pkgcommon.IdGen;
import pkgcommon.LayerPrinter;

public class Main {
	private String id;

	public static void main(String[] args) throws Exception {
		new Main();
	}

	public Main() throws Exception {
                // we are using functionality from modcommon here
		id = IdGen.createID();

		// print the current (boot) layer's contents
		LayerPrinter.printRuntimeInfos(this.getClass().getModule().getLayer(), this);

		System.out.println("\nReflective call to pkgfoo.Foo in modfoo in current boot layer...");
		callFoo();
		System.out.println("\nReflective call to pkgbar.Bar in modbar in current boot layer...");
		callBar();
	}

	@Override
	public String toString() {
		return this.getClass().getName() + ", id=" + id;
	}

	// --------------------------------------------------------------------------------------------------------------

	private void callFoo() throws Exception {
		call("pkgfoo.Foo", "toString");
	}

	private void callBar() throws Exception {
		call("pkgbar.Bar", "toString");
	}

	// reflective call from the Main's layer to a class in the module modfoo or
	// modbar, respectively
	private void call(String clazzName, String methodName) throws Exception {
		Class<?> clazz = Class.forName(clazzName);

		Constructor<?> con = clazz.getDeclaredConstructor();
		con.setAccessible(true);
		Object o = con.newInstance();
		Method m = o.getClass().getMethod(methodName);
		m.setAccessible(true);
		System.out.println(m.invoke(o));
	}
}
