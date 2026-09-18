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

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Loads the dex files of a source. It keeps no state, so nothing outlives the caller. */
public class DexFileProvider {

  private final Logger logger = LoggerFactory.getLogger(DexFileProvider.class);

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
  }

  /**
   * Returns all dex files found in dex source
   *
   * @param dexSource Path to a jar, apk, dex, odex or a directory containing multiple dex files
   * @param apiVersion the API level, only used for odex files
   * @return List of dex files derived from source, lowest priority first
   * @throws IOException if the dex source is not parsed properly
   */
  public List<DexContainer<? extends DexFile>> getDexFromSource(File dexSource, int apiVersion)
      throws IOException {
    return getDexFromSource(dexSource, apiVersion, DEFAULT_PRIORITIZER);
  }

  public List<DexContainer<? extends DexFile>> getDexFromSource(
      File dexSource, int apiVersion, Comparator<DexContainer<? extends DexFile>> prioritizer)
      throws IOException {
    ArrayList<DexContainer<? extends DexFile>> resultList = new ArrayList<>();
    for (File theSource : allSourcesFromFile(dexSource)) {
      resultList.addAll(mappingForFile(theSource, apiVersion).values());
    }

    // lowest priority first, because later dex files overwrite earlier ones when indexed
    if (resultList.size() > 1) {
      resultList.sort(Collections.reverseOrder(prioritizer));
    }
    return resultList;
  }

  /**
   * @param dexSourceFile A file containing either one or multiple dex files (apk, zip, etc.) but no
   *     directory!
   */
  private Map<String, DexContainer<? extends DexFile>> mappingForFile(
      File dexSourceFile, int apiVersion) throws IOException {
    // dex files carry their version in the header; only odex needs the device API level
    Opcodes opcodes =
        dexSourceFile.getName().toLowerCase().endsWith(".odex") ? Opcodes.forApi(apiVersion) : null;
    MultiDexContainer<? extends DexBackedDexFile> dexContainer =
        DexFileFactory.loadDexContainer(dexSourceFile, opcodes);

    List<String> dexEntryNameList = dexContainer.getDexEntryNames();
    Map<String, DexContainer<? extends DexFile>> dexMap = new HashMap<>(dexEntryNameList.size());
    for (String entryName : dexEntryNameList) {
      MultiDexContainer.DexEntry<? extends DexFile> entry = dexContainer.getEntry(entryName);
      String name = deriveDexName(entryName);
      logger.debug(
          "Found dex file '{}' with {} classes in '{}'",
          name,
          entry.getDexFile().getClasses().size(),
          dexSourceFile.getCanonicalPath());
      dexMap.put(name, new DexContainer<>(entry, name, dexSourceFile));
    }
    return Collections.unmodifiableMap(dexMap);
  }

  public List<File> allSourcesFromFile(File dexSource) {
    if (dexSource.isDirectory()) {
      List<File> dexFiles = getAllDexFilesInDirectory(dexSource);
      return dexFiles;
    } else {
      String ext = com.google.common.io.Files.getFileExtension(dexSource.getName()).toLowerCase();
      if ((ext.equals("jar") || ext.equals("zip"))) {
        return Collections.emptyList();
      } else {
        return Collections.singletonList(dexSource);
      }
    }
  }

  private List<File> getAllDexFilesInDirectory(File path) {
    Queue<File> toVisit = new ArrayDeque<File>();
    Set<File> visited = new HashSet<File>();
    List<File> ret = new ArrayList<File>();
    toVisit.add(path);
    while (!toVisit.isEmpty()) {
      File cur = toVisit.poll();
      if (visited.contains(cur)) {
        continue;
      }
      visited.add(cur);
      if (cur.isDirectory()) {
        toVisit.addAll(Arrays.asList(cur.listFiles()));
      } else if (cur.isFile() && cur.getName().endsWith(".dex")) {
        ret.add(cur);
      }
    }
    return ret;
  }

  private String deriveDexName(String entryName) {
    return new File(entryName).getName();
  }

  /**
   * Orders dex files like Android's class loader: classes.dex, classes2.dex, classes3.dex, ... and
   * then everything else. A smaller value means a higher priority.
   */
  static final Comparator<DexContainer<? extends DexFile>> DEFAULT_PRIORITIZER =
      (o1, o2) -> {
        String s1 = o1.getDexName(), s2 = o2.getDexName();
        if (s1.equals("classes.dex")) {
          return s2.equals("classes.dex") ? 0 : -1;
        } else if (s2.equals("classes.dex")) {
          return 1;
        }

        boolean s1IsMultiDex = isSecondaryDexName(s1);
        boolean s2IsMultiDex = isSecondaryDexName(s2);
        if (s1IsMultiDex && s2IsMultiDex) {
          // numeric, so classes9.dex comes before classes10.dex
          return Long.compare(secondaryDexNumber(s1), secondaryDexNumber(s2));
        } else if (s1IsMultiDex) {
          return -1;
        } else if (s2IsMultiDex) {
          return 1;
        }
        return s1.compareTo(s2);
      };

  private static boolean isSecondaryDexName(String name) {
    // Android skips classes1.dex
    return name.matches("classes([2-9]|[1-9]\\d+)\\.dex");
  }

  private static long secondaryDexNumber(String name) {
    return Long.parseLong(name.substring("classes".length(), name.length() - ".dex".length()));
  }
}
