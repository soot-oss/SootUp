package pkgcommon;

import java.util.List;
import java.util.stream.Collectors;
import java.lang.module.ResolvedModule;

public final class LayerPrinter {
    public static void printRuntimeInfos(ModuleLayer layer, Object o) throws Exception {
    	System.out.println("Infos for Layer and Module which contain " + o);

    	// print the layer's object as an ID
    	String layerAsString = layer.getClass().getName() + "@" + Integer.toHexString(layer.hashCode());
    	
	    System.out.println("Layer (" + layerAsString
	    		+ (layer.equals(ModuleLayer.boot()) ? "), boot layer" : "), not boot layer"));
    	System.out.print("Layer's parents: ");
	    List<ModuleLayer> parents = layer.parents();
	    if (parents.isEmpty() || (parents.size()==1 && parents.contains(ModuleLayer.empty()))) {
	    	System.out.println("none, as this is the boot layer");
	    }
	    else {
	    	if (parents.size() == 1 && parents.get(0)==ModuleLayer.boot()) {
		    	System.out.println("Parent is boot layer");
	    	}
	    	else {
		    	for (ModuleLayer parentLayer: parents) {
		    		System.out.println(parentLayer.getClass().getName() + "@" + Integer.toHexString(parentLayer.hashCode()));
		    	}
	    	}
	    }

	    System.out.println("Layer's list of modules:");
	    System.out.println(o.getClass().getModule().getLayer().
	    		modules().stream().map(Module::getName).sorted().collect(Collectors.joining(", ")));
	    System.out.println("Layer's configuration with its list of resolved modules:");
	    System.out.println(o.getClass().getModule().getLayer().configuration().
	    		modules().stream().map(ResolvedModule::name).sorted().collect(Collectors.joining(", ")));
    }
}
