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
import sootup.core.types.ClassType;

/**
 * DexlibWrapper provides an entry point to the dexlib library from the smali project. Given a dex
 * file, it will use dexlib to retrieve all classes for further processing A call to getClass
 * retrieves the specific class to analyze further.
 */
public class DexLibWrapper {

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
          DexFileProvider.getInstance()
              .getDexFromSource(dexSource, DexUtil.getAndroidVersionInfo().getApi_version());
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
        String forClassName = DexUtil.dottedClassName(defItem.getType());
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
          // TODO Still now did not find the usecase for this, but according to Soot it was said
          // this case can happen, let's see.....
        }
      }
    }
  }

  public ClassInformation getClassInformation(ClassType classType) {
    String className =
        DexUtil.isByteCodeClassName(classType.toString())
            ? DexUtil.dottedClassName(classType.toString())
            : classType.toString();
    ClassInformation defItem = classesToDefItems.get(className);
    return defItem;
  }
}
