package sootup.java.bytecode.frontend.inputlocation;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018-2020 Manuel Benz, Christian Brüggemann, Markus Schmidt and others
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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.interceptors.BytecodeBodyInterceptors;
import sootup.java.bytecode.frontend.conversion.AsmJavaClassProvider;
import sootup.java.core.JavaSootClassSource;
import sootup.java.core.types.JavaClassType;

public class ArchiveBasedAnalysisInputLocation extends PathBasedAnalysisInputLocation {

  // We cache the FileSystem instances as their creation is expensive.
  // The Guava Cache is thread-safe (see JavaDoc of LoadingCache) hence this
  // cache can be safely shared in a static variable.
  protected static final LoadingCache<Path, FileSystem> fileSystemCache =
      CacheBuilder.newBuilder()
          .weakValues()
          .removalListener(
              (RemovalNotification<Path, FileSystem> removalNotification) -> {
                try {
                  FileSystem value = removalNotification.getValue();
                  if (value != null) {
                    value.close();
                  }
                } catch (IOException e) {
                  throw new RuntimeException(
                      "Could not close file system of " + removalNotification.getKey(), e);
                }
              })
          // .expireAfterAccess(1, TimeUnit.SECONDS)
          .build(
              CacheLoader.from(
                  path -> {
                    try {
                      return FileSystems.newFileSystem(
                          Objects.requireNonNull(path), (ClassLoader) null);
                    } catch (IOException e) {
                      throw new RuntimeException("Could not open file system of " + path, e);
                    }
                  }));

  public ArchiveBasedAnalysisInputLocation(@NonNull Path path, @NonNull SourceType srcType) {
    this(path, srcType, BytecodeBodyInterceptors.Default.getBodyInterceptors());
  }

  public ArchiveBasedAnalysisInputLocation(
      @NonNull Path path,
      @NonNull SourceType srcType,
      @NonNull List<BodyInterceptor> bodyInterceptors) {
    this(path, srcType, bodyInterceptors, Collections.emptyList());
  }

  public ArchiveBasedAnalysisInputLocation(
      Path path,
      SourceType srcType,
      List<BodyInterceptor> bodyInterceptors,
      Collection<Path> ignoredPaths) {
    super(path, srcType, bodyInterceptors, ignoredPaths);
  }

  @Override
  @NonNull
  public Optional<JavaSootClassSource> getClassSource(@NonNull ClassType type, @NonNull View view) {
    try {
      FileSystem fs = fileSystemCache.get(path);
      final Path archiveRoot = fs.getPath("/");
      // TODO: dont create a new AsmJavaClassProvider all the time!
      return getClassSourceInternal(
          (JavaClassType) type, archiveRoot, new AsmJavaClassProvider(view));
    } catch (ExecutionException e) {
      throw new RuntimeException("Failed to retrieve file system from cache for " + path, e);
    }
  }

  /** returns a Autocloseable resource that must be closed! */
  @Override
  @NonNull
  public Stream<JavaSootClassSource> getClassSources(@NonNull View view) {
    try {
      FileSystem fs = fileSystemCache.get(path);
      final Path archiveRoot = fs.getPath("/");
      // TODO: dont create a new AsmJavaClassProvider all the time!
      return walkDirectory(
          archiveRoot, view.getIdentifierFactory(), new AsmJavaClassProvider(view));
    } catch (ExecutionException e) {
      throw new RuntimeException("Failed to retrieve file system from cache for " + path, e);
    }
  }
}
