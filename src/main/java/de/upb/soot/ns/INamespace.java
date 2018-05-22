package de.upb.soot.ns;

import java.util.Collection;
import java.util.Optional;

import de.upb.soot.ClassSource;

/** @author Manuel Benz created on 22.05.18 */
public interface INamespace {
  Collection<ClassSource> getClasses();

  Optional<ClassSource> getClass(String className);
}
