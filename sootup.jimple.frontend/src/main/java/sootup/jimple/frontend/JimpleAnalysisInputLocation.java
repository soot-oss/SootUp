package sootup.jimple.frontend;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 1997 - 2024 Raja Vallée-Rai and others
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.ClassProvider;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.FileType;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.util.PathUtils;
import sootup.core.util.StreamUtils;
import sootup.core.views.View;

/**
 * @author Markus Schmidt
 */
public class JimpleAnalysisInputLocation implements AnalysisInputLocation {
  final Path path;

  /** Variable to track if user has specified the SourceType. By default, it will be set to null. */
  private final SourceType srcType;

  @NonNull private final List<BodyInterceptor> bodyInterceptors;

  public JimpleAnalysisInputLocation(@NonNull Path path) {
    this(path, SourceType.Application, Collections.emptyList());
  }

  public JimpleAnalysisInputLocation(@NonNull Path path, @Nullable SourceType srcType) {
    this(path, srcType, Collections.emptyList());
  }

  public JimpleAnalysisInputLocation(
      @NonNull Path path,
      @Nullable SourceType srcType,
      @NonNull List<BodyInterceptor> bodyInterceptors) {
    if (!Files.exists(path)) {
      throw new IllegalArgumentException(
          "The configured path '"
              + path
              + "' pointing to '"
              + path.toAbsolutePath()
              + "' does not exist.");
    }
    this.bodyInterceptors = bodyInterceptors;
    this.path = path;
    this.srcType = srcType;
  }

  @NonNull
  @Override
  public SourceType getSourceType() {
    return srcType;
  }

  @NonNull
  @Override
  public List<BodyInterceptor> getBodyInterceptors() {
    return bodyInterceptors;
  }

  /**
   * @return Autoclosable needs to be closed!
   */
  @NonNull Stream<SootClassSource> walkDirectory(
      @NonNull Path dirPath,
      @NonNull IdentifierFactory factory,
      @NonNull ClassProvider classProvider) {

    try {
      return Files.walk(path)
          .filter(filePath -> PathUtils.hasExtension(filePath, FileType.JIMPLE))
          .flatMap(
              p -> {
                String fullyQualifiedName =
                    FilenameUtils.removeExtension(
                        p.subpath(path.getNameCount(), p.getNameCount())
                            .toString()
                            .replace(p.getFileSystem().getSeparator(), "."));

                return StreamUtils.optionalToStream(
                    classProvider.createClassSource(
                        this, p, factory.getClassType(fullyQualifiedName)));
              });
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  @NonNull
  public Stream<SootClassSource> getClassSources(@NonNull View view) {
    // TODO: dont create a new CLassProvider every time
    return walkDirectory(
        path, view.getIdentifierFactory(), new JimpleClassProvider(bodyInterceptors, view));
  }

  @Override
  @NonNull
  public Optional<SootClassSource> getClassSource(@NonNull ClassType type, @NonNull View view) {
    final JimpleClassProvider classProvider = new JimpleClassProvider(bodyInterceptors, view);

    final String ext = classProvider.getHandledFileType().toString().toLowerCase();

    // is file under path:  with name package.subpackage.class.jimple
    Path pathToClass = path.resolve(type.getFullyQualifiedName() + "." + ext);
    if (!Files.exists(pathToClass)) {
      // is file under path with dir structure: package/subpackage/className.jimple
      pathToClass =
          path.resolve(
              type.getPackageName().toString().replace('.', File.separatorChar)
                  + File.separator
                  + type.getClassName()
                  + "."
                  + ext);
      if (!Files.exists(pathToClass)) {
        return Optional.empty();
      }
    }

    return classProvider.createClassSource(this, pathToClass, type);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof JimpleAnalysisInputLocation)) {
      return false;
    }
    return path.equals(((JimpleAnalysisInputLocation) o).path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path);
  }
}
