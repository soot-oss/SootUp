package sootup.core.cache;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2026 Hazem Gamal and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;

/**
 * A mutable cache on top of the cache of another view. Lookups fall through to the base cache,
 * while additions, replacements and removals only change this cache, i.e., the classes of the base
 * cache are shared and never modified. The base cache must not change while this cache is in use,
 * e.g., it belongs to a view that has resolved all of its classes.
 */
public class OverlayClassCache extends FullCache implements MutableClassCache {

  @NonNull private final ClassCache base;

  /** Classes of the base cache that were removed from this cache. */
  @NonNull private final Set<ClassType> hidden = new HashSet<>();

  public OverlayClassCache(@NonNull ClassCache base) {
    this.base = base;
  }

  @Override
  public SootClass getClass(ClassType classType) {
    SootClass sootClass = cache.get(classType);
    if (sootClass != null || hidden.contains(classType)) {
      return sootClass;
    }
    return base.getClass(classType);
  }

  @NonNull
  @Override
  public Stream<SootClass> getClasses() {
    return Stream.concat(
        cache.values().stream(),
        base.getClasses()
            .filter(c -> !hidden.contains(c.getType()) && !cache.containsKey(c.getType())));
  }

  @Override
  public void putClass(ClassType classType, SootClass sootClass) {
    if (hasClass(classType)) {
      return;
    }
    cache.put(classType, sootClass);
    hidden.remove(classType);
  }

  @Override
  public boolean hasClass(ClassType classType) {
    return cache.containsKey(classType)
        || (!hidden.contains(classType) && base.hasClass(classType));
  }

  @Override
  public SootClass removeClass(ClassType classType) {
    SootClass removed = getClass(classType);
    if (removed == null) {
      return null;
    }
    cache.remove(classType);
    if (base.hasClass(classType)) {
      hidden.add(classType);
    }
    return removed;
  }

  @Override
  public int size() {
    return (int) getClasses().count();
  }
}
