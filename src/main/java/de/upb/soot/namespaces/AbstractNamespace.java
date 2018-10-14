package de.upb.soot.namespaces;

import de.upb.soot.namespaces.classprovider.AbstractClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.namespaces.classprovider.asm.AsmJavaClassProvider;
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

  /**
   * Create the namespace
   * @param classProvider The class provider to be used
   */
  public AbstractNamespace(IClassProvider classProvider) {
    this.classProvider = classProvider;
  }

  /**
   * Returns the {@link IClassProvider} instance for this namespace
   * @return The class provider for this namespace
   */
  @Override
  public IClassProvider getClassProvider() {
    return classProvider;
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

  /**
   * Constructs a default class provider for use with namespaces.
   * Currently, this provides an instance of {@link AsmJavaClassProvider} to read Java Bytecode.
   * This might be more brilliant in the future.
   *
   * @return An instance of {@link IClassProvider} to be used.
   */
  protected static IClassProvider getDefaultClassProvider() {
    return new AsmJavaClassProvider();
  }

  protected abstract Collection<AbstractClassSource> getClassSources(SignatureFactory factory);

  @Override
  public abstract Optional<AbstractClassSource> getClassSource(ClassSignature classSignature);
}
