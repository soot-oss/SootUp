package sootup.java.bytecode.frontend.inputlocation;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018-2024 Markus Schmidt and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import static java.lang.System.currentTimeMillis;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.stream.Stream;
import javax.tools.*;
import org.jspecify.annotations.NonNull;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.interceptors.BytecodeBodyInterceptors;

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
      @NonNull SourceType srcType,
      @NonNull List<BodyInterceptor> bodyInterceptors) {
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
      @NonNull List<Path> dotJavaFiles,
      @NonNull SourceType srcType,
      @NonNull List<BodyInterceptor> bodyInterceptors) {
    Path compile = compile(dotJavaFiles);
    inputLocation = PathBasedAnalysisInputLocation.create(compile, srcType, bodyInterceptors);
  }

  @NonNull
  @Override
  public Optional<? extends SootClassSource> getClassSource(
      @NonNull ClassType type, @NonNull View view) {
    return inputLocation.getClassSource(type, view);
  }

  @NonNull
  @Override
  public Stream<? extends SootClassSource> getClassSources(@NonNull View view) {
    return inputLocation.getClassSources(view);
  }

  @NonNull
  @Override
  public SourceType getSourceType() {
    return inputLocation.getSourceType();
  }

  @NonNull
  @Override
  public List<BodyInterceptor> getBodyInterceptors() {
    return inputLocation.getBodyInterceptors();
  }

  private static Path getTempDirectory(String fileName) throws IOException {
    return Files.createTempDirectory("sootup-otfcompile-" + fileName.hashCode());
  }

  static Path compile(String fileName, String fileContent) {
    try {
      Path tmp = getTempDirectory(fileName);
      Path path = tmp.resolve(fileName.hashCode() + "/");
      boolean dirWasCreated = path.toFile().mkdirs();
      Path srcFile = tmp.resolve(fileName);

      if (dirWasCreated) {
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

      /* TODO: don't save source as file - make use of JavaFileObjectImpl
      int i = name.lastIndexOf('.');
      String packageName = i < 0 ? "" : name.substring(0, i);
      String className = i < 0 ? name : name.substring(i + 1);
      JavaFileObjectImpl javaFileObject = new JavaFileObjectImpl(className, sourceCode);
      javaFileManager.putFileForInput(StandardLocation.SOURCE_PATH, packageName,
              className + ClassUtils.JAVA_EXTENSION, javaFileObject);
      */

      return compile(Collections.singletonList(srcFile));

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @NonNull
  static Path compile(List<Path> srcFiles) {

    // create key for temp dir / caching
    StringBuilder sb = new StringBuilder();
    for (Path srcFile : srcFiles) {
      sb.append(srcFile);
    }
    String concatenatedFileNames = sb.toString();

    try {
      Path binDirpath = getTempDirectory(concatenatedFileNames).resolve("bin/");
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
      try (StandardJavaFileManager fileManager =
          compiler.getStandardFileManager(null, null, null)) {
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(binDir));

        File[] files = new File[srcFiles.size()];
        srcFiles.stream().map(Path::toFile).toList().toArray(files);
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
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
