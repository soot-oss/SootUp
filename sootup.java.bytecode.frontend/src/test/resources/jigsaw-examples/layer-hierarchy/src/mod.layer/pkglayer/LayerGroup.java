package pkglayer;

import java.lang.ModuleLayer;
import java.util.HashSet;
import java.util.Set;

// non-leaf node in the layer hierarchy tree

public class LayerGroup extends AbstractLayerRef {
    private final Set<AbstractLayerRef> children = new HashSet<AbstractLayerRef>();
    private final int levelCnt;
    
    public LayerGroup(final LayerGroup parent, final String name, final String level, final int levelCnt) {
        super(parent, name, level);
        this.levelCnt = levelCnt;
    }
    
    public LayerGroup(final LayerGroup parent, final String name, final String level, final int levelCnt, final ModuleLayer layer) {
        super(parent, name, level, layer);
        this.levelCnt = levelCnt;
    }
    
    public void addChildLayer(final AbstractLayerRef l) { 
        children.add(l);
    }

    public Set<AbstractLayerRef> getAllChildren() { 
        return children; 
    }    
    
    @Override
    public String toString() {
        String levelTabs = "";
        for (int i=0;i<levelCnt;i++) levelTabs += "\t";  // TODO better indentation needed...
        return name + " on level " + level + " contains\n" + levelTabs + children.toString() + "\n"; 
    }
}