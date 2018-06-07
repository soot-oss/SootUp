package de.upb.soot.ns;

import java.util.Collection;
import java.util.Optional;

import de.upb.soot.core.SootClass;
import de.upb.soot.signatures.ClassSignature;

/** @author Manuel Benz created on 22.05.18 */
public interface INamespace {
  Collection<SootClass> getClasses();

  Optional<SootClass> getClass(ClassSignature classSignature);
}
