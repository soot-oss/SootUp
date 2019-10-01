package de.upb.swt.soot.callgraph.typehierarchy;

import de.upb.swt.soot.core.views.View.ModuleDataKey;
import java.util.function.Supplier;

class TypeHierarchyKey implements ModuleDataKey<Supplier<MutableTypeHierarchy>> {
  private static final TypeHierarchyKey instance = new TypeHierarchyKey();

  static TypeHierarchyKey getInstance() {
    return instance;
  }

  private TypeHierarchyKey() {}
}
