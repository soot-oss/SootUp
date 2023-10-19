import dexpler.DexClassProvider;
import dexpler.DexFileProvider;
import org.jf.dexlib2.iface.DexFile;
import sootup.core.frontend.AbstractClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.ClassModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootClassSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class ApkAnalysisInputLocation<J extends SootClass<JavaSootClassSource>> implements AnalysisInputLocation<JavaSootClass> {

    Path path;

    int api_version;

    final Map<String, EnumSet<ClassModifier>> classNamesList;

    public ApkAnalysisInputLocation(Path path, int api_version){
        this.path = path;
        this.api_version = api_version;
        this.classNamesList = extractDexFilesFromPath();
    }

    private Map<String, EnumSet<ClassModifier>> extractDexFilesFromPath() {
        List<DexFileProvider.DexContainer<? extends DexFile>> dexFromSource;
        try {
            dexFromSource = new DexFileProvider().getDexFromSource(new File(path.toString()), api_version);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Map <String, EnumSet<ClassModifier>> classList = new HashMap<>();
        dexFromSource.forEach(dexContainer -> dexContainer.getBase().getDexFile().getClasses().forEach(dexClass -> classList.put(dexClass.toString(), getClassModifiers(dexClass.getAccessFlags()))));
        return classList;
    }

    public static EnumSet<ClassModifier> getClassModifiers(int access) {
        EnumSet<ClassModifier> modifierEnumSet = EnumSet.noneOf(ClassModifier.class);

        // add all modifiers for which (access & ABSTRACT) =! 0
        for (ClassModifier modifier : ClassModifier.values()) {
            if ((access & modifier.getBytecode()) != 0) {
                modifierEnumSet.add(modifier);
            }
        }
        return modifierEnumSet;
    }

    @Nonnull
    @Override
    public Optional<? extends AbstractClassSource<JavaSootClass>> getClassSource(@Nonnull ClassType type, @Nonnull View<?> view) {
        return Objects.requireNonNull(getClassSourceInternal(type, new DexClassProvider(view)));
    }

    private Optional<? extends AbstractClassSource<JavaSootClass>> getClassSourceInternal(ClassType type,
                                                                                          DexClassProvider dexClassProvider) {

        return dexClassProvider.createClassSource(this, path, type);
    }

    @Nonnull
    @Override
    public Collection<? extends AbstractClassSource<JavaSootClass>> getClassSources(@Nonnull View<?> view) {
        return null;
    }

    @Nullable
    @Override
    public SourceType getSourceType() {
        return SourceType.Application;
    }
}
