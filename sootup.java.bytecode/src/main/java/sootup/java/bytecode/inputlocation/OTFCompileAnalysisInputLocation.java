package sootup.java.bytecode.inputlocation;

import static java.lang.System.currentTimeMillis;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
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

/** e.g. to simplify creating testcases - no manual compilation step is required */
public class OTFCompileAnalysisInputLocation implements AnalysisInputLocation {

  private final AnalysisInputLocation inputLocation;

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
    inputLocation = PathBasedAnalysisInputLocation.create(compile, srcType, bodyInterceptors);
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
    Path compile = compile(dotJavaFiles);
    inputLocation = PathBasedAnalysisInputLocation.create(compile, srcType, bodyInterceptors);
  }

  @Nonnull
  @Override
  public Optional<? extends SootClassSource> getClassSource(
      @Nonnull ClassType type, @Nonnull View view) {
    return inputLocation.getClassSource(type, view);
  }

  @Nonnull
  @Override
  public Collection<? extends SootClassSource> getClassSources(@Nonnull View view) {
    return inputLocation.getClassSources(view);
  }

  @Nonnull
  @Override
  public SourceType getSourceType() {
    return inputLocation.getSourceType();
  }

  @Nonnull
  @Override
  public List<BodyInterceptor> getBodyInterceptors() {
    // hint: all referenced inputlocations have the same settings
    return inputLocation.getBodyInterceptors();
  }

  private static Path getTempDirectoryPath(String fileName) throws IOException {
    return Files.createTempDirectory("sootup-otfcompile-" + fileName.hashCode());
  }

  static Path compile(String fileName, String fileContent) {
    // TODO: use MemoryFileManager to compile the String 'fileContent' directly i.e. without saving
    // it to the filesystem
    Path srcFile;
    try {
      Path path = getTempDirectoryPath(fileName);
      boolean isDirNewlyCreated = path.toFile().mkdirs();
      srcFile = path.resolve(fileName);

      if (isDirNewlyCreated || !Files.exists(srcFile)) {
        Files.write(srcFile, fileContent.getBytes());
      } else {
        // when the directory with the same content.hashcode() already exists, check its content as
        // well.
        byte[] bytes = Files.readAllBytes(srcFile);
        if (!new String(bytes).equals(fileContent)) {
          // only write when sth actually changed
          Files.write(srcFile, fileContent.getBytes());
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return compile(Collections.singletonList(srcFile));
  }

  @Nonnull
  static Path compile(List<Path> srcFiles) {

    // create key for temp dir / caching
    StringBuilder sb = new StringBuilder();
    for (Path srcFile : srcFiles) {
      sb.append(srcFile);
    }
    String concatenatedFileNames = sb.toString();

    try {
      Path binDirpath = getTempDirectoryPath(concatenatedFileNames).resolve("bin/");
      File binDir = binDirpath.toFile();
      boolean binDirCreated = binDir.mkdirs();
      if (!binDirCreated) {
        // bin dir already exists -> check modified time
        FileTime binDirLastModifiedTime = Files.getLastModifiedTime(binDirpath);
        boolean cacheDirty = false;
        for (Path srcFile : srcFiles) {
          if (Files.getLastModifiedTime(srcFile).compareTo(binDirLastModifiedTime) > 0) {
            cacheDirty = true;
          }
        }
        if (!cacheDirty) {
          return binDirpath;
        }
      }

      JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
      fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(binDir));

      File[] files = new File[srcFiles.size()];
      srcFiles.stream().map(Path::toFile).collect(Collectors.toList()).toArray(files);
      Iterable<? extends JavaFileObject> javaFileObjects = fileManager.getJavaFileObjects(files);

      try (Writer writer = new StringWriter()) {
        JavaCompiler.CompilationTask task =
            compiler.getTask(writer, fileManager, null, null, null, javaFileObjects);

        if (task.call()) {
          /* collect all generated .class files
          Set<JavaFileObject.Kind> clazzType = Collections.singleton(JavaFileObject.Kind.CLASS);
          for (JavaFileObject jfo : fileManager.list(location, "", clazzType, true)) {
            compiledFiles.add(Paths.get(jfo.toUri()));
          }*/
          if (!binDirCreated) {
            // update modified timestamp of bin/
            Files.setLastModifiedTime(binDirpath, FileTime.fromMillis(currentTimeMillis()));
          }
          return binDir.toPath();
        } else {
          throw new IllegalArgumentException("Could not compile the given input.\n " + writer);
        }
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
