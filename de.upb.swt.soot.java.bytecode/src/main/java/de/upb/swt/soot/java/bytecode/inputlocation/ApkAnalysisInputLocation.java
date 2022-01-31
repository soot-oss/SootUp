package de.upb.swt.soot.java.bytecode.inputlocation;

import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexFileProvider;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.Util;
import de.upb.swt.soot.java.core.JavaSootClass;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.MagicNumberFileFilter;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pxb.android.axml.AxmlReader;
import pxb.android.axml.AxmlVisitor;
import pxb.android.axml.NodeVisitor;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ApkAnalysisInputLocation implements AnalysisInputLocation<JavaSootClass> {

    private static final @Nonnull
    Logger logger = LoggerFactory.getLogger(ApkAnalysisInputLocation.class);

    private final Map<String, Integer> maxAPIs = new HashMap<String, Integer>();
    private AndroidVersionInfo androidSDKVersionInfo;
    private int androidAPIVersion = -1;
    private static final int defaultSdkVersion = 15;

    private final static Comparator<DexFileProvider.DexContainer<? extends DexFile>> DEFAULT_PRIORITIZER
            = new Comparator<DexFileProvider.DexContainer<? extends DexFile>>() {

        @Override
        public int compare(DexFileProvider.DexContainer<? extends DexFile> o1, DexFileProvider.DexContainer<? extends DexFile> o2) {
            String s1 = o1.getDexName(), s2 = o2.getDexName();

            // "classes.dex" has highest priority
            if (s1.equals("classes.dex")) {
                return 1;
            } else if (s2.equals("classes.dex")) {
                return -1;
            }

            // if one of the strings starts with "classes", we give it the edge right here
            boolean s1StartsClasses = s1.startsWith("classes");
            boolean s2StartsClasses = s2.startsWith("classes");

            if (s1StartsClasses && !s2StartsClasses) {
                return 1;
            } else if (s2StartsClasses && !s1StartsClasses) {
                return -1;
            }

            // otherwise, use natural string ordering
            return s1.compareTo(s2);
        }
    };

    /**
     * Mapping of filesystem file (apk, dex, etc.) to mapping of dex name to dex file
     */
    private final Map<Path, Map<Path, DexFileProvider.DexContainer<? extends DexFile>>> dexMap = new HashMap<>();

    @Nonnull private final Path apkPath;
    boolean process_multiple_dex = true;
    boolean search_dex_in_archives = true;
    boolean verbose = true;
    int android_api_version = 10;


    public ApkAnalysisInputLocation(@Nonnull Path apkPath, Collection<MultiDexContainer.DexEntry<? extends DexFile>> dexFiles){
        this.dexFiles = dexFiles;
        if (!Files.exists(apkPath)) {
            throw new ResolveException("No APK file found",apkPath);
        }
        this.apkPath = apkPath;

    }

    @Nonnull
    @Override
    public Optional<? extends AbstractClassSource<JavaSootClass>> getClassSource(@Nonnull ClassType type, @Nonnull View<?> view) {
        // TODO code here
        ArrayList<DexFileProvider.DexContainer<? extends DexFile>> resultList = new ArrayList<>();
        // TODO allSourcesFromFile(path) is quite redent to allsourcesfromfile
        List<Path> allSources = allSourcesFromFile(apkPath);
        updateIndex(allSources);

        for (Path theSource : allSources) {
            resultList.addAll(dexMap.get(theSource).values());
        }

        if (resultList.size() > 1) {
            Collections.sort(resultList, Collections.reverseOrder(DEFAULT_PRIORITIZER));
        }

        return Optional.ofNullable(new DexFileProvider().createClassSource(this, apkPath, type));
    }

    @Nonnull
    @Override
    public Collection<? extends AbstractClassSource<JavaSootClass>> getClassSources(@Nonnull View<?> view) {
        // TODO code here
        return null;
    }

    /**
     * Returns all dex files found in dex source sorted by the default dex prioritizer
     *
     * @param dexSource
     *          Path to a jar, apk, dex, odex or a directory containing multiple dex files
     * @return List of dex files derived from source
     */
    public List<DexFileProvider.DexContainer<? extends DexFile>> getDexFromSource(Path dexSource) throws IOException {
        return getDexFromSource(dexSource, DEFAULT_PRIORITIZER);
    }

    /**
     * Returns all dex files found in dex source sorted by the default dex prioritizer
     *
     * @param dexSource
     *          Path to a jar, apk, dex, odex or a directory containing multiple dex files
     * @param prioritizer
     *          A comparator that defines the ordering of dex files in the result list
     * @return List of dex files derived from source
     */
    public List<DexFileProvider.DexContainer<? extends DexFile>> getDexFromSource(Path dexSource,
                                                                                  Comparator<DexFileProvider.DexContainer<? extends DexFile>> prioritizer) throws IOException {
        ArrayList<DexFileProvider.DexContainer<? extends DexFile
                >> resultList = new ArrayList<>();
        List<Path> allSources = allSourcesFromFile(dexSource);
        updateIndex(allSources);

        for (Path theSource : allSources) {
            resultList.addAll(dexMap.get(theSource).values());
        }

        if (resultList.size() > 1) {
            Collections.sort(resultList, Collections.reverseOrder(prioritizer));
        }
        return resultList;
    }

    /**
     * Returns the first dex file with the given name found in the given dex source
     *
     * @param dexSource
     *          Path to a jar, apk, dex, odex or a directory containing multiple dex files
     * @return Dex file with given name in dex source
     * @throws ResolveException
     *           If no dex file with the given name exists
     */
    public DexFileProvider.DexContainer<? extends DexFile> getDexFromSource(Path dexSource, String dexName) throws IOException {
        List<Path> allSources = allSourcesFromFile(dexSource);
        updateIndex(allSources);

        // we take the first dex we find with the given name
        for (Path theSource : allSources) {
            DexFileProvider.DexContainer<? extends DexFile> dexFile = dexMap.get(theSource).get(dexName);
            if (dexFile != null) {
                return dexFile;
            }
        }

        throw new ResolveException("Dex file with name '" + dexName + "' not found in " + dexSource, dexSource);
    }

    private List<Path> allSourcesFromFile(Path dexSource) {
        if (Files.isDirectory(dexSource)) {
            List<Path> dexFiles = getAllDexFilesInDirectory(dexSource);
            if (dexFiles.size() > 1 && !process_multiple_dex) {
                Path path = dexFiles.get(0);
                logger.warn("Multiple dex files detected, only processing '" + path
                        + "'. Use '-process-multiple-dex' option to process them all.");
                return Collections.singletonList(path);
            } else {
                return dexFiles;
            }
        } else {
            String ext = com.google.common.io.Files.getFileExtension(dexSource.getFileName().toString()).toLowerCase();
            if ((ext.equals("jar") || ext.equals("zip")) && !search_dex_in_archives) {
                return Collections.emptyList();
            } else {
                return Collections.singletonList(dexSource);
            }
        }
    }

    private void updateIndex(List<Path> dexSources) {
        for (Path theSource : dexSources) {
            Map<Path, DexFileProvider.DexContainer<? extends DexFile>> dexFiles = dexMap.get(theSource);
            if (dexFiles == null) {
                try {
                    dexFiles = mappingForFile(theSource);
                    dexMap.put(theSource, dexFiles);
                } catch (IOException e) {
                    throw new ResolveException("Error parsing dex source ", theSource, e);
                }
            }
        }
    }

    /**
     * @param dexSourceFile
     *          A file containing either one or multiple dex files (apk, zip, etc.) but no directory!
     * @return
     * @throws IOException
     */
    private Map<Path, DexFileProvider.DexContainer<? extends DexFile>> mappingForFile(Path dexSourceFile) throws IOException {
        int api = android_api_version;
        boolean multiple_dex = process_multiple_dex; // TODO set multidex from dex file count automatically

        // load dex files from apk/folder/file
        MultiDexContainer<? extends DexBackedDexFile> dexContainer
                = DexFileFactory.loadDexContainer(dexSourceFile.toFile(), Opcodes.forApi(api));

        List<String> dexEntryNameList = dexContainer.getDexEntryNames();
        int dexFileCount = dexEntryNameList.size();

        if (dexFileCount < 1) {
            if (verbose) {
                logger.debug("" + String.format("Warning: No dex file found in '%s'", dexSourceFile));
            }
            return Collections.emptyMap();
        }

        Map<Path, DexFileProvider.DexContainer<? extends DexFile>> dexMap = new HashMap<>(dexFileCount);

        // report found dex files and add to list.
        // We do this in reverse order to make sure that we add the first entry if there is no classes.dex file in single dex
        // mode
        ListIterator<String> entryNameIterator = dexEntryNameList.listIterator(dexFileCount);
        while (entryNameIterator.hasPrevious()) {
            String entryName = entryNameIterator.previous();
            Path entryPath = Paths.get(entryName);
            MultiDexContainer.DexEntry<? extends DexFile> entry = dexContainer.getEntry(entryName);
            logger.debug("" + String.format("Found dex file '%s' with %d classes in '%s'", entryName,
                    entry.getDexFile().getClasses().size(), dexSourceFile));

            if (multiple_dex) {
                dexMap.put(entryPath, new DexFileProvider.DexContainer<>(entry, entryName, dexSourceFile));
            } else if (dexMap.isEmpty() && (entryName.equals("classes.dex") || !entryNameIterator.hasPrevious())) {
                // We prefer to have classes.dex in single dex mode.
                // If we haven't found a classes.dex until the last element, take the last!
                dexMap = Collections.singletonMap(entryPath, new DexFileProvider.DexContainer<>(entry, entryName, dexSourceFile));
                if (dexFileCount > 1) {
                    logger.warn("Multiple dex files detected, only processing '" + entryName
                            + "'. Use '-process-multiple-dex' option to process them all.");
                }
            }
        }
        return Collections.unmodifiableMap(dexMap);
    }

    private List<Path> getAllDexFilesInDirectory(Path path) {
        try {
            return Files.walk(path).filter(p-> {
                return p.toString().endsWith(".dex") && Files.isDirectory(p);
            }).collect(Collectors.toList());
        } catch (IOException e) {
            throw new ResolveException("Error while finding .dex file",path,e);
        }
    }

    public void initialize() {
        // resolve classes in dex files
        for (MultiDexContainer.DexEntry<? extends DexFile> dexEntry : dexFiles) {
            final DexFile dexFile = dexEntry.getDexFile();
            for (ClassDef defItem : dexFile.getClasses()) {
                String forClassName = Util.dottedClassName(defItem.getType());
                classesToDefItems.put(forClassName, new ClassInformation(dexEntry, defItem));
            }
        }
    }

    private final static Set<String> systemAnnotationNames;

    static {
        Set<String> systemAnnotationNamesModifiable = new HashSet<String>();
        // names as defined in the ".dex - Dalvik Executable Format" document
        systemAnnotationNamesModifiable.add("dalvik.annotation.AnnotationDefault");
        systemAnnotationNamesModifiable.add("dalvik.annotation.EnclosingClass");
        systemAnnotationNamesModifiable.add("dalvik.annotation.EnclosingMethod");
        systemAnnotationNamesModifiable.add("dalvik.annotation.InnerClass");
        systemAnnotationNamesModifiable.add("dalvik.annotation.MemberClasses");
        systemAnnotationNamesModifiable.add("dalvik.annotation.Signature");
        systemAnnotationNamesModifiable.add("dalvik.annotation.Throws");
        systemAnnotationNames = Collections.unmodifiableSet(systemAnnotationNamesModifiable);
    }

    //private final DexClassLoader dexLoader = new DexClassLoader();

    private static class ClassInformation {
        public MultiDexContainer.DexEntry<? extends DexFile> dexEntry;
        public ClassDef classDefinition;

        public ClassInformation(MultiDexContainer.DexEntry<? extends DexFile> entry, ClassDef classDef) {
            this.dexEntry = entry;
            this.classDefinition = classDef;
        }
    }

    private final Map<String, ClassInformation> classesToDefItems = new HashMap<String, ClassInformation>();
    private final Collection<MultiDexContainer.DexEntry<? extends DexFile>> dexFiles;

    /**
     * Construct a DexlibWrapper from a dex file and stores its classes referenced by their name. No further process is done
     * here.
     */
    public ApkAnalysisInputLocation(Path dexSource) {
        this.apkPath = dexSource;
        try {
            List<DexFileProvider.DexContainer<? extends DexFile>> containers = new DexFileProvider().getDexFromSource(dexSource);
            this.dexFiles = new ArrayList<>(containers.size());
            for (DexFileProvider.DexContainer<? extends DexFile> container : containers) {
                this.dexFiles.add(container.getBase());
            }
        } catch (IOException e) {
            throw new ResolveException("IOException during dex parsing", dexSource);
        }
    }

    /**
     * Returns the max Android API version number available in directory 'dir'
     *
     * @param dir
     * @return
     */
    private int getMaxAPIAvailable(String dir) {
        Integer mapi = this.maxAPIs.get(dir);
        if (mapi != null) {
            return mapi;
        }

        File d = new File(dir);
        if (!d.exists()) {
            throw new AndroidPlatformException(
                    String.format("The Android platform directory you have specified (%s) does not exist. Please check.", dir));
        }

        File[] files = d.listFiles();
        if (files == null) {
            return -1;
        }

        int maxApi = -1;
        for (File f : files) {
            String name = f.getName();
            if (f.isDirectory() && name.startsWith("android-")) {
                try {
                    int v = Integer.decode(name.split("android-")[1]);
                    if (v > maxApi) {
                        maxApi = v;
                    }
                } catch (NumberFormatException ex) {
                    // We simply ignore directories that do not follow the
                    // Android naming structure
                }
            }
        }
        this.maxAPIs.put(dir, maxApi);
        return maxApi;
    }

    public String getAndroidJarPath(String jars, String apk) {
        int APIVersion = getAndroidAPIVersion(jars, apk);

        String jarPath = jars + File.separatorChar + "android-" + APIVersion + File.separatorChar + "android.jar";

        // check that jar exists
        File f = new File(jarPath);
        if (!f.isFile()) {
            throw new AndroidPlatformException(String.format("error: target android.jar %s does not exist.", jarPath));
        }
        return jarPath;
    }

    private int options_android_api_version = 10;

    public int getAndroidAPIVersion() {
        return androidAPIVersion > 0 ? androidAPIVersion
                : (options_android_api_version > 0 ? options_android_api_version : defaultSdkVersion);
    }

    private int getAndroidAPIVersion(String jars, String apk) {
        // Do we already have an API version?
        if (androidAPIVersion > 0) {
            return androidAPIVersion;
        }

        // get path to appropriate android.jar
        File jarsF = new File(jars);
        if (!jarsF.exists()) {
            throw new AndroidPlatformException(
                    String.format("Android platform directory '%s' does not exist!", jarsF.getAbsolutePath()));
        }
        if (apk != null && !(new File(apk)).exists()) {
            throw new RuntimeException("file '" + apk + "' does not exist!");
        }

        // Use the default if we don't have any other information
        androidAPIVersion = defaultSdkVersion;

        // Do we have an explicit API version?
        if (options_android_api_version > 0) {
            androidAPIVersion = options_android_api_version;
        } else if (apk != null) {
            if (apk.toLowerCase().endsWith(".apk")) {
                androidAPIVersion = getTargetSDKVersion(apk, jars);
            }
        }

        // If we don't have that API version installed, we take the most recent one we have
        final int maxAPI = getMaxAPIAvailable(jars);
        if (maxAPI > 0 && androidAPIVersion > maxAPI) {
            androidAPIVersion = maxAPI;
        }

        // If the platform version is missing in the middle, we take the next one
        while (androidAPIVersion < maxAPI) {
            String jarPath = jars + File.separatorChar + "android-" + androidAPIVersion + File.separatorChar + "android.jar";
            if (new File(jarPath).exists()) {
                break;
            }
            androidAPIVersion++;
        }

        return androidAPIVersion;
    }

    private int getTargetSDKVersion(String apkFile, String platformJARs) {
        // get AndroidManifest
        ZipFile archive = null;
        try {
            InputStream manifestIS = null;
            try {
                archive = new ZipFile(apkFile);
                for (Enumeration<? extends ZipEntry> entries = archive.entries(); entries.hasMoreElements();) {
                    ZipEntry entry = entries.nextElement();
                    // We are dealing with the Android manifest
                    if ("AndroidManifest.xml".equals(entry.getName())) {
                        manifestIS = archive.getInputStream(entry);
                        break;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Error when looking for manifest in apk: " + e);
            }

            if (manifestIS == null) {
                logger.debug("Could not find sdk version in Android manifest! Using default: " + defaultSdkVersion);
                return defaultSdkVersion;
            }

            // process AndroidManifest.xml
            androidSDKVersionInfo = AndroidVersionInfo.get(manifestIS);
        } finally {
            if (archive != null) {
                try {
                    archive.close();
                } catch (IOException e) {
                    throw new RuntimeException("Error when looking for manifest in apk: " + e);
                }
            }
        }

        int maxAPI = getMaxAPIAvailable(platformJARs);
        int APIVersion = -1;
        if (androidSDKVersionInfo.sdkTargetVersion != -1) {
            if (androidSDKVersionInfo.sdkTargetVersion > maxAPI && androidSDKVersionInfo.minSdkVersion != -1
                    && androidSDKVersionInfo.minSdkVersion <= maxAPI) {
                logger.warn("Android API version '" + androidSDKVersionInfo.sdkTargetVersion
                        + "' not available, using minApkVersion '" + androidSDKVersionInfo.minSdkVersion + "' instead");
                APIVersion = androidSDKVersionInfo.minSdkVersion;
            } else {
                APIVersion = androidSDKVersionInfo.sdkTargetVersion;
            }
        } else if (androidSDKVersionInfo.platformBuildVersionCode != -1) {
            if (androidSDKVersionInfo.platformBuildVersionCode > maxAPI && androidSDKVersionInfo.minSdkVersion != -1
                    && androidSDKVersionInfo.minSdkVersion <= maxAPI) {
                logger.warn("Android API version '" + androidSDKVersionInfo.platformBuildVersionCode
                        + "' not available, using minApkVersion '" + androidSDKVersionInfo.minSdkVersion + "' instead");
                APIVersion = androidSDKVersionInfo.minSdkVersion;
            } else {
                APIVersion = androidSDKVersionInfo.platformBuildVersionCode;
            }
        } else if (androidSDKVersionInfo.minSdkVersion != -1) {
            APIVersion = androidSDKVersionInfo.minSdkVersion;
        } else {
            logger.debug("Could not find sdk version in Android manifest! Using default: " + defaultSdkVersion);
            APIVersion = defaultSdkVersion;
        }

        if (APIVersion <= 2) {
            APIVersion = 3;
        }
        return APIVersion;
    }

    public AndroidVersionInfo getAndroidSDKVersionInfo() {
        return androidSDKVersionInfo;
    }
    private String options_android_jars = "";
    private String options_force_android_jar = "";
    private String options_soot_classpath = "";
    private List<String> options_process_dir;

    private String defaultAndroidClassPath() {
        // check that android.jar is not in classpath
        String androidJars = options_android_jars;
        String forceAndroidJar = options_force_android_jar;
        if ((androidJars == null || androidJars.isEmpty()) && (forceAndroidJar == null || forceAndroidJar.isEmpty())) {
            throw new RuntimeException("You are analyzing an Android application but did "
                    + "not define android.jar. Options -android-jars or -force-android-jar should be used.");
        }

        // Get the platform JAR file. It either directly specified, or
        // we detect it from the target version of the APK we are
        // analyzing
        String jarPath = "";
        if (forceAndroidJar != null && !forceAndroidJar.isEmpty()) {
            jarPath = forceAndroidJar;

            if (options_android_api_version > 0) {
                androidAPIVersion = options_android_api_version;
            } else if (forceAndroidJar.contains("android-")) {
                Pattern pt = Pattern.compile("\\b" + File.separatorChar + "android-(\\d+)" + "\\b" + File.separatorChar);
                Matcher m = pt.matcher(forceAndroidJar);
                if (m.find()) {
                    androidAPIVersion = Integer.valueOf(m.group(1));
                }
            } else {
                androidAPIVersion = defaultSdkVersion;
            }
        } else if (androidJars != null && !androidJars.isEmpty()) {
            List<String> classPathEntries
                    = new ArrayList<String>(Arrays.asList(options_soot_classpath.split(File.pathSeparator)));
            classPathEntries.addAll(options_process_dir);


            String targetApk = "";
            Set<String> targetDexs = new HashSet<String>();
            for (String entry : classPathEntries) {
                if (isApk(new File(entry))) {
                    if (targetApk != null && !targetApk.isEmpty()) {
                        throw new RuntimeException("only one Android application can be analyzed when using option -android-jars.");
                    }
                    targetApk = entry;
                }
                if (entry.toLowerCase().endsWith(".dex")) {
                    // names are case-insensitive
                    targetDexs.add(entry);
                }
            }

            // We need at least one file to process
            if (targetApk == null || targetApk.isEmpty()) {
                if (targetDexs.isEmpty()) {
                    throw new RuntimeException("no apk file given");
                }
                jarPath = getAndroidJarPath(androidJars, null);
            } else {
                jarPath = getAndroidJarPath(androidJars, targetApk);
            }
        }

        // We must have a platform JAR file when analyzing Android apps
        if (jarPath.isEmpty()) {
            throw new RuntimeException("android.jar not found.");
        }

        // Check the platform JAR file
        File f = new File(jarPath);
        if (!f.exists()) {
            throw new RuntimeException("file '" + jarPath + "' does not exist!");
        } else {
            logger.debug("Using '" + jarPath + "' as android.jar");
        }

        return jarPath;
    }

    public static boolean isApk(File apk) {
        // first check magic number
        // Note that there are multiple magic numbers for different versions of ZIP files, but all of them
        // have "PK" at the beginning. In order to not decline possible future versions of ZIP files which
        // may be supported by the JVM, we only check these two bytes.
        MagicNumberFileFilter apkFilter = new MagicNumberFileFilter(new byte[] { (byte) 0x50, (byte) 0x4B });
        if (!apkFilter.accept(apk)) {
            return false;
        }
        // second check if contains dex file.
        try (ZipFile zf = new ZipFile(apk)) {
            for (Enumeration<? extends ZipEntry> en = zf.entries(); en.hasMoreElements();) {
                ZipEntry z = en.nextElement();
                if ("classes.dex".equals(z.getName())) {
                    return true;
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public static class AndroidVersionInfo {

        public int sdkTargetVersion = -1;
        public int minSdkVersion = -1;
        public int platformBuildVersionCode = -1;

        private static AndroidVersionInfo get(InputStream manifestIS) {
            final AndroidVersionInfo versionInfo = new AndroidVersionInfo();
            final AxmlVisitor axmlVisitor = new AxmlVisitor() {
                private String nodeName = null;

                @Override
                public void attr(String ns, String name, int resourceId, int type, Object obj) {
                    super.attr(ns, name, resourceId, type, obj);

                    if (nodeName != null && name != null) {
                        if (nodeName.equals("manifest")) {
                            if (name.equals("platformBuildVersionCode")) {
                                versionInfo.platformBuildVersionCode = Integer.valueOf("" + obj);
                            }
                        } else if (nodeName.equals("uses-sdk")) {
                            // Obfuscated APKs often remove the attribute names and use the resourceId instead
                            // Therefore it is better to check for both variants
                            if (name.equals("targetSdkVersion") || (name.isEmpty() && resourceId == 16843376)) {
                                versionInfo.sdkTargetVersion = Integer.valueOf(String.valueOf(obj));
                            } else if (name.equals("minSdkVersion") || (name.isEmpty() && resourceId == 16843276)) {
                                versionInfo.minSdkVersion = Integer.valueOf(String.valueOf(obj));
                            }
                        }
                    }
                }

                @Override
                public NodeVisitor child(String ns, String name) {
                    nodeName = name;
                    return this;
                }
            };

            try {
                AxmlReader xmlReader = new AxmlReader(IOUtils.toByteArray(manifestIS));
                xmlReader.accept(axmlVisitor);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            return versionInfo;
        }
    }

}
