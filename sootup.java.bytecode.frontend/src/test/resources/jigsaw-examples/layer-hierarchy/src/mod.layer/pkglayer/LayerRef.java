package pkglayer;

import java.lang.ModuleLayer;

// leaf node in the layer hierarchy tree

public class LayerRef extends AbstractLayerRef {
    public LayerRef(final LayerGroup parent, final String name, final String level) {
        super(parent, name, level);
    }

    public LayerRef(final LayerGroup parent, final String name, final String level, final ModuleLayer layer) {
        super(parent, name, level, layer);
    }

    @Override
    public String toString() { 
        return name + " on " + level; 
    }
}