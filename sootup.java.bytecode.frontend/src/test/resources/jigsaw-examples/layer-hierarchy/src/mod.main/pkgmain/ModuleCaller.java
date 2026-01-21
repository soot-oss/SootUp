package pkgmain;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.ModuleLayer;
import java.lang.Module;
import java.util.Comparator;

import pkglayer.AbstractLayerRef;
import pkglayer.LayerGroup;

public class ModuleCaller {
    public void callAllViaReflection(final AbstractLayerRef root) throws Exception {
        System.out.println("\n-------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Now calling stuff in all mod.x* modules (on any level) ...");
        callModuleViaReflection(root, "mod.x_", "pkgx", "X", false, "doIt");

        System.out.println("\n-------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Now calling stuff in all mod.y* modules (on any level) ...");
        callModuleViaReflection(root, "mod.y_", "pkgy", "Y", true, "doIt");
        
        System.out.println("\n-------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Now calling stuff in all mod.z* modules (on any level) ...");
        callModuleViaReflection(root, "mod.z_", "pkgz", "Z", true, "doIt");
        
        System.out.println("\n-------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Now calling stuff in all mod.zreverse* modules (on any level) ...");
        callModuleViaReflection(root, "mod.zreverse_", "pkgzreverse", "Z", true, "doIt");

        System.out.println("\n-------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Now calling stuff in all mod.u* modules (on any level) ...");
        callModuleViaReflection(root, "mod.u_", "pkgu", "U", false, "doIt");
    }

    // Call ...doIt() via reflection in each of the mod.... modules in each of the layers

    private void callModuleViaReflection(final AbstractLayerRef lRef, 
            final String modNamePrefix, final String pkgName, final String clazzNamePrefix, 
            final boolean useLayerInClazzName, final String methodName) 
    {
        ModuleLayer layer = lRef.getLayer();
        layer.modules()
             .stream()
             .filter(mod -> mod.getName().startsWith(modNamePrefix))
             .sorted(Comparator.comparing(Module::getName))
             .forEach(mod -> {
                    System.out.println("\nLayer '" + lRef.name + "' on level '" + lRef.level + "'");
                 
                    // this is not needed, as for reflection, readability is always given "for free"
                    // ModuleCaller.class.getModule().addReads(mod);

                    String moduleName = mod.getName();
                    System.out.println("Module '" + moduleName + "'");
                         
                    try {
                        // might differ from layerName (in case of the zreverse modules, all are in top!)
                        String layerFromModName = moduleName.split("_")[1];
                        String layerNameCapitalized = layerFromModName.substring(0, 1).toUpperCase() + layerFromModName.substring(1);
                            
                        String clazzName = pkgName 
                                + (useLayerInClazzName ? "." + layerFromModName : "" )
                                + "." + clazzNamePrefix
                                + (useLayerInClazzName ? layerNameCapitalized + "Class" : "");
                        Class<?> clazz = layer.findLoader(mod.getName()).loadClass(clazzName);
        
                        Constructor<?> con = clazz.getDeclaredConstructor();
                        con.setAccessible(true);
                        Object theInstance = con.newInstance();
                            
                        // call static X.doIt() via reflection
                        Method method = clazz.getMethod(methodName);
                        method.setAccessible(true);
                             
                        // and print out the result of the call
                        String result = (String) method.invoke(theInstance);
                        System.out.println("Calling " + mod.getName() + "/"+clazzName+"."+methodName+"() returns:\n" + result);
                    }
                    catch (Exception ex) {
                        ex.printStackTrace(System.err);
                    }
                });
        
        if (lRef instanceof LayerGroup) {
            LayerGroup group = (LayerGroup) lRef;
            for (AbstractLayerRef l: group.getAllChildren()) {
                callModuleViaReflection(l, modNamePrefix, pkgName, clazzNamePrefix, useLayerInClazzName, methodName);
            }
        }
    }
}
