package de.upb.swt.soot.java.bytecode.inputlocation;

import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.types.*;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.*;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaSootClass;
import javafx.scene.Scene;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.reference.DexBackedTypeReference;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ApkAnalysisInputLocation implements AnalysisInputLocation<JavaSootClass> {

    private static final @Nonnull
    Logger logger = LoggerFactory.getLogger(ApkAnalysisInputLocation.class);

    /**
     * Mapping of filesystem file (apk, dex, etc.) to mapping of dex name to dex file
     */
    private final Map<String, Map<Path, DexFileProvider.DexContainer<? extends DexFile>>> dexMap = new HashMap<>();

    @Nonnull private final Path apkPath;

    public ApkAnalysisInputLocation(@Nonnull Path apkPath){
        if (!Files.exists(apkPath)) {
            throw new ResolveException("No APK file found",apkPath);
        }
        this.apkPath = apkPath;
    }

    @Nonnull
    @Override
    public Optional<? extends AbstractClassSource<JavaSootClass>> getClassSource(@Nonnull ClassType type, @Nonnull View<?> view) {
        // TODO code here
        ArrayList<DexContainer<? extends DexFile>> resultList = new ArrayList<>();
        List<File> allSources = allSourcesFromFile(apkPath);
        updateIndex(allSources);

        for (File theSource : allSources) {
            resultList.addAll(dexMap.get(theSource.getCanonicalPath()).values());
        }

        if (resultList.size() > 1) {
            Collections.sort(resultList, Collections.reverseOrder(prioritizer));
        }
        return new DexFileProvider().createClassSource(this, dexMap.get(), type);
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
     * @param prioritizer
     *          A comparator that defines the ordering of dex files in the result list
     * @return List of dex files derived from source
     */
    public List<DexContainer<? extends DexFile>> getDexFromSource(File dexSource,
                                                                  Comparator<DexContainer<? extends DexFile>> prioritizer) throws IOException {
        ArrayList<DexContainer<? extends DexFile>> resultList = new ArrayList<>();
        List<File> allSources = allSourcesFromFile(dexSource);
        updateIndex(allSources);

        for (File theSource : allSources) {
            resultList.addAll(dexMap.get(theSource.getCanonicalPath()).values());
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
     * @throws CompilationDeathException
     *           If no dex file with the given name exists
     */
    public DexContainer<? extends DexFile> getDexFromSource(File dexSource, String dexName) throws IOException {
        List<File> allSources = allSourcesFromFile(dexSource);
        updateIndex(allSources);

        // we take the first dex we find with the given name
        for (File theSource : allSources) {
            DexContainer<? extends DexFile> dexFile = dexMap.get(theSource.getCanonicalPath()).get(dexName);
            if (dexFile != null) {
                return dexFile;
            }
        }

        throw new CompilationDeathException("Dex file with name '" + dexName + "' not found in " + dexSource);
    }

    private List<File> allSourcesFromFile(Path dexSource) {
        if (Files.isDirectory(dexSource)) {
            List<File> dexFiles = getAllDexFilesInDirectory(dexSource);
            if (dexFiles.size() > 1 && !Options.v().process_multiple_dex()) {
                File file = dexFiles.get(0);
                logger.warn("Multiple dex files detected, only processing '" + file.getCanonicalPath()
                        + "'. Use '-process-multiple-dex' option to process them all.");
                return Collections.singletonList(file);
            } else {
                return dexFiles;
            }
        } else {
            String ext = com.google.common.io.Files.getFileExtension(dexSource.getName()).toLowerCase();
            if ((ext.equals("jar") || ext.equals("zip")) && !Options.v().search_dex_in_archives()) {
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
                    dexMap.put(key, dexFiles);
                } catch (IOException e) {
                    throw new CompilationDeathException("Error parsing dex source", e);
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
    private Map<String, DexContainer<? extends DexFile>> mappingForFile(Path dexSourceFile) throws IOException {
        int api = Scene.v().getAndroidAPIVersion();
        boolean multiple_dex = Options.v().process_multiple_dex();

        // load dex files from apk/folder/file
        MultiDexContainer<? extends DexBackedDexFile> dexContainer
                = DexFileFactory.loadDexContainer(dexSourceFile, Opcodes.forApi(api));

        List<String> dexEntryNameList = dexContainer.getDexEntryNames();
        int dexFileCount = dexEntryNameList.size();

        if (dexFileCount < 1) {
            if (Options.v().verbose()) {
                logger.debug("" + String.format("Warning: No dex file found in '%s'", dexSourceFile));
            }
            return Collections.emptyMap();
        }

        Map<String, DexContainer<? extends DexFile>> dexMap = new HashMap<>(dexFileCount);

        // report found dex files and add to list.
        // We do this in reverse order to make sure that we add the first entry if there is no classes.dex file in single dex
        // mode
        ListIterator<String> entryNameIterator = dexEntryNameList.listIterator(dexFileCount);
        while (entryNameIterator.hasPrevious()) {
            String entryName = entryNameIterator.previous();
            MultiDexContainer.DexEntry<? extends DexFile> entry = dexContainer.getEntry(entryName);
            entryName = deriveDexName(entryName);
            logger.debug("" + String.format("Found dex file '%s' with %d classes in '%s'", entryName,
                    entry.getDexFile().getClasses().size(), dexSourceFile.getCanonicalPath()));

            if (multiple_dex) {
                dexMap.put(entryName, new DexContainer<>(entry, entryName, dexSourceFile));
            } else if (dexMap.isEmpty() && (entryName.equals("classes.dex") || !entryNameIterator.hasPrevious())) {
                // We prefer to have classes.dex in single dex mode.
                // If we haven't found a classes.dex until the last element, take the last!
                dexMap = Collections.singletonMap(entryName, new DexContainer<>(entry, entryName, dexSourceFile));
                if (dexFileCount > 1) {
                    logger.warn("Multiple dex files detected, only processing '" + entryName
                            + "'. Use '-process-multiple-dex' option to process them all.");
                }
            }
        }
        return Collections.unmodifiableMap(dexMap);
    }

    private String deriveDexName(String entryName) {
        return new File(entryName).getName();
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

    public static final class DexContainer<T extends DexFile> {
        private final MultiDexContainer.DexEntry<T> base;
        private final String name;
        private final File filePath;

        public DexContainer(MultiDexContainer.DexEntry<T> base, String name, File filePath) {
            this.base = base;
            this.name = name;
            this.filePath = filePath;
        }

        public MultiDexContainer.DexEntry<T> getBase() {
            return base;
        }

        public String getDexName() {
            return name;
        }

        public File getFilePath() {
            return filePath;
        }
    }

    public void initialize() {
        // resolve classes in dex files
        for (DexEntry<? extends DexFile> dexEntry : dexFiles) {
            final DexFile dexFile = dexEntry.getDexFile();
            for (ClassDef defItem : dexFile.getClasses()) {
                String forClassName = Util.dottedClassName(defItem.getType());
                classesToDefItems.put(forClassName, new ClassInformation(dexEntry, defItem));
            }
        }

        // It is important to first resolve the classes, otherwise we will
        // produce an error during type resolution.
        for (MultiDexContainer.DexEntry<? extends DexFile> dexEntry : dexFiles) {
            final DexFile dexFile = dexEntry.getDexFile();
            if (dexFile instanceof DexBackedDexFile) {
                for (DexBackedTypeReference typeRef : ((DexBackedDexFile) dexFile).getTypeReferences()) {
                    String t = typeRef.getType();

                    Type st = DexType.toSoot(t);
                    if (st instanceof ArrayType) {
                        st = ((ArrayType) st).getBaseType();
                    }
                    String sootTypeName = st.toString();
                    if (!Scene.v().containsClass(sootTypeName)) {
                        if (st instanceof PrimitiveType || st instanceof VoidType || systemAnnotationNames.contains(sootTypeName)) {
                            // dex files contain references to the Type IDs of void
                            // primitive types - we obviously do not want them
                            // to be resolved
                            /*
                             * dex files contain references to the Type IDs of the system annotations. They are only visible to the Dalvik
                             * VM (for reflection, see vm/reflect/Annotations.cpp), and not to the user - so we do not want them to be
                             * resolved.
                             */
                            continue;
                        }
                        JavaIdentifierFactory.getInstance().getClassType(sootTypeName);
                    }
                }
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

    private final DexClassLoader dexLoader = createDexClassLoader();

    private static class ClassInformation {
        public DexEntry<? extends DexFile> dexEntry;
        public ClassDef classDefinition;

        public ClassInformation(DexEntry<? extends DexFile> entry, ClassDef classDef) {
            this.dexEntry = entry;
            this.classDefinition = classDef;
        }
    }

    private final Map<String, DexlibWrapper.ClassInformation> classesToDefItems = new HashMap<String, DexlibWrapper.ClassInformation>();
    private final Collection<DexEntry<? extends DexFile>> dexFiles;

    /**
     * Construct a DexlibWrapper from a dex file and stores its classes referenced by their name. No further process is done
     * here.
     */
    public DexlibWrapper(File dexSource) {
        try {
            List<DexFileProvider.DexContainer<? extends DexFile>> containers = new DexFileProvider().getDexFromSource(dexSource);
            this.dexFiles = new ArrayList<>(containers.size());
            for (DexFileProvider.DexContainer<? extends DexFile> container : containers) {
                this.dexFiles.add(container.getBase());
            }
        } catch (IOException e) {
            throw new ResolveException("IOException during dex parsing", dexSource.toPath());
        }
    }

}
