package sootup.apk.parser.dexpler;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;
import sootup.apk.parser.Util.*;
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
   *
   * @param dexSource the dex file from which the classes are taken for jimplification
   */
  public DexLibWrapper(File dexSource) {
    try {
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
