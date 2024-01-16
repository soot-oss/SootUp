package sootup.java.bytecode.inputlocation;

import sootup.core.Language;
import sootup.core.frontend.SootClassSource;
import sootup.core.model.SourceType;
import sootup.core.views.View;
import sootup.java.core.JavaModuleInfo;
import sootup.java.core.ModuleInfoAnalysisInputLocation;
import sootup.java.core.signatures.ModuleSignature;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class ModuleMultiReleaseJarAnalysisInputLocation extends MultiReleaseJarAnalysisInputLocation implements ModuleInfoAnalysisInputLocation {
    public ModuleMultiReleaseJarAnalysisInputLocation(@Nonnull Path path, @Nonnull SourceType srcType, @Nonnull Language language) {
        super(path, srcType, language);
    }

    @Override
    protected ModuleInfoAnalysisInputLocation createAnalysisInputLocation(@Nonnull Path path) {
        try {
            return new JavaModulePathAnalysisInputLocation(path.toString(), fileSystemCache.get(this.path), sourceType );
        } catch (ExecutionException e) {
            throw new IllegalArgumentException("Could not open filesystemcache.", e);
        }
    }


    @Override
    public Collection<? extends SootClassSource> getModulesClassSources(@Nonnull ModuleSignature moduleSignature, @Nonnull View view) {
        // FIXME: implement
        return null;
    }

    @Nonnull
    @Override
    public Optional<JavaModuleInfo> getModuleInfo(@Nonnull ModuleSignature sig, @Nonnull View view) {
        // TODO: check if we need to combine modules as well or if only versioned .class files are allowed
        return ( (ModuleInfoAnalysisInputLocation) inputLocations.get(DEFAULT_VERSION)).getModuleInfo(sig, view);
    }

    @Nonnull
    @Override
    public Set<ModuleSignature> getModules(@Nonnull View view) {
        // TODO: check if we need to combine modules as well or if only versioned .class files are allowed
        return ( (ModuleInfoAnalysisInputLocation) inputLocations.get(DEFAULT_VERSION)).getModules(view);
    }
}
