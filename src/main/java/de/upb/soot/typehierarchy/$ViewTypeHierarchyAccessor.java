package de.upb.soot.typehierarchy;

import de.upb.soot.views.View;

/**
 * To avoid leaking the {@link ViewTypeHierarchy} constructor as an implementation detail to the
 * public API, we only allow its instantiation through this class. It is deliberately cryptically
 * named. Soot users are not supposed to instantiate this class, as it is managed by the View.
 */
public final class $ViewTypeHierarchyAccessor {
  private $ViewTypeHierarchyAccessor() {}

  /** Calls the constructor of {@link ViewTypeHierarchy}. */
  public static ViewTypeHierarchy createViewTypeHierarchy(View view) {
    return new ViewTypeHierarchy(view);
  }
}
