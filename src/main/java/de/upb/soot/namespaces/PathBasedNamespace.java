package de.upb.soot.namespaces;

import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.IClassProvider;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.util.Utils;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
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
 * Base class for {@link INamespace}s that can be located by a {@link Path} object.
 *
 * @author Manuel Benz created on 22.05.18
 */
public abstract class PathBasedNamespace extends AbstractNamespace {
  protected final Path path;

  private PathBasedNamespace(@Nonnull Path path) {
    this(path, getDefaultClassProvider());
  }

  private PathBasedNamespace(@Nonnull Path path, @Nonnull IClassProvider classProvider) {
    super(classProvider);
    this.path = path;
  }

  /**
   * Creates a {@link PathBasedNamespace} depending on the given {@link Path}, e.g., differs between
   * directories, archives (and possibly network path's in the future).
   *
   * @param path The path to search in
   * @return A {@link PathBasedNamespace} implementation dependent on the given {@link Path}'s
   *     {@link FileSystem}
   */
  public static @Nonnull PathBasedNamespace createForClassContainer(@Nonnull Path path) {
    if (Files.isDirectory(path)) {
      return new DirectoryBasedNamespace(path);
    } else if (PathUtils.isArchive(path)) {
      return new ArchiveBasedNamespace(path);
    } else {
      throw new IllegalArgumentException(
          "Path has to be pointing to the root of a class container, e.g. directory, jar, zip, apk, etc.");
    }
  }

  protected @Nonnull Collection<ClassSource> walkDirectory(
      @Nonnull Path dirPath, @Nonnull SignatureFactory factory) {
    try {
      final FileType handledFileType = classProvider.getHandledFileType();

      return Files.walk(dirPath)
          .filter(filePath -> PathUtils.hasExtension(filePath, handledFileType))
          .flatMap(
              p ->
                  Utils.optionalToStream(
                      Optional.of(classProvider.createClassSource(this, p, factory.fromPath(p)))))
          .collect(Collectors.toList());

    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  protected @Nonnull Optional<ClassSource> getClassSourceInternal(
      @Nonnull JavaClassSignature signature, @Nonnull Path path) {
    Path pathToClass =
        path.resolve(signature.toPath(classProvider.getHandledFileType(), path.getFileSystem()));

    if (!Files.exists(pathToClass)) {
      return Optional.empty();
    }

    return Optional.of(classProvider.createClassSource(this, pathToClass, signature));
  }

  private static final class DirectoryBasedNamespace extends PathBasedNamespace {

    private DirectoryBasedNamespace(@Nonnull Path path) {
      super(path);
    }

    @Override
    public @Nonnull Collection<ClassSource> getClassSources(@Nonnull SignatureFactory factory) {
      return walkDirectory(path, factory);
    }

    @Override
    public @Nonnull Optional<ClassSource> getClassSource(@Nonnull JavaClassSignature signature) {
      return getClassSourceInternal(signature, path);
    }
  }

  private static final class ArchiveBasedNamespace extends PathBasedNamespace {

    private ArchiveBasedNamespace(@Nonnull Path path) {
      super(path);
    }

    @Override
    public @Nonnull Optional<ClassSource> getClassSource(@Nonnull JavaClassSignature signature) {
      try (FileSystem fs = FileSystems.newFileSystem(path, null)) {
        final Path archiveRoot = fs.getPath("/");
        return getClassSourceInternal(signature, archiveRoot);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public @Nonnull Collection<ClassSource> getClassSources(@Nonnull SignatureFactory factory) {
      try (FileSystem fs = FileSystems.newFileSystem(path, null)) {
        final Path archiveRoot = fs.getPath("/");
        return walkDirectory(archiveRoot, factory);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
