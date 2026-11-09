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

public class DexFileProvider {

  private final Logger logger = LoggerFactory.getLogger(DexFileProvider.class);

  private int api_version;

  private static DexFileProvider instance;

  public static DexFileProvider getInstance() {
    if (instance == null) {
      instance = new DexFileProvider();
    }
    return instance;
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
  }

  /** Mapping of filesystem file (apk, dex, etc.) to mapping of dex name to dex file */
  private final Map<String, Map<String, DexContainer<? extends DexFile>>> dexMap = new HashMap<>();

  /**
   * Returns all dex files found in dex source
   *
   * @param dexSource Path to a jar, apk, dex, odex or a directory containing multiple dex files
   * @param api_version the version of the currently instrumenting APK
   * @return List of dex files derived from source
   * @throws IOException if the dex source is not parsed properly
   */
  public List<DexContainer<? extends DexFile>> getDexFromSource(File dexSource, int api_version)
      throws IOException {
    this.api_version = api_version;
    return getDexFromSource(dexSource, DEFAULT_PRIORITIZER);
  }

  public List<DexContainer<? extends DexFile>> getDexFromSource(
      File dexSource, Comparator<DexContainer<? extends DexFile>> prioritizer) throws IOException {
    ArrayList<DexContainer<? extends DexFile>> resultList = new ArrayList<>();
    List<File> allSources = allSourcesFromFile(dexSource);
    updateIndex(allSources);
    for (File theSource : allSources) {
      resultList.addAll(dexMap.get(theSource.getCanonicalPath()).values());
    }

    if (resultList.size() > 1) {
      resultList.sort(Collections.reverseOrder(prioritizer));
    }
    return resultList;
  }

  private void updateIndex(List<File> dexSources) throws IOException {
    for (File theSource : dexSources) {
      String key = theSource.getCanonicalPath();
      Map<String, DexContainer<? extends DexFile>> dexFiles = dexMap.get(key);
      if (dexFiles == null) {
        try {
          dexFiles = mappingForFile(theSource);
          dexMap.put(key, dexFiles);
        } catch (IOException e) {
          throw new IllegalStateException("Error parsing dex source", e);
        }
      }
    }
  }

  /**
   * @param dexSourceFile A file containing either one or multiple dex files (apk, zip, etc.) but no
   *     directory!
   * @return
   * @throws IOException
   */
  private Map<String, DexContainer<? extends DexFile>> mappingForFile(File dexSourceFile)
      throws IOException {
    // load dex files from apk/folder/file
    boolean multiple_dex = true;
    MultiDexContainer<? extends DexBackedDexFile> dexContainer =
        DexFileFactory.loadDexContainer(dexSourceFile, Opcodes.forApi(api_version));

    List<String> dexEntryNameList = dexContainer.getDexEntryNames();
    int dexFileCount = dexEntryNameList.size();

    if (dexFileCount < 1) {
      return Collections.emptyMap();
    }

    Map<String, DexContainer<? extends DexFile>> dexMap = new HashMap<>(dexFileCount);

    // report found dex files and add to list.
    // We do this in reverse order to make sure that we add the first entry if there is no
    // classes.dex file in single dex
    // mode
    ListIterator<String> entryNameIterator = dexEntryNameList.listIterator(dexFileCount);
    while (entryNameIterator.hasPrevious()) {
      String entryName = entryNameIterator.previous();
      MultiDexContainer.DexEntry<? extends DexFile> entry = dexContainer.getEntry(entryName);
      entryName = deriveDexName(entryName);
      logger.debug(
          String.format(
              "Found dex file '%s' with %d classes in '%s'",
              entryName, entry.getDexFile().getClasses().size(), dexSourceFile.getCanonicalPath()));

      if (multiple_dex) {
        dexMap.put(entryName, new DexContainer<>(entry, entryName, dexSourceFile));
      } else if (dexMap.isEmpty()
          && (entryName.equals("classes.dex") || !entryNameIterator.hasPrevious())) {
        // We prefer to have classes.dex in single dex mode.
        // If we haven't found a classes.dex until the last element, take the last!
        dexMap =
            Collections.singletonMap(
                entryName, new DexContainer<>(entry, entryName, dexSourceFile));
        if (dexFileCount > 1) {
          logger.warn(
              "Multiple dex files detected, only processing '"
                  + entryName
                  + "'. Use '-process-multiple-dex' option to process them all.");
        }
      }
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

  private static final Comparator<DexContainer<? extends DexFile>> DEFAULT_PRIORITIZER =
      (o1, o2) -> {
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
      };
}
