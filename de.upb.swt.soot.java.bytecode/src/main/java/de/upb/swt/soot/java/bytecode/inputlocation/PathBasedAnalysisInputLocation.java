package de.upb.swt.soot.java.bytecode.inputlocation;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.PathUtils;
import de.upb.swt.soot.core.util.StreamUtils;
import de.upb.swt.soot.java.bytecode.frontend.AsmJavaClassProvider;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 22.05.2018 Manuel Benz
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

/**
 * Base class for {@link PathBasedAnalysisInputLocation}s that can be located by a {@link Path}
 * object.
 *
 * @author Manuel Benz created on 22.05.18
 */
public abstract class PathBasedAnalysisInputLocation implements BytecodeAnalysisInputLocation {
  protected final Path path;
  protected static List<PathBasedAnalysisInputLocation> allJars = new ArrayList<>();
  protected static List<String> allClasses = new ArrayList<>();

  private PathBasedAnalysisInputLocation(@Nonnull Path path) {
    this.path = path;
  }

  /**
   * Creates a {@link PathBasedAnalysisInputLocation} depending on the given {@link Path}, e.g.,
   * differs between directories, archives (and possibly network path's in the future).
   *
   * @param path The path to search in
   * @return A {@link PathBasedAnalysisInputLocation} implementation dependent on the given {@link
   *     Path}'s {@link FileSystem}
   */
  public static @Nonnull PathBasedAnalysisInputLocation createForClassContainer(
      @Nonnull Path path) {
    System.out.println("inside createForClassContainer"); // TODO Debug
    System.out.println("the file at " + path + " is " + path.getFileName()); // TODO Debug

    if (Files.isDirectory(path)) {
      return new DirectoryBasedAnalysisInputLocation(path);
    } else if (PathUtils.isArchive(path)) {
      /* TODO create new namespace for the jar file, packed in the war and load all classes from that jar file
       *   walkDirectory() for such all methods*/

      System.out.println("this is an archive file"); // TODO Debug
      if (PathUtils.hasExtension(path, FileType.WAR)) {
        // listAllJars(path);
        return new WarFileBasedAnalyisInputLocation(path);
      }
      return new ArchiveBasedAnalysisInputLocation(path);
    } else {
      throw new IllegalArgumentException(
          "Path has to be pointing to the root of a class container, e.g. directory, jar, zip, apk, war etc.");
    }
  }

  /**
   * Lists all jars at the given {@link path}
   *
   * @param path the path for the war file
   */
  public static void listAllJars(@Nonnull Path path) {
    String line;

    try {
      Process ps = Runtime.getRuntime().exec(new String[] {"jar", "-tvf", path.toString()});
      ps.waitFor();
      BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
      while ((line = br.readLine()) != null) {
        if (line.contains("jar")) {
          String[] arr = line.split(" ");
          System.out.println(path + arr[9]);
          allJars.add(new ArchiveBasedAnalysisInputLocation(Paths.get(arr[9])));
        } else if (line.contains(".class")) {
          String[] arr = line.split(" ");
          System.out.println(arr[9]);
          allClasses.add(arr[9]);
        }
      }
    } catch (InterruptedException | IOException e) {
      throw new RuntimeException("Issues in listing the contents of the war file");
    }
    System.out.println("listAllJars() completed ");
  }

  @Nonnull
  Collection<? extends AbstractClassSource> walkDirectory(
      @Nonnull Path dirPath, @Nonnull IdentifierFactory factory, ClassProvider classProvider) {
    try {
      final FileType handledFileType = classProvider.getHandledFileType();
      System.out.println(
          "inside walkDirectory for "
              + dirPath
              + " for file with type "
              + handledFileType); // TODO Debug

      System.out.println(
          "------->"
              + Files.walk(dirPath)
                  .filter(filePath -> PathUtils.hasExtension(filePath, handledFileType))
                  .collect(Collectors.toList()));

      return Files.walk(dirPath)
          .filter(filePath -> PathUtils.hasExtension(filePath, handledFileType))
          .flatMap(
              p ->
                  StreamUtils.optionalToStream(
                      Optional.of(classProvider.createClassSource(this, p, factory.fromPath(p)))))
          .collect(Collectors.toList());

    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Nonnull
  Collection<Path> walkDirectoryForJars(@Nonnull Path dirPath) throws IOException {
    System.out.println("inside walkDirectoryForJars");
    return Files.walk(dirPath)
        .filter(filePath -> PathUtils.hasExtension(filePath, FileType.WAR))
        .flatMap(p1 -> StreamUtils.optionalToStream(Optional.of(p1)))
        .collect(Collectors.toList());
    /*return Files.walk(dirPath)
            .filter(filePath -> PathUtils.hasExtension(filePath, FileType.JAR))
            .flatMap(path1 -> StreamUtils.optionalToStream(Optional.of(path1)))
    .collect(Collectors.toList());*/
  }

  @Nonnull
  Optional<? extends AbstractClassSource> getClassSourceInternal(
      @Nonnull JavaClassType signature, @Nonnull Path path, @Nonnull ClassProvider classProvider) {
    Path pathToClass =
        path.resolve(signature.toPath(classProvider.getHandledFileType(), path.getFileSystem()));

    if (!Files.exists(pathToClass)) {
      return Optional.empty();
    }

    System.out.println(
        "getClassSourceInternal ->>"
            + Optional.of(
                classProvider.createClassSource(this, pathToClass, signature))); // TODO Debug

    return Optional.of(classProvider.createClassSource(this, pathToClass, signature));
  }

  ClassProvider buildClassProvider(@Nonnull ClassLoadingOptions classLoadingOptions) {
    List<BodyInterceptor> bodyInterceptors = classLoadingOptions.getBodyInterceptors();
    return new AsmJavaClassProvider(bodyInterceptors);
  }

  private static final class DirectoryBasedAnalysisInputLocation
      extends PathBasedAnalysisInputLocation {

    private DirectoryBasedAnalysisInputLocation(@Nonnull Path path) {
      super(path);
    }

    @Override
    public @Nonnull Collection<? extends AbstractClassSource> getClassSources(
        @Nonnull IdentifierFactory identifierFactory,
        @Nonnull ClassLoadingOptions classLoadingOptions) {
      return walkDirectory(path, identifierFactory, buildClassProvider(classLoadingOptions));
    }

    @Override
    public @Nonnull Optional<? extends AbstractClassSource> getClassSource(
        @Nonnull ClassType type, @Nonnull ClassLoadingOptions classLoadingOptions) {
      return getClassSourceInternal(
          (JavaClassType) type, path, buildClassProvider(classLoadingOptions));
    }
  }

  private static final class ArchiveBasedAnalysisInputLocation
      extends PathBasedAnalysisInputLocation {

    // We cache the FileSystem instances as their creation is expensive.
    // The Guava Cache is thread-safe (see JavaDoc of LoadingCache) hence this
    // cache can be safely shared in a static variable.
    private static final LoadingCache<Path, FileSystem> fileSystemCache =
        CacheBuilder.newBuilder()
            .removalListener(
                (RemovalNotification<Path, FileSystem> removalNotification) -> {
                  try {
                    removalNotification.getValue().close();
                  } catch (IOException e) {
                    throw new RuntimeException(
                        "Could not close file system of " + removalNotification.getKey(), e);
                  }
                })
            .expireAfterAccess(1, TimeUnit.SECONDS)
            .build(
                CacheLoader.from(
                    path -> {
                      try {
                        return FileSystems.newFileSystem(Objects.requireNonNull(path), null);
                      } catch (IOException e) {
                        throw new RuntimeException("Could not open file system of " + path, e);
                      }
                    }));

    private ArchiveBasedAnalysisInputLocation(@Nonnull Path path) {
      super(path);
    }

    @Override
    public @Nonnull Optional<? extends AbstractClassSource> getClassSource(
        @Nonnull ClassType type, @Nonnull ClassLoadingOptions classLoadingOptions) {
      try {
        FileSystem fs = fileSystemCache.get(path);
        final Path archiveRoot = fs.getPath("/");
        return getClassSourceInternal(
            (JavaClassType) type, archiveRoot, buildClassProvider(classLoadingOptions));
      } catch (ExecutionException e) {
        throw new RuntimeException("Failed to retrieve file system from cache for " + path, e);
      }
    }

    @Override
    public @Nonnull Collection<? extends AbstractClassSource> getClassSources(
        @Nonnull IdentifierFactory identifierFactory,
        @Nonnull ClassLoadingOptions classLoadingOptions) {
      try (FileSystem fs = FileSystems.newFileSystem(path, null)) {
        final Path archiveRoot = fs.getPath("/");
        System.out.println("walkDirectory called for " + archiveRoot + " at " + path); // TODO Debug
        return walkDirectory(
            archiveRoot, identifierFactory, buildClassProvider(classLoadingOptions));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static final class WarFileBasedAnalyisInputLocation
      extends PathBasedAnalysisInputLocation {

    // We cache the FileSystem instances as their creation is expensive.
    // The Guava Cache is thread-safe (see JavaDoc of LoadingCache) hence this
    // cache can be safely shared in a static variable.
    private static final LoadingCache<Path, FileSystem> fileSystemCache =
        CacheBuilder.newBuilder()
            .removalListener(
                (RemovalNotification<Path, FileSystem> removalNotification) -> {
                  try {
                    removalNotification.getValue().close();
                  } catch (IOException e) {
                    throw new RuntimeException(
                        "Could not close file system of " + removalNotification.getKey(), e);
                  }
                })
            .expireAfterAccess(1, TimeUnit.SECONDS)
            .build(
                CacheLoader.from(
                    path -> {
                      try {
                        return FileSystems.newFileSystem(Objects.requireNonNull(path), null);
                      } catch (IOException e) {
                        throw new RuntimeException("Could not open file system of " + path, e);
                      }
                    }));

    private WarFileBasedAnalyisInputLocation(@Nonnull Path path) {
      super(path);
    }

    @Override
    public @Nonnull Optional<? extends AbstractClassSource> getClassSource(
        @Nonnull ClassType type, @Nonnull ClassLoadingOptions classLoadingOptions) {
      try {
        FileSystem fs = fileSystemCache.get(path);
        final Path archiveRoot = fs.getPath("/");
        return getClassSourceInternal(
            (JavaClassType) type, archiveRoot, buildClassProvider(classLoadingOptions));
      } catch (ExecutionException e) {
        throw new RuntimeException("Failed to retrieve file system from cache for " + path, e);
      }
    }

    @Override
    public @Nonnull Collection<? extends AbstractClassSource> getClassSources(
        @Nonnull IdentifierFactory identifierFactory,
        @Nonnull ClassLoadingOptions classLoadingOptions) {

      System.out.println(
          "inside getClassSources of WarFileBasedAnalyisInputLocation"); // TODO Debug
      Collection<? extends AbstractClassSource> classesFromWar = null;
      try {
        System.out.println("walkDirectoryForJars called for \"/\" " + " at " + path); // TODO Debug
        Collection<Path> jarsFromPath = walkDirectoryForJars(path);
        System.out.println("Printing the paths of all jars >>" + jarsFromPath); // TODO Debug
        if (jarsFromPath.isEmpty()) System.out.println("No jars found");
        ;
        for (Path path : jarsFromPath) {

          System.out.println("-->" + path.getFileName());
          Collection<? extends AbstractClassSource> allClassesFromJar = null;
          try (FileSystem fs = FileSystems.newFileSystem(path, null)) {
            final Path archiveRoot = fs.getPath("/");
            System.out.println(
                "WarFileBasedAnalyisInputLocation: walkDirectory called for "
                    + archiveRoot); // TODO Debug
            allClassesFromJar =
                walkDirectory(
                    archiveRoot, identifierFactory, buildClassProvider(classLoadingOptions));
            System.out.println(allClassesFromJar);

          } catch (IOException e) {
            e.getMessage();
          }
        }

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return classesFromWar;
    }

    public void loadJar(
        @Nonnull IdentifierFactory identifierFactory,
        @Nonnull ClassLoadingOptions classLoadingOptions,
        @Nonnull ArrayList<PathBasedAnalysisInputLocation> allJars) {}
  }
}
