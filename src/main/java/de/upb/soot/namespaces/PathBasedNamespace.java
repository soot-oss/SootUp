package de.upb.soot.namespaces;

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

import de.upb.soot.Utils;
import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Base class for {@link INamespace}s that can be located by a {@link Path} object.
 *
 * @author Manuel Benz created on 22.05.18
 */
public abstract class PathBasedNamespace extends AbstractNamespace {
  protected final Path path;

  private PathBasedNamespace(IClassProvider classProvider, Path path) {
    super(classProvider);
    this.path = path;
  }

  /**
   * Creates a {@link PathBasedNamespace} depending on the given {@link Path}, e.g., differs between directories, archives
   * (and possibly network path's in the future).
   * 
   * @param classProvider
   *          The {@link IClassProvider} for generating {@link ClassSource}es out of the found files on the given path
   * @param path
   *          The path to search in
   * @return A {@link PathBasedNamespace} implementation dependent on the given {@link Path}'s {@link FileSystem}
   */
  public static PathBasedNamespace createForClassContainer(IClassProvider classProvider, Path path) {
    if (Files.isDirectory(path)) {
      return new DirectoryBasedNamespace(classProvider, path);
    } else if (PathUtils.isArchive(path)) {
      return new ArchiveBasedNamespace(classProvider, path);
    } else {
      throw new IllegalArgumentException(
          "Path has to be pointing to the root of a class container, e.g. directory, jar, zip, apk, etc.");
    }
  }

  protected Collection<ClassSource> walkDirectory(Path dirPath) {
    try {
      final FileType handledFileType = classProvider.getHandledFileType();
      return Files.walk(dirPath).filter(filePath -> PathUtils.hasExtension(filePath, handledFileType))
          .flatMap(p -> Utils.optionalToStream(classProvider.getClass(this, p))).collect(Collectors.toList());
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  protected Optional<ClassSource> getClassSourceInternal(ClassSignature signature, Path path) {
    Path pathToClass = path.resolve(signature.toPath(classProvider.getHandledFileType(), path.getFileSystem()));

    if (!Files.exists(pathToClass)) {
      return Optional.empty();
    }

    return classProvider.getClass(this, pathToClass);
  }

  private static final class DirectoryBasedNamespace extends PathBasedNamespace {

    private DirectoryBasedNamespace(IClassProvider classProvider, Path path) {
      super(classProvider, path);
    }

    @Override
    public Collection<ClassSource> getClassSources() {
      return walkDirectory(path);
    }

    @Override
    public Optional<ClassSource> getClassSource(ClassSignature signature) {
      return getClassSourceInternal(signature, path);
    }
  }

  private static final class ArchiveBasedNamespace extends PathBasedNamespace {

    private ArchiveBasedNamespace(IClassProvider classProvider, Path path) {
      super(classProvider, path);
    }

    @Override
    public Optional<ClassSource> getClassSource(ClassSignature signature) {
      try (FileSystem fs = FileSystems.newFileSystem(path, null)) {
        final Path archiveRoot = fs.getPath("/");
        return getClassSourceInternal(signature, archiveRoot);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    protected Collection<ClassSource> getClassSources() {
      try (FileSystem fs = FileSystems.newFileSystem(path, null)) {
        final Path archiveRoot = fs.getPath("/");
        return walkDirectory(archiveRoot);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
