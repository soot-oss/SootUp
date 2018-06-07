package de.upb.soot.namespaces;

import de.upb.soot.core.SootClass;
import de.upb.soot.signatures.ClassSignature;

import java.util.Collection;
import java.util.Optional;

/** @author Manuel Benz created on 22.05.18 */
public interface INamespace {
  Collection<SootClass> getClasses();

  Optional<SootClass> getClass(ClassSignature classSignature);
}
