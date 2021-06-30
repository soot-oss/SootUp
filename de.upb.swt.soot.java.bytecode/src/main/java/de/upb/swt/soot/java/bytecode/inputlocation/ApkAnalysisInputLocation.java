package de.upb.swt.soot.java.bytecode.inputlocation;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.core.JavaSootClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ApkAnalysisInputLocation implements BytecodeAnalysisInputLocation {
    private static final @Nonnull
    Logger logger = LoggerFactory.getLogger(ApkAnalysisInputLocation.class);

    @Nonnull private final List<Path> apkPaths;

    public ApkAnalysisInputLocation(@Nonnull String apkPath){
        if (isNullOrEmpty(apkPath)) {
            throw new IllegalStateException("Empty apk path given");
        }
        apkPaths = JavaClassPathAnalysisInputLocation.explode(apkPath).collect(Collectors.toList());

        if(apkPaths.isEmpty()){
            throw new IllegalStateException("Empty apk path is given.");
        }
    }

    @Override
    @Nonnull
    public Optional<? extends AbstractClassSource<JavaSootClass>> getClassSource(@Nonnull ClassType type, @Nonnull ClassLoadingOptions classLoadingOptions) {
        return Optional.empty();
    }

    @Override
    @Nonnull
    public Collection<? extends AbstractClassSource<JavaSootClass>> getClassSources(@Nonnull IdentifierFactory identifierFactory, @Nonnull ClassLoadingOptions classLoadingOptions) {
        return null;
    }
}
