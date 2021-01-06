package de.upb.swt.soot.core.frontend;
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
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.signatures.Signature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.views.View;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * AbstractClassSource represents a Compilation Unit (Interpretation Unit for interpreted
 * languages). e.g. its connecting a file with source(code) to a {@link Signature} that a {@link
 * View} can resolve.
 */
public abstract class AbstractClassSource<T extends SootClass> {
  // TODO: [ms] I dont see the necessity of the AnalysisInputLocation in this class; maybe not even
  // for sourcepath
  protected final AnalysisInputLocation srcNamespace;
  protected final Path sourcePath;
  protected ClassType classSignature;

  public AbstractClassSource(
      @Nonnull AnalysisInputLocation srcNamespace,
      @Nonnull ClassType classSignature,
      @Nonnull Path sourcePath) {
    this.srcNamespace = srcNamespace;
    this.classSignature = classSignature;
    this.sourcePath = sourcePath;
  }

  public ClassType getClassType() {
    return classSignature;
  }

  public abstract T buildClass(@Nonnull SourceType sourceType);

  public Path getSourcePath() {
    return sourcePath;
  }

  public void setClassSignature(ClassType classSignature) {
    this.classSignature = classSignature;
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
    AbstractClassSource that = (AbstractClassSource) o;
    return Objects.equal(srcNamespace, that.srcNamespace)
        && Objects.equal(sourcePath, that.sourcePath);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(srcNamespace, sourcePath);
  }
}
