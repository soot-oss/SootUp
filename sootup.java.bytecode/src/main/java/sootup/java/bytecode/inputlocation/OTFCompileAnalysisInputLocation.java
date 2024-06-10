package sootup.java.bytecode.inputlocation;

import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.core.interceptors.BytecodeBodyInterceptors;

import javax.annotation.Nonnull;
import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * e.g. to simplify creating testcases - no manual compilation step is required
 *
 * TODO: cache compilation results if src did not change
 */
public class OTFCompileAnalysisInputLocation implements AnalysisInputLocation {

    private final List<AnalysisInputLocation> inputLocations;
    private final Path outputDir = Paths.get("bin/");

    /** for Java file contents as a String i.e. not as a File on the filesystem */
    public OTFCompileAnalysisInputLocation(String fileName, String compilationUnitsContent){
        this(compile(fileName, compilationUnitsContent), "", SourceType.Application, BytecodeBodyInterceptors.Default.getBodyInterceptors() );
    }

    public OTFCompileAnalysisInputLocation(String fileName, String compilationUnitsContent,  @Nonnull SourceType srcType, @Nonnull List<BodyInterceptor> bodyInterceptors){
        this(compile(fileName, compilationUnitsContent), "", srcType, bodyInterceptors );
    }

    /** existing .java files */
    public OTFCompileAnalysisInputLocation(Path dotJavaFile) {
        this(Collections.singletonList(dotJavaFile));
    }

    public OTFCompileAnalysisInputLocation(List<Path> dotJavaFile) {
        this(dotJavaFile, "");
    }

    public OTFCompileAnalysisInputLocation(Path dotJavaFile, String omittedPackageName) {
        this(Collections.singletonList(dotJavaFile), omittedPackageName);
    }

    public OTFCompileAnalysisInputLocation(List<Path> dotJavaFile, String omittedPackageName) {
        this(dotJavaFile, omittedPackageName, SourceType.Application, BytecodeBodyInterceptors.Default.getBodyInterceptors());
    }

    public OTFCompileAnalysisInputLocation(@Nonnull List<Path> dotJavaFiles, @Nonnull String omittedPackageName, @Nonnull SourceType srcType, @Nonnull List<BodyInterceptor> bodyInterceptors) {
        inputLocations = new ArrayList<>(dotJavaFiles.size());
        dotJavaFiles.forEach(file -> inputLocations.add(new PathBasedAnalysisInputLocation.ClassFileBasedAnalysisInputLocation(file, omittedPackageName, srcType, bodyInterceptors)));
    }

    @Nonnull
    @Override
    public Optional<? extends SootClassSource> getClassSource(@Nonnull ClassType type, @Nonnull View view) {
        return inputLocations.parallelStream().map(il -> il.getClassSource(type, view) ).filter(Optional::isPresent).map(Optional::get).findFirst();
    }

    @Nonnull
    @Override
    public Collection<? extends SootClassSource> getClassSources(@Nonnull View view) {
        return inputLocations.stream().flatMap(il -> il.getClassSources(view).stream()).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public SourceType getSourceType() {
        return inputLocations.get(0).getSourceType();
    }

    @Nonnull
    @Override
    public List<BodyInterceptor> getBodyInterceptors() {
        // hint: all referenced inputlocations have the same settings
        return inputLocations.get(0).getBodyInterceptors();
    }

    static List<Path> compile(String fileName, String fileContent){
        try {
            Path tmp = Files.createTempDirectory("compile-test-");
            Path src = tmp.resolve(fileName);
            Files.write(src, fileContent.getBytes());
            return compile( src, src.toFile() );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    static List<Path> compile(Path tmpDir, File... srcFiles) {
        // based on https://stackoverflow.com/questions/39239285/how-to-get-list-of-class-files-generated-by-javacompiler-compilationtask
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        List<Path> compiledFiles = new ArrayList<>();
        try {
            JavaFileManager.Location location = StandardLocation.CLASS_OUTPUT;
            fileManager.setLocation(location, Collections.singleton(tmpDir.toFile()));

            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, null, null, fileManager.getJavaFileObjects(srcFiles));

            if (task.call()) {
                for (JavaFileObject jfo : fileManager.list(location, "", Collections.singleton(JavaFileObject.Kind.CLASS), true)) {
                    compiledFiles.add(Paths.get(jfo.toUri()));
                }
            }
            return compiledFiles;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
