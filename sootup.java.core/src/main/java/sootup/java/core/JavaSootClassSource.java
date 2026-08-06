package sootup.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Markus Schmidt
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

import com.google.common.base.Objects;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.frontend.PathbasedClassProvider;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;

public abstract class JavaSootClassSource implements SootClassSource {

  // holds information about the class
  protected final AnalysisInputLocation analysisInputLocation;
  // the classType that identifies the containing class information
  protected ClassType classSignature;
  // holds information about the specific data unit where the information about a class is stored
  protected final Path sourcePath;

  /**
   * Creates and a {@link SootClassSource} for a specific source file. The file should be passed as
   * {@link Path} and can be located in an arbitrary {@link java.nio.file.FileSystem}.
   * Implementations should use {@link java.nio.file.Files#newInputStream(Path, OpenOption...)}
   * (Path, OpenOption...)} to access the file.
   *
   * @param inputLocation The {@link AnalysisInputLocation} that holds the given file
   * @param sourcePath Path to the source file of the to-be-created {@link SootClassSource}. The
   *     given path has to exist and requires to be handled by this {@link PathbasedClassProvider}.
   *     Implementations might double check this if wanted.
   * @param classSignature the signature that has been used to resolve this class
   */
  public JavaSootClassSource(
      @NonNull AnalysisInputLocation inputLocation,
      @NonNull ClassType classSignature,
      @NonNull Path sourcePath) {
    this.analysisInputLocation = inputLocation;
    this.classSignature = classSignature;
    this.sourcePath = sourcePath;
  }

  public JavaSootClassSource(SootClassSource delegate) {
    this.analysisInputLocation = delegate.getAnalysisInputLocation();
    this.classSignature = delegate.getClassType();
    this.sourcePath = delegate.getSourcePath();
  }

  protected abstract Iterable<AnnotationUsage> resolveAnnotations();

  @Override
  @NonNull
  public JavaSootClass buildClass(@NonNull SourceType sourceType) {
    return new JavaSootClass(this, sourceType);
  }

  @Override
  public ClassType getClassType() {
    return classSignature;
  }

  public AnalysisInputLocation getAnalysisInputLocation() {
    return analysisInputLocation;
  }

  public Path getSourcePath() {
    return sourcePath;
  }

  /**
   * Even if a the signature changes, the classource remains the same, e.g., if it is associated to
   * an automatic module s
   *
   * @param o the object to compare with
   * @return both objects are logically equal
   */
  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    JavaSootClassSource that = (JavaSootClassSource) o;
    return Objects.equal(analysisInputLocation, that.analysisInputLocation)
        && Objects.equal(sourcePath, that.sourcePath);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(analysisInputLocation, sourcePath);
  }
}
