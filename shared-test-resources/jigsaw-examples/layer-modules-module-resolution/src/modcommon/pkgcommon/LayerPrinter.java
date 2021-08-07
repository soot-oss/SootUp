package pkgcommon;

import java.lang.module.ResolvedModule;
import java.util.List;
import java.util.stream.Collectors;

public final class LayerPrinter {
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_RED = "\u001B[31m";

	public static void printRuntimeInfos(ModuleLayer layer) throws Exception {
		System.out.println("Infos for Layer and Module:");

		System.out.println("Layer (" + printId(layer)
				+ (layer.equals(ModuleLayer.boot()) ? "), boot layer" : "), not boot layer"));
		System.out.print("Layer's parents: ");
		List<ModuleLayer> parents = layer.parents();
		if (parents.isEmpty() || (parents.size() == 1 && parents.contains(ModuleLayer.empty()))) {
			System.out.println("none, as this is the boot layer");
		} else {
			if (parents.size() == 1 && parents.get(0) == ModuleLayer.boot()) {
				System.out.println("Parent is boot layer");
			} else {
				for (ModuleLayer parentLayer : parents) {
					System.out.println(
							parentLayer.getClass().getName() + "@" + Integer.toHexString(parentLayer.hashCode()));
				}
			}
		}

		System.out.println("Layer's configuration including read dependencies:");
		System.out.println(layer.configuration().modules().stream() //
				.filter(mod -> !isJdkModule(mod)) //
				.map(resMod -> { //
					return String.format("%s%s%s -> [%s]", ANSI_RED, printModuleNameAndConfiguration(resMod),
							ANSI_RESET, printRequires(resMod, true));
				}).sorted().collect(Collectors.joining(", ")));
	}

	public static String printRequires(ResolvedModule resMod, boolean excludeJdk) {
		return resMod.reads().stream().filter(mod -> !(excludeJdk && isJdkModule(resMod)))
				.map(LayerPrinter::printModuleNameAndConfiguration).sorted().collect(Collectors.joining(", "));
	}

	public static boolean isJdkModule(ResolvedModule mod) {
		return mod.name().startsWith("jdk.");
	}

	public static boolean isJavaModule(ResolvedModule mod) {
		return mod.name().startsWith("java.");
	}

	/**
	 * Prints the name of a resolved module including the id of its configuration.
	 */
	public static String printModuleNameAndConfiguration(ResolvedModule mod) {
		return String.format("%s (%s)", mod.reference().descriptor().toNameAndVersion(), printId(mod.configuration()));
	}

	/**
	 * Prints an Id for an object using the simple class name and a hex string of
	 * the hashcode.
	 */
	public static String printId(Object obj) {
		return obj.getClass().getSimpleName() + "@" + Integer.toHexString(obj.hashCode());
	}
}
