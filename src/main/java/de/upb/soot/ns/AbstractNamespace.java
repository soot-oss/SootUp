package de.upb.soot.ns;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import de.upb.soot.core.SootClass;
import de.upb.soot.ns.classprovider.ClassSource;
import de.upb.soot.ns.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;

/** @author Manuel Benz created on 22.05.18 */
public abstract class AbstractNamespace implements INamespace {
  protected final IClassProvider classProvider;

  public AbstractNamespace(IClassProvider classProvider) {
    this.classProvider = classProvider;
  }

  @Override
  public Collection<SootClass> getClasses() {
    return getClassSources().stream().map(cs -> new SootClass(cs)).collect(Collectors.toList());
  }

  @Override
  public Optional<SootClass> getClass(ClassSignature classSignature) {
    return getClassSource(classSignature).map(cs -> new SootClass(cs));
  }

  protected abstract Collection<ClassSource> getClassSources();

  protected abstract Optional<ClassSource> getClassSource(ClassSignature classSignature);
}
