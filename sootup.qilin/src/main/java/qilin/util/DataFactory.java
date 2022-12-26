package qilin.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
