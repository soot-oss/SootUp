package sootup.core.cache;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 1997 - 2024 Raja Vallée-Rai and others
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

import java.util.*;
import javax.annotation.Nonnull;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;

/**
 * Cache that implements a least recently used strategy. If the amount of stored classes exceeds a
 * specified amount, the lest recently used class will be overwritten.
 */
public class LRUCache implements ClassCache {
  private final int cacheSize;
  private final Map<ClassType, SootClass> cache = new HashMap<>();
  private final LinkedList<ClassType> accessOrder = new LinkedList<>();

  public LRUCache(int cacheSize) {
    this.cacheSize = cacheSize;
  }

  @Override
  public synchronized SootClass getClass(ClassType classType) {
    SootClass sootClass = cache.get(classType);
    if (sootClass != null) {
      accessOrder.remove(classType);
      accessOrder.addFirst(classType);
    }

    return sootClass;
  }

  @Nonnull
  @Override
  public synchronized Collection<SootClass> getClasses() {
    return cache.values();
  }

  @Override
  public void putClass(ClassType classType, SootClass sootClass) {
    if (accessOrder.size() >= cacheSize) {
      ClassType leastAccessed = accessOrder.removeLast();
      cache.remove(leastAccessed);
    }

    accessOrder.addFirst(classType);
    cache.putIfAbsent(classType, sootClass);
  }

  @Override
  public boolean hasClass(ClassType classType) {
    return cache.containsKey(classType);
  }

  @Override
  public int size() {
    return cache.size();
  }
}
