package de.upb.swt.soot.java.core.jimple.basic;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.core.AnnotationType;
import java.util.Objects;
import javax.annotation.Nonnull;

public class JavaLocal extends Local {

  // TODO: [ms] add to JavaJimple
  // TODO: [ms] make use of this class in both Java Frontends

  @Nonnull private final Iterable<AnnotationType> annotations;

  /**
   * Constructs a JimpleLocal of the given name and type.
   *
   * @param name
   * @param type
   */
  public JavaLocal(
      @Nonnull String name, @Nonnull Type type, @Nonnull Iterable<AnnotationType> annotations) {
    super(name, type);
    this.annotations = annotations;
  }

  @Nonnull
  public Iterable<AnnotationType> getAnnotations() {
    return annotations;
  }

  @Override
  public boolean equals(Object o) {
    return equivTo(o) && ((JavaLocal) o).getAnnotations().equals(getAnnotations());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getType(), getAnnotations());
  }

  @Nonnull
  public Local withName(@Nonnull String name) {
    return new JavaLocal(name, getType(), getAnnotations());
  }

  @Nonnull
  public Local withType(@Nonnull Type type) {
    return new JavaLocal(getName(), type, getAnnotations());
  }

  @Nonnull
  public Local withAnnotations(@Nonnull Iterable<AnnotationType> annotations) {
    return new JavaLocal(getName(), getType(), annotations);
  }
}
