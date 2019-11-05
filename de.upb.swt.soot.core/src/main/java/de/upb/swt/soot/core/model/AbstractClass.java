package de.upb.swt.soot.core.model;

import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.signatures.MethodSubSignature;
import de.upb.swt.soot.core.signatures.Signature;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.views.View;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Abstract class represents a class/module lives in {@link View}. It may have different
 * implementations, since we want to support multiple languages. An abstract class must be uniquely
 * identified by its {@link Signature}.
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
  public Optional<? extends Method> getMethod(@Nonnull MethodSignature signature) {
    return this.getMethods().stream().filter(m -> m.getSignature().equals(signature)).findAny();
  }

  @Nonnull
  public Optional<? extends Method> getMethod(@Nonnull MethodSubSignature subSignature) {
    return getMethods().stream()
        .filter(m -> m.getSignature().getSubSignature().equals(subSignature))
        .findAny();
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
