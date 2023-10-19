package dexpler;

import Util.Util;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.frontend.ClassProvider;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.FileType;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.core.JavaSootClass;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DexClassProvider implements ClassProvider<JavaSootClass> {


    private static final Logger logger = LoggerFactory.getLogger(DexClassProvider.class);

    @Nonnull
    private final View<?> view;

    public DexClassProvider(@Nonnull View<?> view){
        this.view = view;
    }

    public static Set<String> classesOfDex(DexFile dexFile) {
        Set<String> classes = new HashSet<>();
        for (ClassDef c : dexFile.getClasses()) {
            classes.add(Util.dottedClassName(c.getType()));
        }
        return classes;
    }

    @Override
    public Optional<SootClassSource<JavaSootClass>> createClassSource(AnalysisInputLocation<? extends SootClass<?>> inputLocation, Path sourcePath, ClassType classSignature) {
        return Optional.of(new DexClassSource(inputLocation, classSignature, sourcePath));
    }

    @Override
    public FileType getHandledFileType() {
        return FileType.DEX;
    }
}
