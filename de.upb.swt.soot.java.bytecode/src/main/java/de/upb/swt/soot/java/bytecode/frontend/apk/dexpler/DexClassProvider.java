package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler;

import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.views.JavaView;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class DexClassProvider implements ClassProvider<JavaSootClass> {

    private static Map<String, File> index; // TODO: double check

    private static final Logger logger = LoggerFactory.getLogger(DexClassProvider.class);

    private JavaView view;

    public DexClassProvider(JavaView view){
        this.view = view;
    }

    @Override
    public DexClassSource createClassSource(AnalysisInputLocation<? extends SootClass<?>> inputLocation, Path sourcePath, ClassType classSignature) {
        File file = index.get(classSignature.getFullyQualifiedName());
        if(file==null){
            return null;
        }else{
            return new DexClassSource(inputLocation, classSignature, sourcePath, view);
        }
    }

    @Override
    public FileType getHandledFileType() {
        return FileType.DEX;
    }

    public Set<String> getClassNames(Path sourcePath){
        ensureDexIndex(sourcePath);
        return index.keySet();
    }

    public static Set<String> classesOfDex(DexFile dexFile) {
        Set<String> classes = new HashSet<String>();
        for (ClassDef c : dexFile.getClasses()) {
            classes.add(Util.dottedClassName(c.getType()));
        }
        return classes;
    }

    protected void ensureDexIndex(Path path) {
        if (index == null) {
            index = new HashMap<String, File>();
            //TODO: KKwip
            buildDexIndex(index, Collections.singletonList(path));
        }

        // TODO: KKwip
        /*
        // Process the classpath extensions
        Set<String> extensions = loc.getDexClassPathExtensions();
        if (extensions != null) {
            buildDexIndex(index, new ArrayList<>(extensions));
            loc.clearDexClassPathExtensions();
        }
        */
    }

    /**
     * Build index of ClassName-to-File mappings.
     *
     * @param index
     *          map to insert mappings into
     * @param classPath
     *          paths to index
     */
    private void buildDexIndex(Map<String, File> index, List<Path> classPath) {
        for (Path path : classPath) {
            try {
                File dexFile = path.toFile();
                if (dexFile.exists()) {
                    for (DexFileProvider.DexContainer<? extends DexFile> container : new DexFileProvider().getDexFromSource(path)) {
                        for (String className : classesOfDex(container.getBase().getDexFile())) {
                            if (!index.containsKey(className)) {
                                index.put(className, container.getFilePath().toFile());
                            } else {
                                logger.debug(String.format(
                                        "Warning: Duplicate of class '%s' found in dex file '%s' from source '%s'. Omitting class.", className,
                                        container.getDexName(), container.getFilePath()));
                            }
                        }
                    }
                }
            } catch (IOException e) {
                logger.warn("IO error while processing dex file '" + path + "'");
                logger.debug("Exception: " + e);
            } catch (Exception e) {
                logger.warn("exception while processing dex file '" + path + "'");
                logger.debug("Exception: " + e);
            }
        }
    }

}
