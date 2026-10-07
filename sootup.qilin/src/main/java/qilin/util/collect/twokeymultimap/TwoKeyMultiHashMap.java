package qilin.util.collect.twokeymultimap;

/*-
 * #%L
 * SootUp - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt and others
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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TwoKeyMultiHashMap<K1, K2, V> {
  private final Map<K1, SetMultimap<K2, V>> map = new HashMap<>();

  public boolean put(K1 key1, K2 key2, V value) {
    return map.computeIfAbsent(key1, k -> HashMultimap.create()).put(key2, value);
  }

  public Set<V> get(K1 key1, K2 key2) {
    SetMultimap<K2, V> subMap = map.get(key1);
    if (subMap == null) {
      return Set.of();
    }
    return subMap.get(key2);
  }

  public SetMultimap<K2, V> get(K1 key1) {
    SetMultimap<K2, V> subMap = map.get(key1);
    if (subMap == null) {
      return HashMultimap.create();
    }
    return subMap;
  }

  public boolean containsKey(K1 key1) {
    return map.containsKey(key1);
  }

  public boolean containsKey(K1 key1, K2 key2) {
    SetMultimap<K2, V> subMap = map.get(key1);
    return subMap != null && subMap.containsKey(key2);
  }
}
