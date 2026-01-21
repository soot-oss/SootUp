package pkgmain;

import pkglayer.LayerHierarchy;

public class Main {
	private static void buildUpLayerHierarchyAndAddModules(String[] args) throws Exception {
    	String path;
    	if (args.length == 0) {
    		// no path given? then use current working directory
    		path = new java.io.File(System.getProperty("user.dir")).getAbsolutePath();
    	}
    	else {
    		path = new java.io.File(args[0]).getAbsolutePath();
    	}
    	
        // parse layer JSON file
        LayerBuilder layerBuilder = new LayerBuilder(path + "/layers_triple_hierarchy.json");
        LayerHierarchy.root.addChildLayer(layerBuilder.parseLayerJsonFile(LayerHierarchy.root));
        // System.out.println(LayerHierarchy.root);
        
        // create Jigsaw layers and add modules to them
        layerBuilder.createJigsawLayers(LayerHierarchy.root, LayerHierarchy.root.getLayer(), path + "/mlib");
	}
	
    public static void main(String[] args) throws Exception {    	
    	// step 1: build up layer hierarchy as defined in the JSON file and add modules from the module path according to a naming convention
    	buildUpLayerHierarchyAndAddModules(args);
        
        // step 2: print out the layers, their hierarchy and their modules
        LayerHierarchy.printLayerHierarchy(LayerHierarchy.root);
        
        // step 3: call module's classes via reflection
        new ModuleCaller().callAllViaReflection(LayerHierarchy.root);
    }
}
