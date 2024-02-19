package sootup.core.cache.provider;

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

import sootup.core.cache.ClassCache;
import sootup.core.cache.LRUCache;

/** Provides a new {@link LRUCache} object. */
public class LRUCacheProvider implements ClassCacheProvider {
  private final int cacheSize;

  /** Create a new LRUCacheProvider that returns a {@link LRUCache} with a default size of 100. */
  public LRUCacheProvider() {
    this(100);
  }

  /** Create a new LRUCacheProvider that returns a {@link LRUCache} with the specified size. */
  public LRUCacheProvider(int cacheSize) {
    if (cacheSize < 1) {
      throw new IllegalArgumentException("Cache size has to be at least 1");
    }
    this.cacheSize = cacheSize;
  }

  @Override
  public ClassCache createCache() {
    return new LRUCache(cacheSize);
  }
}
