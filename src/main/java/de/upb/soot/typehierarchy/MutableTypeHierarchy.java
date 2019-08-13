package de.upb.soot.typehierarchy;

import de.upb.soot.core.SootClass;
import de.upb.soot.types.JavaClassType;

public interface MutableTypeHierarchy extends TypeHierarchy {
  void addType(JavaClassType type);

  void addType(SootClass sootClass);
}
