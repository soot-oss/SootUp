package pkgy.bottom;

import java.lang.ModuleLayer;

import pkglayer.LayerHierarchy;

public class YBottomClass extends pkgy.middle.YMiddleClass {
    @Override
    public String doIt() {
        ModuleLayer myLayer = this.getClass().getModule().getLayer();
        String layerName  = LayerHierarchy.getLayerName(myLayer);
        String layerLevel = LayerHierarchy.getLayerLevel(myLayer);

        return "\t" + this.toString() + " [ " + YBottomClass.class
            + ", module " + this.getClass().getModule().getName() 
            + ", layer '" + layerName + "' on level '" + layerLevel + "' (" + myLayer + ") ]"

            + "\n\tplus " + super.doIt();
    }
}
