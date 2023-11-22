package sootup.java.bytecode.inputlocation;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.frontend.AbstractClassSource;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.bytecode.frontend.AsmJavaClassProvider;
import sootup.java.core.JavaSootClass;
import sootup.java.core.types.JavaClassType;

public class ArchiveBasedAnalysisInputLocation extends PathBasedAnalysisInputLocation {

  // We cache the FileSystem instances as their creation is expensive.
  // The Guava Cache is thread-safe (see JavaDoc of LoadingCache) hence this
  // cache can be safely shared in a static variable.
  protected static final LoadingCache<Path, FileSystem> fileSystemCache =
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
                      return FileSystems.newFileSystem(
                          Objects.requireNonNull(path), (ClassLoader) null);
                    } catch (IOException e) {
                      throw new RuntimeException("Could not open file system of " + path, e);
                    }
                  }));

  public ArchiveBasedAnalysisInputLocation(@Nonnull Path path, @Nullable SourceType srcType) {
    super(path, srcType);
  }

  @Override
  @Nonnull
  public Optional<? extends AbstractClassSource<JavaSootClass>> getClassSource(
      @Nonnull ClassType type, @Nonnull View<?> view) {
    try {
      FileSystem fs = fileSystemCache.get(path);
      final Path archiveRoot = fs.getPath("/");
      return getClassSourceInternal(
          (JavaClassType) type, archiveRoot, new AsmJavaClassProvider(view));
    } catch (ExecutionException e) {
      throw new RuntimeException("Failed to retrieve file system from cache for " + path, e);
    }
  }

  @Override
  @Nonnull
  public Collection<? extends AbstractClassSource<JavaSootClass>> getClassSources(
      @Nonnull View<?> view) {
    // we don't use the filesystem cache here as it could close the filesystem after the timeout
    // while we are still iterating
    try (FileSystem fs = FileSystems.newFileSystem(path, (ClassLoader) null)) {
      final Path archiveRoot = fs.getPath("/");
      return walkDirectory(
          archiveRoot, view.getIdentifierFactory(), new AsmJavaClassProvider(view));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
