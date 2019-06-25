package de.upb.soot.core;

import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.signatures.Signature;
import de.upb.soot.types.Type;
import de.upb.soot.views.View;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Abstract class represents a class/module lives in {@link View}. It may have different
 * implementations, since we want to support multiple languages.
 *
 * @author Linghui Luo
 */
public abstract class AbstractClass<T extends AbstractClassSource> {

  protected final T classSource;

  public AbstractClass(T cs) {
    this.classSource = cs;
  }

  public AbstractClassSource getClassSource() {
    return classSource;
  }

  public abstract String getName();

  public abstract Type getType();

  @Nonnull
  public Optional<? extends Method> getMethod(@Nonnull Signature signature) {
    return this.getMethods().stream().filter(m -> m.getSignature().equals(signature)).findAny();
  }

  @Nonnull
  public abstract Set<? extends Method> getMethods();

  @Nonnull
  public Optional<? extends Field> getField(@Nonnull Signature signature) {
    return this.getFields().stream().filter(f -> f.getSignature().equals(signature)).findAny();
  }

  @Nonnull
  public abstract Set<? extends Field> getFields();
}
