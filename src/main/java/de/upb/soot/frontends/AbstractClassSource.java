package de.upb.soot.frontends;

import com.google.common.base.Objects;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.types.JavaClassType;
import java.nio.file.Path;
import javax.annotation.Nullable;

public class AbstractClassSource {
  protected final INamespace srcNamespace;
  protected final Path sourcePath;
  // TODO: AD unfortunately I need to change it in the ModuleFinder, since I only know a module's
  // name after resolving its
  // module-info.class
  protected JavaClassType classSignature;

  public AbstractClassSource(
      INamespace srcNamespace, JavaClassType classSignature, Path sourcePath) {
    this.srcNamespace = srcNamespace;
    this.classSignature = classSignature;
    this.sourcePath = sourcePath;
  }

  public JavaClassType getClassType() {
    return classSignature;
  }

  public Path getSourcePath() {
    return sourcePath;
  }

  public void setClassSignature(JavaClassType classSignature) {
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
