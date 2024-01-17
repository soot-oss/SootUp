package dexpler;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

public class DexResolver {
    protected Map<File, DexLibWrapper> cache = new TreeMap<File, DexLibWrapper>();

    private static DexResolver instance;

    public static DexResolver getInstance() {
        if (instance == null) {
            instance = new DexResolver();
        }
        return instance;
    }

    public DexLibWrapper initializeDexFile(File file) {
        DexLibWrapper wrapper = cache.get(file);
        if (wrapper == null) {
            wrapper = new DexLibWrapper(file);
            cache.put(file, wrapper);
            wrapper.initialize();
        }
        return wrapper;
    }
}
