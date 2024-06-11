package sootup.java.bytecode.inputlocation;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.tools.*;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.core.interceptors.BytecodeBodyInterceptors;

/**
 * e.g. to simplify creating testcases - no manual compilation step is required
 *
 * <p>TODO: cache compilation results if src did not change
 */
public class OTFCompileAnalysisInputLocation implements AnalysisInputLocation {

  private final List<AnalysisInputLocation> inputLocations = new ArrayList<>();

  /** for Java file contents as a String i.e. not as a File on the filesystem */
  public OTFCompileAnalysisInputLocation(String fileName, String compilationUnitsContent) {
    this(
        fileName,
        compilationUnitsContent,
        SourceType.Application,
        BytecodeBodyInterceptors.Default.getBodyInterceptors());
  }

  public OTFCompileAnalysisInputLocation(
      String fileName,
      String compilationUnitsContent,
      @Nonnull SourceType srcType,
      @Nonnull List<BodyInterceptor> bodyInterceptors) {
    Path compile = compile(fileName, compilationUnitsContent);
    inputLocations.add(PathBasedAnalysisInputLocation.create(compile, srcType, bodyInterceptors));
  }

  /** existing .java files */
  public OTFCompileAnalysisInputLocation(Path dotJavaFile) {
    this(Collections.singletonList(dotJavaFile));
  }

  public OTFCompileAnalysisInputLocation(List<Path> dotJavaFile) {
    this(
        dotJavaFile,
        SourceType.Application,
        BytecodeBodyInterceptors.Default.getBodyInterceptors());
  }

  public OTFCompileAnalysisInputLocation(
      @Nonnull List<Path> dotJavaFiles,
      @Nonnull SourceType srcType,
      @Nonnull List<BodyInterceptor> bodyInterceptors) {
    dotJavaFiles.forEach(
        file -> {
          Path compile = compile(file.toFile());
          inputLocations.add(
              PathBasedAnalysisInputLocation.create(compile, srcType, bodyInterceptors));
        });
  }

  @Nonnull
  @Override
  public Optional<? extends SootClassSource> getClassSource(
      @Nonnull ClassType type, @Nonnull View view) {
    return inputLocations
        .parallelStream()
        .map(il -> il.getClassSource(type, view))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  @Nonnull
  @Override
  public Collection<? extends SootClassSource> getClassSources(@Nonnull View view) {
    return inputLocations.stream()
        .flatMap(il -> il.getClassSources(view).stream())
        .collect(Collectors.toList());
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

  static Path compile(String fileName, String fileContent) {
    try {
      Path tmp = getTempDirectory(fileName);
      Path src = tmp.resolve(fileName);
      Files.write(src, fileContent.getBytes());
      return compile(src.toFile());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Path getTempDirectory(String fileName) throws IOException {
    return Files.createTempDirectory("sootup-otfcompile-" + fileName.hashCode());
  }

  @Nonnull
  static Path compile(File... srcFiles) {
    // based on
    // https://stackoverflow.com/questions/39239285/how-to-get-list-of-class-files-generated-by-javacompiler-compilationtask
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
    List<Path> compiledFiles = new ArrayList<>();

    // create key for temp dir / caching
    StringBuilder sb = new StringBuilder();
    for (File srcFile : srcFiles) {
      sb.append(srcFile);
    }
    String concatenatedFileNames = sb.toString();

    try {
      JavaFileManager.Location location = StandardLocation.CLASS_OUTPUT;
      File file = getTempDirectory(concatenatedFileNames).resolve("bin/").toFile();
      file.mkdirs();

      fileManager.setLocation(location, Collections.singleton(file));

      Writer writer = new StringWriter();
      JavaCompiler.CompilationTask task =
          compiler.getTask(
              writer, fileManager, null, null, null, fileManager.getJavaFileObjects(srcFiles));

      if (task.call()) {
        for (JavaFileObject jfo :
            fileManager.list(
                location, "", Collections.singleton(JavaFileObject.Kind.CLASS), true)) {
          compiledFiles.add(Paths.get(jfo.toUri()));
        }
        return file.toPath();
      } else {
        throw new IllegalArgumentException("Could not compile the given input.\n " + writer);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
