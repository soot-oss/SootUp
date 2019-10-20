package de.upb.swt.soot.core.frontend;

import com.google.common.base.Objects;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.model.AbstractClass;
import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.types.ClassType;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractClassSource {
  protected final AnalysisInputLocation srcNamespace;
  protected final Path sourcePath;
  // TODO: AD unfortunately I need to change it in the ModuleFinder, since I only know a module's
  // name after resolving its module-info.class
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

  public abstract AbstractClass<? extends AbstractClassSource> buildClass(
      @Nonnull SourceType sourceType);

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
