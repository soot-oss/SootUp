package dexpler;

import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MultiDexContainer;
import sootup.core.frontend.ResolveException;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.*;
import sootup.core.types.ClassType;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaSootClassSource;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DexClassSource extends JavaSootClassSource {

    DexLibWrapper wrapper;

    protected Map<File, DexLibWrapper> cache = new TreeMap<File, DexLibWrapper>();

    DexLibWrapper.ClassInformation classInformation;

    public DexClassSource(@Nonnull AnalysisInputLocation<? extends SootClass<?>> srcNamespace,
                          @Nonnull ClassType classSignature,
                          @Nonnull Path sourcePath) {
        super(srcNamespace, classSignature, sourcePath);
        // Initialize only for the first time.
        if(this.wrapper == null) {
            this.wrapper = initializeDexFile(new File(sourcePath.toString()));
        }
        this.classInformation = wrapper.getClassInformation(classSignature);
    }

    @Nonnull
    @Override
    public Collection<? extends SootMethod> resolveMethods() throws ResolveException {
        Iterable<? extends Method> methodIterable = classInformation.classDefinition.getMethods();
        DexMethod dexMethod = createDexMethodFactory(classInformation.dexEntry, classSignature);
        // Convert the Iterable to a Stream
        Stream<? extends Method> methodStream = StreamSupport.stream(methodIterable.spliterator(), false);
        Iterable<? extends Method> virtualMethodIterable = classInformation.classDefinition.getVirtualMethods();
        Stream<? extends Method> virtualMethodStream = StreamSupport.stream(virtualMethodIterable.spliterator(), false);
        return Stream.concat(methodStream, virtualMethodStream).map(method -> loadMethod(method, dexMethod)).collect(Collectors.toSet());
    }

    @Nonnull
    @Override
    public Collection<? extends SootField> resolveFields() throws ResolveException {
        return null;
    }

    @Nonnull
    @Override
    public Set<ClassModifier> resolveModifiers() {
        return null;
    }

    @Nonnull
    @Override
    public Set<? extends ClassType> resolveInterfaces() {
        return null;
    }

    @Nonnull
    @Override
    public Optional<? extends ClassType> resolveSuperclass() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Optional<? extends ClassType> resolveOuterClass() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Position resolvePosition() {
        return null;
    }

    @Override
    protected Iterable<AnnotationUsage> resolveAnnotations() {
        return null;
    }

    private DexMethod createDexMethodFactory(MultiDexContainer.DexEntry<? extends DexFile> dexEntry, final ClassType declaringClass) {
        return new DexMethod(dexEntry, declaringClass);
    }

    private DexLibWrapper initializeDexFile(File file) {
        DexLibWrapper wrapper = cache.get(file);
        if (wrapper == null) {
            wrapper = new DexLibWrapper(file);
            cache.put(file, wrapper);
            wrapper.initialize();
        }
        return wrapper;
    }

    private SootMethod loadMethod(Method method, DexMethod dexMethod) {
        SootMethod sootMethod = dexMethod.makeSootMethod(method);
        return sootMethod;
    }
}
