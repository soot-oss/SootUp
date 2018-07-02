package de.upb.soot.namespaces;

import de.upb.soot.core.SootClass;
import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.SignatureFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Basic implementation of {@link INamespace}, encapsulating common behavior. Also used to keep the {@link INamespace}
 * interface clean from internal methods like {@link AbstractNamespace#getClassSource(ClassSignature)}.
 *
 * @author Manuel Benz created on 22.05.18
 */
public abstract class AbstractNamespace implements INamespace {
  protected final IClassProvider classProvider;

  public AbstractNamespace(IClassProvider classProvider) {
    this.classProvider = classProvider;
  }

  @Override
  public Collection<SootClass> getClasses(SignatureFactory factory) {
    return getClassSources(factory).stream().map(cs -> cs.getSootClass()).collect(Collectors.toList());
  }

  @Override
  public Optional<SootClass> getClass(ClassSignature classSignature) {
    return getClassSource(classSignature).map(cs -> cs.getSootClass());
  }

  protected abstract Collection<ClassSource> getClassSources(SignatureFactory factory);

  protected abstract Optional<ClassSource> getClassSource(ClassSignature classSignature);
}
