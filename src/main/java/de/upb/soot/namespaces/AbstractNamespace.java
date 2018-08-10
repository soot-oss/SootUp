package de.upb.soot.namespaces;

import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.SignatureFactory;

import java.util.Collection;
import java.util.Optional;

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

  /*
   * @Override public Collection<SootClass> getClasses(SignatureFactory factory) {
   * 
   * // FIXME: here we must take the classSources and invoke akka... return getClassSources(factory).stream().map(cs ->
   * classProvider.getSootClass(cs)).collect(Collectors.toList()); }
   * 
   * @Override public Optional<SootClass> getClass(ClassSignature classSignature) { // FIXME: here we must take the
   * classSources and invoke akka...
   * 
   * return getClassSource(classSignature).map(cs -> classProvider.getSootClass(cs)); }
   */

  protected abstract Collection<ClassSource> getClassSources(SignatureFactory factory);

  public abstract Optional<ClassSource> getClassSource(ClassSignature classSignature);
}
