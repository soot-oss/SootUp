package pkglayer;

import java.lang.ModuleLayer;
import java.lang.Module;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// helper class which holds the layer hierarchy tree, constructs its root (i.e. the boot layer) and has some more utility collections

public class LayerHierarchy {
    // global map of all layer names -> AbstractLayerRef
    static final Map<String, AbstractLayerRef> mapName2AbstractLayerRef  = new HashMap<String, AbstractLayerRef>();
    // global map of all layers -> AbstractLayerRef
    static final Map<ModuleLayer, AbstractLayerRef>  mapLayer2AbstractLayerRef = new HashMap<ModuleLayer, AbstractLayerRef>();

    public static String getLayerName(final ModuleLayer layer) {
        return mapLayer2AbstractLayerRef.get(layer).name;
    }
    public static String getLayerLevel(final ModuleLayer layer) {
        return mapLayer2AbstractLayerRef.get(layer).level;
    }

    // ---------------------------------------------------------------------------------

    public static final String BOOT = "boot";
    public static final String ROOT = "root";
    public static LayerGroup root = new LayerGroup(null, BOOT, ROOT, 0, ModuleLayer.boot());

    // ---------------------------------------------------------------------------------

    public static void printLayerHierarchy(final AbstractLayerRef lRef) {
        System.out.println("Layer  '" + lRef.name + "'on level '" + lRef.level + "' (" + lRef.getLayer().toString() + ")");
        
        AbstractLayerRef parentRef = lRef.parent;
        if (parentRef != null) {
            List<java.lang.ModuleLayer> parentLayers = lRef.getLayer().parents();
            for (java.lang.ModuleLayer parentLayer: parentLayers) {
            	System.out.println("Parent '" + parentRef.name + "'on level '" + parentRef.level + "' (" + parentLayer.toString() + ")");
            }
        }

        if (lRef.getLayer().modules().isEmpty()) {
            System.out.println("Contains no modules.");
        }
        else {
            System.out.println("Contains these modules:");
            lRef.getLayer().modules().stream()
                    .sorted(Comparator.comparing(Module::getName))
                    .forEach(module -> System.out.println("    " + module));
        }
        
        if (lRef instanceof LayerGroup) {
            LayerGroup group = (LayerGroup) lRef;
            for (AbstractLayerRef l: group.getAllChildren()) {
                printLayerHierarchy(l);
            }
        }
    }
}
