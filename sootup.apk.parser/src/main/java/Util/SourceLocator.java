package Util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SourceLocator {


    /**
     * The index that maps classes to the files they are defined in. This is necessary because a dex file can hold multiple
     * classes.
     */
    private Map<String, File> dexClassIndex;

    protected List<String> classPath;

    // Private static instance variable
    private static SourceLocator instance;

    // Private constructor to prevent external instantiation
    private SourceLocator() {
        this.classPath = new ArrayList<>();
    }

    // Public method to get the instance of the class
    public static SourceLocator getInstance() {
        // Create the instance if it doesn't exist
        if (instance == null) {
            instance = new SourceLocator();
        }
        return instance;
    }

    /**
     * Set the class_container (dex, assembly) class index
     *
     * @param index
     *          the index
     */
    public void setDexClassIndex(Map<String, File> index) {
        dexClassIndex = index;
    }

    /**
     * Return the class index that maps class names to dex/assembly(exe/dll) files. A dex/exe/dll file contains multiple
     * classes and is not structured as a "folder structure"
     *
     * @return the index
     */
    public Map<String, File> dexClassIndex() {
        return dexClassIndex;
    }

    public List<String> getClassPath() {
        return this.classPath;
    }

    public void setClassPath(String apk_path, String android_jar){
        this.classPath.add(apk_path);
        this.classPath.add(android_jar);
    }


}
