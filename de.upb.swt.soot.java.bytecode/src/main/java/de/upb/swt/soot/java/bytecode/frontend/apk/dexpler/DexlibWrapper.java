package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler;

import com.sun.tools.classfile.Dependencies;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.VoidType;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.JavaTaggedSootClass;
import de.upb.swt.soot.java.core.views.JavaView;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.reference.DexBackedTypeReference;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer.DexEntry;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DexlibWrapper {

    private final static Set<String> systemAnnotationNames;
    private JavaView view; // TODO: KKwip

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

    private final DexClassLoader dexLoader;

    private static class ClassInformation {
        public DexEntry<? extends DexFile> dexEntry;
        public ClassDef classDefinition;

        public ClassInformation(DexEntry<? extends DexFile> entry, ClassDef classDef) {
            this.dexEntry = entry;
            this.classDefinition = classDef;
        }
    }

    private final Map<String, ClassInformation> classesToDefItems = new HashMap<String, ClassInformation>();
    private final Collection<DexEntry<? extends DexFile>> dexFiles;

    /**
     * Construct a DexlibWrapper from a dex file and stores its classes referenced by their name. No further process is done
     * here.
     */
    public DexlibWrapper(File dexSource, JavaView view) {
        this.view = view;
        this.dexLoader = new DexClassLoader(view);
        try {
            List<DexFileProvider.DexContainer<? extends DexFile>> containers = new DexFileProvider().getDexFromSource(dexSource.toPath());
            this.dexFiles = new ArrayList<>(containers.size());
            for (DexFileProvider.DexContainer<? extends DexFile> container : containers) {
                this.dexFiles.add(container.getBase());
            }
        } catch (IOException e) {
            throw new RuntimeException("IOException during dex parsing", e);
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
        for (DexEntry<? extends DexFile> dexEntry : dexFiles) {
            final DexFile dexFile = dexEntry.getDexFile();
            if (dexFile instanceof DexBackedDexFile) {
                for (DexBackedTypeReference typeRef : ((DexBackedDexFile) dexFile).getTypeReferences()) {
                    String t = typeRef.getType();

                    Type st = DexType.toSoot(t);
                    if (st instanceof ArrayType) {
                        st = ((ArrayType) st).getBaseType();
                    }
                    String sootTypeName = st.toString();
                    //TODO: KKwip
                    if (!view.getClasses().contains(sootTypeName)) {
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
                        // TODO: KKwip
                        //SootResolver.v().makeClassRef(sootTypeName);
                    }
                    // TODO: KKwip
                    //SootResolver.v().resolveClass(sootTypeName, SootClass.SIGNATURES);
                }
            }
        }
    }

    public JavaTaggedSootClass makeSootClass(SootClass sc, String className) {
        if (Util.isByteCodeClassName(className)) {
            className = Util.dottedClassName(className);
        }

        ClassInformation defItem = classesToDefItems.get(className);
        if (defItem != null) {
            // TODO: KKwip
            return dexLoader.makeSootClass(new JavaTaggedSootClass((JavaSootClass) sc), defItem.classDefinition, defItem.dexEntry);
        }

        throw new RuntimeException("Error: class not found in DEX files: " + className);
    }

}
