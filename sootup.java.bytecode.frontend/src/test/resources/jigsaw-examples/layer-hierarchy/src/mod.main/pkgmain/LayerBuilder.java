package pkgmain;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import pkglayer.AbstractLayerRef;
import pkglayer.LayerGroup;
import pkglayer.LayerRef;

public class LayerBuilder {
    private String layers_jsonFileName = null;
    
    LayerBuilder(final String layers_jsonFileName) {
        this.layers_jsonFileName = layers_jsonFileName;
    }

    // ------------------------------------------------------------------------------------------------------------------------
    
    // build layer hierarchy from JSON file (using the composite pattern from AbstractLayer, LayerHolder and LayerGroup,
    //    see LayerHierarchy.java)

    private static final String jsonLAYERS = "layers";
    private static final String jsonNAME = "name";
    private static final String jsonLEVEL = "level";
    private static final String jsonLAYER_LIST = "layerList";

    AbstractLayerRef parseLayerJsonFile(final LayerGroup root) throws IOException {
        File jsonFile = new File(layers_jsonFileName).getAbsoluteFile().getCanonicalFile();
        try (InputStream in = new FileInputStream(jsonFile)) {
            JsonReader jsonReader = Json.createReader(in);

            JsonObject jsonObj = jsonReader.readObject().getJsonObject(jsonLAYERS);
            return buildLayerHierarchyFromJsonFile(jsonObj, root);
        }
    }

    private AbstractLayerRef buildLayerHierarchyFromJsonFile(final JsonObject jsonObj, final LayerGroup root) {
        String jsonName  = jsonObj.getJsonString(jsonNAME).getString();
        String jsonLevel = jsonObj.getJsonString(jsonLEVEL).getString();
        LayerGroup group = new LayerGroup(root, jsonName, jsonLevel, 0);        

        if (jsonObj.containsKey(jsonLAYER_LIST)) {
            JsonArray jsonChildren = jsonObj.getJsonArray(jsonLAYER_LIST);
            for (int idx=0; idx<jsonChildren.size(); idx++) {
                buildLayerHierarchyFromJsonFile(jsonChildren.getJsonObject(idx), group, 1);
            }
        }
        
        return group;
    }

    private AbstractLayerRef buildLayerHierarchyFromJsonFile(final JsonObject jsonObj, final LayerGroup parent, final int levelCnt) {
        String jsonName  = jsonObj.getJsonString(jsonNAME).getString();
        String jsonLevel = jsonObj.getJsonString(jsonLEVEL).getString();

        if (jsonObj.containsKey(jsonLAYER_LIST)) {
            LayerGroup group = new LayerGroup(parent, jsonName, jsonLevel, levelCnt);
            parent.addChildLayer(group);

            JsonArray jChildren = jsonObj.getJsonArray(jsonLAYER_LIST);
            for (int idx=0; idx<jChildren.size(); idx++) {
                buildLayerHierarchyFromJsonFile(jChildren.getJsonObject(idx), group, levelCnt+1);
            }
        }
        else {
            LayerRef lRef = new LayerRef(parent, jsonName, jsonLevel);
            parent.addChildLayer(lRef);
        }
        
        return parent;
    }

    // ------------------------------------------------------------------------------------------------------------------------

    // now create the Jigsaw layers and add their modules (based on the naming convention of the module name's suffix)
    
    void createJigsawLayers(final LayerGroup root, final java.lang.ModuleLayer bootLayer, final String modulePath) {
        Path modPath = Paths.get(modulePath).toAbsolutePath().normalize();
        
        ModuleFinder moduleFinder = ModuleFinder.of(modPath);
        Set<ModuleReference> allModules = moduleFinder.findAll();
        if (allModules.isEmpty()) {
            throw new RuntimeException("No modules found in " + modPath.toString() + ". Terminating ...");
        }
             
        for (AbstractLayerRef l: root.getAllChildren()) {
            createJigsawLayers(l, bootLayer, moduleFinder, allModules);
        }
    }

    private void createJigsawLayers(final AbstractLayerRef lRef, final java.lang.ModuleLayer parentLayer, 
            final ModuleFinder moduleFinder, final Set<ModuleReference> allModules) 
    {
        createJigsawLayerAndAddModules(lRef, parentLayer, moduleFinder, allModules);
        
        if (lRef instanceof LayerGroup) {
            LayerGroup group = (LayerGroup) lRef;
            for (AbstractLayerRef l: group.getAllChildren()) {
                createJigsawLayers(l, group.getLayer(), moduleFinder, allModules);
            }
        }
    }

    private void createJigsawLayerAndAddModules(final AbstractLayerRef lRef, final java.lang.ModuleLayer parentLayer, 
            final ModuleFinder moduleFinder, final Set<ModuleReference> allModules) 
    {
        // add layers via naming convention (module's name suffix must match layer's level)
        Set<String> allFilteredModuleNames = new HashSet<>();
        String nameSuffix = "_" + lRef.level;
        allModules.stream()
            .map(modRef -> modRef.descriptor().name())
            .filter(modName -> modName.contains(nameSuffix))
            .forEach(modName -> allFilteredModuleNames.add(modName));

        // Create configuration
        Configuration cf = parentLayer.configuration()
                             .resolve(ModuleFinder.of(), moduleFinder, allFilteredModuleNames);
        
        // ... and create a new Jigsaw Layer with this configuration
        java.lang.ModuleLayer layer = parentLayer.defineModulesWithOneLoader(cf, ClassLoader.getSystemClassLoader());
        
        lRef.setLayer(layer);
    }
}
