package pkgmain;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

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
		LayerPrinter.printRuntimeInfos(this.getClass().getModule().getLayer());

		// create a first child layer of the boot layer, containing modfoo and modversion1
		System.out.println("\nCreate a new 'foo layer #1' as a child of the current layer...");
		ModuleLayer layer = createFooLayer();
		// ... and then do a reflective call to a class in modfoo in this new layer
		System.out.println("Reflective call to pkgfoo.Foo in modfoo in 'foo layer #1'...");
		callFoo(layer);

		// create a second child layer of the boot layer, containing modbar and modversion2
		System.out.println("\nCreate a new 'bar layer #1' as a child of the current layer...");
		layer = createBarLayer(false);
		// ... and then do a reflective call to a class in modbar in this new layer
		System.out.println("Reflective call to pkgbar.Bar in modbar in 'bar layer #1'...");
		callBar(layer);

		// create a third child layer of the boot layer, containing again modbar and modversion2
		// this time, we look up the module path barmlib before looking into the parent configuration
		System.out.println("\nCreate a new 'bar layer #2' as a child of the current layer...");
		layer = createBarLayer(true);
		// ... and then do a reflective call to a class in modbar in this new layer
		System.out.println("Reflective call to pkgbar.Bar in modbar in 'bar layer #2'...");
		callBar(layer);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + ", id=" + id;
	}

	// --------------------------------------------------------------------------------------------------------------

	private void callFoo(ModuleLayer fooLayer) throws Exception {
		call(fooLayer, "modfoo", "pkgfoo.Foo", "toString");
	}

	private void callBar(ModuleLayer barLayer) throws Exception {
		call(barLayer, "modbar", "pkgbar.Bar", "toString");
	}

	// reflective call from the Main's layer to a class in the module modfoo or
	// modbar, respectively - in another(!) layer
	private void call(ModuleLayer layer, String modName, String clazzName, String methodName) throws Exception {
		Class<?> clazz = layer.findLoader(modName).loadClass(clazzName);

		Constructor<?> con = clazz.getDeclaredConstructor();
		con.setAccessible(true);
		Object o = con.newInstance();
		Method m = o.getClass().getMethod(methodName);
		m.setAccessible(true);
		System.out.println(m.invoke(o));
	}

	// --------------------------------------------------------------------------------------------------------------

	private ModuleLayer createFooLayer() {
		return createLayer(Main.class.getModule().getLayer(), Main.class.getModule().getClassLoader(), "modfoo",
				"./foomlib");
	}

	private ModuleLayer createBarLayer(boolean before) {
		return createLayer(Main.class.getModule().getLayer(), Main.class.getModule().getClassLoader(), "modbar",
				"./barmlib", before);
	}

	private ModuleLayer createLayer(final ModuleLayer parentLayer, ClassLoader classLoader, String modName,
			String amlib) {
		return createLayer(parentLayer, classLoader, modName, amlib, false);
	}
	// create a new layer and add the given module modfoo or modbar, respectively
	private ModuleLayer createLayer(final ModuleLayer parentLayer, ClassLoader classLoader, String modName,
			String amlib, boolean before) {
		String amlibPath = new java.io.File(System.getProperty("user.dir")).getAbsolutePath() + amlib;
		ModuleFinder moduleFinder = ModuleFinder.of(Paths.get(amlibPath).toAbsolutePath().normalize());
		Set<ModuleReference> allModules = moduleFinder.findAll();
		Set<String> allModuleNames = new HashSet<>();
		allModules.stream().map(modRef -> modRef.descriptor().name()).filter(name -> modName.contains(modName))
				.forEach(name -> allModuleNames.add(modName));
		if (allModuleNames.isEmpty()) {
			throw new RuntimeException(
					"No observable module " + modName + " found on the module paths. Terminating ...");
		}

		// Create configuration
		Configuration cf = parentLayer.configuration().resolve(before ? moduleFinder : ModuleFinder.of(),
				before ? ModuleFinder.of() : moduleFinder, allModuleNames);

		// ... and create a new Jigsaw Layer with this configuration
		ModuleLayer newLayer = parentLayer.defineModulesWithOneLoader(cf, classLoader);
		return newLayer;
	}
}
