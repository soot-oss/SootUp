package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2018 Raja Vallée-Rai and others
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

import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.core.JavaSootClass;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.jf.dexlib2.iface.MultiDexContainer.DexEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class providing dex files from a given source, e.g., jar, apk, dex, folder containing multiple
 * dex files
 *
 * @author Manuel Benz created on 16.10.17
 */
public class DexFileProvider implements ClassProvider<JavaSootClass> {
  private static final Logger logger = LoggerFactory.getLogger(DexFileProvider.class);
  private boolean process_multiple_dex = true;
  private boolean search_dex_in_archives = true;
  private boolean verbose = true;
  private int android_api_version = 10;

  private static final Comparator<DexContainer<? extends DexFile>> DEFAULT_PRIORITIZER =
      new Comparator<DexContainer<? extends DexFile>>() {

        @Override
        public int compare(DexContainer<? extends DexFile> o1, DexContainer<? extends DexFile> o2) {
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

  /** Mapping of filesystem file (apk, dex, etc.) to mapping of dex name to dex file */
  private final Map<Path, Map<Path, DexContainer<? extends DexFile>>> dexMap = new HashMap<>();

  /**
   * Returns all dex files found in dex source sorted by the default dex prioritizer
   *
   * @param dexSource Path to a jar, apk, dex, odex or a directory containing multiple dex files
   * @return List of dex files derived from source
   */
  public List<DexFileProvider.DexContainer<? extends DexFile>> getDexFromSource(Path dexSource)
      throws IOException {
    return getDexFromSource(dexSource, DEFAULT_PRIORITIZER);
  }

  /**
   * Returns all dex files found in dex source sorted by the default dex prioritizer
   *
   * @param dexSource Path to a jar, apk, dex, odex or a directory containing multiple dex files
   * @param prioritizer A comparator that defines the ordering of dex files in the result list
   * @return List of dex files derived from source
   */
  public List<DexContainer<? extends DexFile>> getDexFromSource(
      Path dexSource, Comparator<DexContainer<? extends DexFile>> prioritizer) throws IOException {
    ArrayList<DexContainer<? extends DexFile>> resultList = new ArrayList<>();
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
   * @param dexSource Path to a jar, apk, dex, odex or a directory containing multiple dex files
   * @return Dex file with given name in dex source
   * @throws ResolveException If no dex file with the given name exists
   */
  public DexContainer<? extends DexFile> getDexFromSource(Path dexSource, String dexName)
      throws IOException {
    List<Path> allSources = allSourcesFromFile(dexSource);
    updateIndex(allSources);

    // we take the first dex we find with the given name
    for (Path theSource : allSources) {
      DexContainer<? extends DexFile> dexFile = dexMap.get(theSource).get(dexName);
      if (dexFile != null) {
        return dexFile;
      }
    }

    throw new ResolveException(
        "Dex file with name '" + dexName + "' not found in " + dexSource, dexSource);
  }

  private List<Path> allSourcesFromFile(Path dexSource) {
    if (Files.isDirectory(dexSource)) {
      List<Path> dexFiles = getAllDexFilesInDirectory(dexSource);
      if (dexFiles.size() > 1 && !process_multiple_dex) {
        Path path = dexFiles.get(0);
        logger.warn(
            "Multiple dex files detected, only processing '"
                + path
                + "'. Use '-process-multiple-dex' option to process them all.");
        return Collections.singletonList(path);
      } else {
        return dexFiles;
      }
    } else {
      String ext =
          com.google.common.io.Files.getFileExtension(dexSource.getFileName().toString())
              .toLowerCase();
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
   * @param dexSourceFile A file containing either one or multiple dex files (apk, zip, etc.) but no
   *     directory!
   * @return
   * @throws IOException
   */
  private Map<Path, DexFileProvider.DexContainer<? extends DexFile>> mappingForFile(
      Path dexSourceFile) throws IOException {
    int api = android_api_version; // Scene.v().getAndroidAPIVersion();
    boolean multiple_dex = process_multiple_dex;

    // load dex files from apk/folder/file
    MultiDexContainer<? extends DexBackedDexFile> dexContainer =
        DexFileFactory.loadDexContainer(dexSourceFile.toFile(), Opcodes.forApi(api));

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
    // We do this in reverse order to make sure that we add the first entry if there is no
    // classes.dex file in single dex
    // mode
    ListIterator<String> entryNameIterator = dexEntryNameList.listIterator(dexFileCount);
    while (entryNameIterator.hasPrevious()) {
      String entryName = entryNameIterator.previous();
      MultiDexContainer.DexEntry<? extends DexFile> entry = dexContainer.getEntry(entryName);
      entryName = deriveDexName(entryName);
      logger.debug(
          ""
              + String.format(
                  "Found dex file '%s' with %d classes in '%s'",
                  entryName, entry.getDexFile().getClasses().size(), dexSourceFile));

      if (multiple_dex) {
        dexMap.put(
            Paths.get(entryName),
            new DexFileProvider.DexContainer<>(entry, entryName, dexSourceFile));
      } else if (dexMap.isEmpty()
          && (entryName.equals("classes.dex") || !entryNameIterator.hasPrevious())) {
        // We prefer to have classes.dex in single dex mode.
        // If we haven't found a classes.dex until the last element, take the last!
        dexMap =
            Collections.singletonMap(
                Paths.get(entryName),
                new DexFileProvider.DexContainer<>(entry, entryName, dexSourceFile));
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

  private String deriveDexName(String entryName) {
    return new File(entryName).getName();
  }

  private List<Path> getAllDexFilesInDirectory(Path path) {
    try {
      return Files.walk(path)
          .filter(
              p -> {
                return p.toString().endsWith(".dex") && Files.isDirectory(p);
              })
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new ResolveException("Error while finding .dex file", path, e);
    }
  }

  @Override
  public AbstractClassSource<JavaSootClass> createClassSource(
      AnalysisInputLocation<? extends SootClass<?>> inputLocation,
      Path sourcePath,
      ClassType classSignature) {
    return null;
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.DEX;
  }

  public static final class DexContainer<T extends DexFile> {
    private final DexEntry<T> base;
    private final String name;
    private final Path filePath;

    public DexContainer(DexEntry<T> base, String name, Path filePath) {
      this.base = base;
      this.name = name;
      this.filePath = filePath;
    }

    public DexEntry<T> getBase() {
      return base;
    }

    public String getDexName() {
      return name;
    }

    public Path getFilePath() {
      return filePath;
    }
  }
}
