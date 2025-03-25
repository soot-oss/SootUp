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
import org.jspecify.annotations.NonNull;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;

/**
 * Cache that implements a least recently used strategy. If the amount of stored classes exceeds a
 * specified amount, the lest recently used class will be overwritten.
 */
public class LRUCache implements ClassCache {
  private final LinkedHashMap<ClassType, SootClass> cache;

  public LRUCache(int cacheSize) {
    cache =
        new LinkedHashMap<ClassType, SootClass>(cacheSize, 1, true) {
          @Override
          protected boolean removeEldestEntry(Map.Entry<ClassType, SootClass> eldest) {
            return size() > cacheSize;
          }
        };
  }

  @Override
  public synchronized SootClass getClass(ClassType classType) {
    return cache.get(classType);
  }

  @NonNull
  @Override
  public synchronized Collection<SootClass> getClasses() {
    return cache.values();
  }

  @Override
  public void putClass(ClassType classType, SootClass sootClass) {
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
