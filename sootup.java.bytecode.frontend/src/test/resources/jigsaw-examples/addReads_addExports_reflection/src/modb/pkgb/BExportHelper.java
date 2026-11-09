package pkgb;

import java.lang.Module;

// this class is called from modmain/Main to add an exports of modb/pkgb to modmain

// TODO: this can only be done if its package is exported already?! or how otherwise?

public class BExportHelper {
    public final static void addExports(String pkgName, Module targetModule) {
        Module myModule = BExportHelper.class.getModule();
        
        System.out.println("BExportHelper: add exports of " + pkgName + " to " + targetModule.getName());

        // addExports of package pkgName to targetModule
        //    Note that this is caller-sensitive, needs to be done in this module
        //    If it would be done in modmain/Main, then a runtime exception is thrown: 
        //        java.lang.IllegalStateException: module modmain != module modb
        myModule.addExports(pkgName, targetModule);
    }
}
