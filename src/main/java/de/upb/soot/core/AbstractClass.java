package de.upb.soot.core;

import de.upb.soot.namespaces.classprovider.AbstractClassSource;
import de.upb.soot.signatures.ISignature;
import de.upb.soot.views.IView;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Abstract class represents a class/module lives in {@link IView}. It may have different implementations, since we want to
 * support multiple languages.
 * 
 * @author Linghui Luo
 *
 */
public abstract class AbstractClass extends AbstractViewResident {

  protected final AbstractClassSource classSource;
  protected final Set<? extends IMethod> methods;
  protected final Set<? extends IField> fields;

  public AbstractClass(IView view, AbstractClassSource cs, Set<? extends IMethod> methods, Set<? extends IField> fields) {
    super(view);
    this.methods = Collections.unmodifiableSet(methods);
    this.fields = Collections.unmodifiableSet(fields);
    this.classSource = cs;

  }

  public AbstractClassSource getClassSource() {
    return classSource;
  }

  public abstract String getName();

  public abstract ISignature getSignature();

  public Optional<? extends IMethod> getMethod(ISignature signature) {
    return methods.stream().filter(m -> m.getSignature().equals(signature)).findFirst();
  }

  public Collection<? extends IMethod> getMethods() {
    return methods;
  }

  public Optional<? extends IField> getField(ISignature signature) {
    return fields.stream().filter(f -> f.getSignature().equals(signature)).findFirst();
  }

  public Collection<? extends IField> getFields() {
    return fields;
  }
}
