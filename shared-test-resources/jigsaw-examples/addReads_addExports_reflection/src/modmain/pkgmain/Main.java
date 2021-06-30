package pkgmain;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.ModuleLayer;
import java.lang.Module;
import java.util.Optional;

import pkgb.BExportHelper;

/**
 * This class cannot be compiled in Eclipse as compiler options --add-exports are needed.
 * See compile.sh for details.
 * 
 * But if compilation has taken place outside Eclipse with the script,
 * the modular JARs can be found in .../mlib
 * 
 * The Eclipse launch files takes the compiled code from there and can be run in Eclipse.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Main mymain = new Main();
        Module modmain = Main.class.getModule();

        // find module modb
        Optional<Module> optMod = ModuleLayer.boot().findModule("modb");
        if (optMod.orElseGet(null) == null) {
            throw new RuntimeException("Main: Could not find modb");
        }
        Module modb = optMod.get();

        // -----------------------------------------------
        // Implement the 'addReads modmain=modb'
        //    note that this is caller-sensitive, needs to be done in modmain, i.e. here
        System.out.println("Main: add reads of " + modmain.getName() + " to " + modb.getName());
        modmain.addReads(modb);
        
        // -----------------------------------------------
        // Ask for the 'addExports modb/pkgb=modmain'
        // Note that this is caller-sensitive, so we need this to in a class which belongs to module modb
        // If we would call the addExports from here directly like with:
        //      BExportHelper.class.getModule().addExports("pkgbinternal", modmain);
        // we would produce a java.lang.IllegalCallerException: module modmain != module modb
        
        // So instead we ask the BExportHelper to get access to the internal package pkgbinternal.
        // For that, Java Compiler and also Runtime option needed first: --add-exports modb/pkgb=modmain
        BExportHelper.addExports("pkgbinternal", modmain);
        
        // -----------------------------------------------
        // get an instance of pkgb.B and call its doIt() method
        Class<?> myinternalBClass = Class.forName("pkgbinternal.InternalB");
        Constructor<?> con = myinternalBClass.getDeclaredConstructor();
        con.setAccessible(true);
        Object myInternalB = con.newInstance();

        Method m = myInternalB.getClass().getMethod("doIt");
        m.setAccessible(true);
        System.out.println("Main: " + mymain.toString() + ", InternalB: " + m.invoke(myInternalB));
    }
}
