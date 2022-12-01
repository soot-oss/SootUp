package sootup.core.frontend;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Linghui Luo, Markus Schmidt, Andreas Dann and others
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
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.AbstractClass;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.signatures.Signature;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * AbstractClassSource represents a Compilation Unit (Interpretation Unit for interpreted
 * languages). e.g. its connecting a file with source(code) to a {@link Signature} that a {@link
 * View} can resolve.
 */
public abstract class AbstractClassSource<T extends AbstractClass> {

  // holds information about the class
  protected final AnalysisInputLocation<? extends SootClass<?>> classSource;
  // holds information about the specific data unit where the information about a class is stored
  protected final Path sourcePath;
  // the classType that identifies the containing class information
  protected ClassType classSignature;

  public AbstractClassSource(
      @Nonnull AnalysisInputLocation<? extends SootClass<?>> classSource,
      @Nonnull ClassType classSignature,
      @Nonnull Path sourcePath) {
    this.classSource = classSource;
    this.classSignature = classSignature;
    this.sourcePath = sourcePath;
  }

  /**
   * @param sourceType instantiates the Subclass of AbstractClassSource to create a *SootClass
   * @return a *SootClass
   */
  public abstract T buildClass(@Nonnull SourceType sourceType);

  public ClassType getClassType() {
    return classSignature;
  }

  public AnalysisInputLocation<? extends SootClass<?>> getClassSource() {
    return classSource;
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
    AbstractClassSource<? extends SootClass<?>> that =
        (AbstractClassSource<? extends SootClass<?>>) o;
    return Objects.equal(classSource, that.classSource)
        && Objects.equal(sourcePath, that.sourcePath);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(classSource, sourcePath);
  }
}
