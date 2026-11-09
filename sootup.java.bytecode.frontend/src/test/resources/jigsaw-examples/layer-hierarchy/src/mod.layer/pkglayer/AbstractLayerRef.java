package pkglayer;

import java.lang.ModuleLayer;

// abstract node in the hierarchy tree (composite pattern)

public abstract class AbstractLayerRef {
    public LayerGroup parent = null;
    public final String name;
    public final String level;

    AbstractLayerRef(final LayerGroup parent, final String name, final String level) {
        if (LayerHierarchy.mapName2AbstractLayerRef.containsKey(name)) {
            throw new IllegalStateException("Duplicate layer found with name '" + name + "'. Names must be unique!");
        }
        LayerHierarchy.mapName2AbstractLayerRef.put(name, this);

        this.parent = parent;
        this.name   = name;
        this.level  = level;
    }

    AbstractLayerRef(final LayerGroup parent, final String name, final String level, final ModuleLayer layer) {
        this(parent, name, level);
        setLayer(layer);
    }

    // ---------------------------------------------------------------------------------------------------------------------
    
    private java.lang.ModuleLayer layer;       // reference to a Jigsaw layer
    
    public void setLayer(final ModuleLayer layer) {
        this.layer = layer;
        LayerHierarchy.mapLayer2AbstractLayerRef.put(this.layer, this);
    }
    
    public ModuleLayer getLayer() {
        return layer;
    }

    // ---------------------------------------------------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((layer == null) ? 0 : layer.hashCode());
        result = prime * result + ((level == null) ? 0 : level.hashCode());
        result = prime * result + ((name  == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AbstractLayerRef other = (AbstractLayerRef) obj;
        if (layer == null) {
            if (other.layer != null) return false;
        } 
        else if (!layer.equals(other.layer)) return false;
        if (level == null) {
            if (other.level != null) return false;
        }
        else if (!level.equals(other.level)) return false;
        if (name == null) {
            if (other.name != null) return false;
        } 
        else if (!name.equals(other.name)) return false;
        if (parent == null) {
            if (other.parent != null) return false;
        } 
        else if (!parent.equals(other.parent)) return false;
        return true;
    }
}