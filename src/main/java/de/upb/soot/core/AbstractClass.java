package de.upb.soot.core;

import de.upb.soot.frontends.ClassSource;
import de.upb.soot.signatures.ISignature;
import de.upb.soot.views.IView;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Abstract class represents a class/module lives in {@link IView}. It may have different
 * implementations, since we want to support multiple languages.
 *
 * @author Linghui Luo
 */
public abstract class AbstractClass {

  protected final ClassSource classSource;

  public AbstractClass(ClassSource cs) {
    this.classSource = cs;
  }

  public ClassSource getClassSource() {
    return classSource;
  }

  public abstract String getName();

  public abstract ISignature getSignature();

  @Nonnull
  public Optional<? extends IMethod> getMethod(@Nonnull ISignature signature) {
    return this.getMethods().stream().filter(m -> m.getSignature().equals(signature)).findAny();
  }

  @Nonnull
  public abstract Set<? extends IMethod> getMethods();

  @Nonnull
  public Optional<? extends IField> getField(@Nonnull ISignature signature) {
    return this.getFields().stream().filter(f -> f.getSignature().equals(signature)).findAny();
  }

  @Nonnull
  public abstract Set<? extends IField> getFields();
}
