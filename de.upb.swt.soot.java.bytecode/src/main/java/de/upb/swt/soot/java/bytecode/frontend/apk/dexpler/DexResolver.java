package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler;

import com.sun.tools.classfile.Dependencies;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.java.core.views.JavaView;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

public class DexResolver {
    protected Map<File, DexlibWrapper> cache = new TreeMap<File, DexlibWrapper>();

    /**
     * Resolve the class contained in file into the passed soot class.
     *
     * @param file
     *          the path to the dex/apk file to resolve
     * @param className
     *          the name of the class to resolve
     * @param sc
     *          the soot class that will represent the class
     * @return the dependencies of this class.
     */
    public void resolveFromFile(File file, String className, SootClass sc, JavaView view) {
        DexlibWrapper wrapper = initializeDexFile(file, view);
        wrapper.makeSootClass(sc, className);
        //addSourceFileTag(sc, "dalvik_source_" + file.getName());
    }

    /**
     * Initializes the dex wrapper for the given dex file
     *
     * @param file
     *          The dex file to load
     * @return The wrapper object for the given dex file
     */
    protected DexlibWrapper initializeDexFile(File file, JavaView view) {
        DexlibWrapper wrapper = cache.get(file);
        if (wrapper == null) {
            wrapper = new DexlibWrapper(file, view);
            cache.put(file, wrapper);
            wrapper.initialize();
        }
        return wrapper;
    }

//    /**
//     * adds source file tag to each sootclass
//     */
//    protected static void addSourceFileTag(SootClass sc, String fileName) {
//        soot.tagkit.SourceFileTag tag = null;
//        if (sc.hasTag("SourceFileTag")) {
//            return; // do not add tag if original class already has debug
//            // information
//        } else {
//            tag = new soot.tagkit.SourceFileTag();
//            sc.addTag(tag);
//        }
//        tag.setSourceFile(fileName);
//    }
}
