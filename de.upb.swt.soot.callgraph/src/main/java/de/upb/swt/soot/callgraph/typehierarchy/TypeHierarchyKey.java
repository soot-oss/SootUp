package de.upb.swt.soot.callgraph.typehierarchy;

import de.upb.swt.soot.core.views.View.ModuleDataKey;
import java.util.function.Supplier;

/**
 * Used to store a caching supplier of the {@link TypeHierarchy} in a {@link
 * de.upb.swt.soot.core.views.View} without the core module needing a dependency on this module.
 *
 * @see #getInstance()
 * @author Christian Brüggemann
 */
class TypeHierarchyKey extends ModuleDataKey<Supplier<MutableTypeHierarchy>> {
  private static final TypeHierarchyKey instance = new TypeHierarchyKey();

  static TypeHierarchyKey getInstance() {
    return instance;
  }

  private TypeHierarchyKey() {}
}
