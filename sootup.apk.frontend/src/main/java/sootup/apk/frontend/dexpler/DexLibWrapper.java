package sootup.apk.frontend.dexpler;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.*;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.jspecify.annotations.Nullable;
import sootup.apk.frontend.Util.*;
import sootup.core.types.ClassType;

/** Indexes the classes of the dex files of one input location by their name. */
public class DexLibWrapper {

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

  /**
   * @param containers the dex files, lowest priority first; a class defined twice is taken from the
   *     later one
   */
  public DexLibWrapper(List<DexFileProvider.DexContainer<? extends DexFile>> containers) {
    for (DexFileProvider.DexContainer<? extends DexFile> container : containers) {
      MultiDexContainer.DexEntry<? extends DexFile> dexEntry = container.getBase();
      for (ClassDef defItem : dexEntry.getDexFile().getClasses()) {
        classesToDefItems.put(
            DexUtil.dottedClassName(defItem.getType()), new ClassInformation(dexEntry, defItem));
      }
    }
  }

  public Set<String> getClassNames() {
    return Collections.unmodifiableSet(classesToDefItems.keySet());
  }

  @Nullable
  public ClassInformation getClassInformation(ClassType classType) {
    String className =
        DexUtil.isByteCodeClassName(classType.toString())
            ? DexUtil.dottedClassName(classType.toString())
            : classType.toString();
    return classesToDefItems.get(className);
  }
}
