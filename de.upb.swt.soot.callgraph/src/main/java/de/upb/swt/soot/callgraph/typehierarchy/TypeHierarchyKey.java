package de.upb.swt.soot.callgraph.typehierarchy;

import java.util.function.Supplier;

import de.upb.swt.soot.core.views.View.ModuleDataKey;

class TypeHierarchyKey implements ModuleDataKey<Supplier<MutableTypeHierarchy>> {
  private static final TypeHierarchyKey instance = new TypeHierarchyKey();

  static TypeHierarchyKey getInstance() {
    return instance;
  }

  private TypeHierarchyKey() {}
}
