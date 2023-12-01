package dexpler;

import Util.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.reference.DexBackedTypeReference;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

/**
 * DexlibWrapper provides an entry point to the dexlib library from the smali project. Given a dex
 * file, it will use dexlib to retrieve all classes for further processing A call to getClass
 * retrieves the specific class to analyze further.
 */
public class DexLibWrapper {
  private static final Set<String> systemAnnotationNames;

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

  //    private final DexClassLoader dexLoader = createDexClassLoader();

  public static class ClassInformation {
    public MultiDexContainer.DexEntry<? extends DexFile> dexEntry;
    public ClassDef classDefinition;

    public ClassInformation(
        MultiDexContainer.DexEntry<? extends DexFile> entry, ClassDef classDef) {
      this.dexEntry = entry;
      this.classDefinition = classDef;
    }
  }

  private final Map<String, ClassInformation> classesToDefItems = new HashMap<>();
  private final Collection<MultiDexContainer.DexEntry<? extends DexFile>> dexFiles;

  /**
   * Construct a DexlibWrapper from a dex file and stores its classes referenced by their name. No
   * further process is done here.
   */
  public DexLibWrapper(File dexSource) {
    try {
      // TODO Change the api_version to some common place
      List<DexFileProvider.DexContainer<? extends DexFile>> containers =
          new DexFileProvider().getDexFromSource(dexSource, 15);
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
    for (MultiDexContainer.DexEntry<? extends DexFile> dexEntry : dexFiles) {
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

          Type st = DexUtil.toSootType(t, 0);
          if (st != null && st instanceof ArrayType) {
            st = ((ArrayType) st).getBaseType();
          }
          //                    String sootTypeName = st.toString();
          //                    if (!Scene.v().containsClass(sootTypeName)) {
          //                        if (st instanceof PrimType || st instanceof VoidType ||
          // systemAnnotationNames.contains(sootTypeName)) {
          //                            // dex files contain references to the Type IDs of void
          //                            // primitive types - we obviously do not want them
          //                            // to be resolved
          //                            /*
          //                             * dex files contain references to the Type IDs of the
          // system annotations. They are only visible to the Dalvik
          //                             * VM (for reflection, see vm/reflect/Annotations.cpp), and
          // not to the user - so we do not want them to be
          //                             * resolved.
          //                             */
          //                            continue;
          //                        }
          //                        SootResolver.v().makeClassRef(sootTypeName);
          //                    }
          //                    SootResolver.v().resolveClass(sootTypeName, SootClass.SIGNATURES);
        }
      }
    }
  }

  public ClassInformation getClassInformation(ClassType classType) {
    String className =
        Util.isByteCodeClassName(classType.toString())
            ? Util.dottedClassName(classType.toString())
            : classType.toString();
    ClassInformation defItem = classesToDefItems.get(className);
    return defItem;
  }
}
