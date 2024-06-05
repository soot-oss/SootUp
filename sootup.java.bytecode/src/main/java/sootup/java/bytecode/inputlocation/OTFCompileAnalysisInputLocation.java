package sootup.java.bytecode.inputlocation;

import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.core.interceptors.BytecodeBodyInterceptors;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * e.g. to simplify creating testcases - no manual recompilation is required :-)
 * */
public class OTFCompileAnalysisInputLocation implements AnalysisInputLocation {

    // TODO: public OTFCompileAnalysisInputLocation(String compilationUnitContents){}
    private final List<AnalysisInputLocation> inputLocations;
    private final Path outputDir = Paths.get("bin/");

    public OTFCompileAnalysisInputLocation(Path dotJavaFile){
        this(Collections.singletonList(dotJavaFile));
    }

    public OTFCompileAnalysisInputLocation(List<Path> dotJavaFile){
        this(dotJavaFile, "");
    }

    public OTFCompileAnalysisInputLocation(Path dotJavaFile, String omittedPackageName){
        this(Collections.singletonList(dotJavaFile), omittedPackageName);
    }

    public OTFCompileAnalysisInputLocation(List<Path> dotJavaFile, String omittedPackageName){
        this(dotJavaFile, omittedPackageName, SourceType.Application, BytecodeBodyInterceptors.Default.getBodyInterceptors());
    }


    public OTFCompileAnalysisInputLocation(@Nonnull List<Path> dotJavaFiles, @Nonnull String omittedPackageName, @Nonnull SourceType srcType, @Nonnull List<BodyInterceptor> bodyInterceptors) {
        inputLocations = new ArrayList<>(dotJavaFiles.size());
        dotJavaFiles.forEach( file -> inputLocations.add( new PathBasedAnalysisInputLocation.ClassFileBasedAnalysisInputLocation(file, omittedPackageName, srcType, bodyInterceptors)));
    }

    @Nonnull
    @Override
    public Optional<? extends SootClassSource> getClassSource(@Nonnull ClassType type, @Nonnull View view) {
        return inputLocations.stream().map( il -> (Optional<? extends SootClassSource>) il.getClassSource(type, view)).findFirst();
    }

    @Nonnull
    @Override
    public Collection<? extends SootClassSource> getClassSources(@Nonnull View view) {
        return inputLocations.stream().flatMap( il -> il.getClassSources(view)).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public SourceType getSourceType() {
        return inputLocations.get(0).getSourceType();
    }

    @Nonnull
    @Override
    public List<BodyInterceptor> getBodyInterceptors() {
        return inputLocations.get(0).getBodyInterceptors();
    }



    List<Path> compile(){
        // https://stackoverflow.com/questions/39239285/how-to-get-list-of-class-files-generated-by-javacompiler-compilationtask
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null,null,null);
        Path tmp=Files.createTempDirectory("compile-test-");
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT,Collections.singleton(tmp.toFile()));
        Path src=tmp.resolve("A.java");
        Files.write(src, Arrays.asList(
                "package test;",
                "class A {",
                "    class B {",
                "    }",
                "}"
        ));
        CompilationTask task = compiler.getTask(null, fileManager,
                null, null, null, fileManager.getJavaFileObjects(src.toFile()));
        if(task.call()) {
            for(JavaFileObject jfo: fileManager.list(StandardLocation.CLASS_OUTPUT,
                    "", Collections.singleton(JavaFileObject.Kind.CLASS), true)) {
                System.out.println(jfo.getName());
            }
        }

    }

}
