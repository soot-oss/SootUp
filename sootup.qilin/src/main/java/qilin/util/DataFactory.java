/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.util;

import java.util.*;

public class DataFactory {
    public static <T> List<T> createList() {
        return new ArrayList<>();
//        return new CopyOnWriteArrayList<>();
    }

    public static <T> Set<T> createSet() {
        return new HashSet<>();
//        return ConcurrentHashMap.newKeySet();
    }

    public static <T> Set<T> createSet(int initCapacity) {
        return new HashSet<>(initCapacity);
//        return ConcurrentHashMap.newKeySet(initCapacity);
    }

    public static <K, V> Map<K, V> createMap() {
        return new HashMap<>();
//        return new ConcurrentHashMap<>();
    }

    public static <K, V> Map<K, V> createMap(int initCapacity) {
        return new HashMap<>(initCapacity);
//        return new ConcurrentHashMap<>(initCapacity);
    }
}
