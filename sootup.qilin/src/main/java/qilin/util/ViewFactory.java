/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.views.View;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/** Builds a {@link View} over an application's classpath, library jars and a JRE. */
public final class ViewFactory {
  private static final Logger logger = LoggerFactory.getLogger(ViewFactory.class);

  private ViewFactory() {}

  public static View createView(String appPath, String libPath, String jrePath) {
    /**
     * Set the soot class path to point to the default class path appended with the app path (the
     * classes dir or the application jar) and jar files in the library dir of the application.
     */
    List<String> classPaths = new ArrayList<>();
    List<AnalysisInputLocation> analysisInputLocations = new ArrayList<>();
    // note that the order is important!
    classPaths.add(appPath);
    analysisInputLocations.add(new JavaClassPathAnalysisInputLocation(appPath));
    classPaths.addAll(getLibJars(libPath));
    for (String clazzPath : getLibJars(libPath)) {
      analysisInputLocations.add(new JavaClassPathAnalysisInputLocation(clazzPath));
    }
    classPaths.addAll(getJreJars(jrePath));
    for (String clazzPath : getJreJars(jrePath)) {
      analysisInputLocations.add(
          new JavaClassPathAnalysisInputLocation(clazzPath, SourceType.Library));
    }
    final String classpath = String.join(File.pathSeparator, classPaths);
    logger.info("Soot ClassPath: {}", classpath);
    return new JavaView(analysisInputLocations);
  }

  /**
   * Like {@link #createView(String, String, String)}, but resolves the JRE against the current
   * JVM's own runtime image ({@link DefaultRuntimeAnalysisInputLocation}) instead of an external
   * JRE stub directory. Only safe for analyses that don't need {@code FakeMainFactory}'s pre-JDK9
   * JVM-bootstrap modeling (i.e. {@code PointerAnalysisConfig.singleEntry(true)}) - that block
   * references JDK6/7/8-internal classes (e.g. {@code sun.misc.Launcher$AppClassLoader}) removed by
   * the JPMS module system in JDK9+.
   */
  public static View createView(String appPath, String libPath) {
    List<String> classPaths = new ArrayList<>();
    List<AnalysisInputLocation> analysisInputLocations = new ArrayList<>();
    // note that the order is important!
    classPaths.add(appPath);
    analysisInputLocations.add(new JavaClassPathAnalysisInputLocation(appPath));
    classPaths.addAll(getLibJars(libPath));
    for (String clazzPath : getLibJars(libPath)) {
      analysisInputLocations.add(new JavaClassPathAnalysisInputLocation(clazzPath));
    }
    analysisInputLocations.add(new DefaultRuntimeAnalysisInputLocation());
    logger.info(
        "Soot ClassPath: {} + current JVM runtime", String.join(File.pathSeparator, classPaths));
    return new JavaView(analysisInputLocations);
  }

  /** Returns a collection of files, one for each of the jar files in the app's lib folder */
  private static Collection<String> getLibJars(String LIB_PATH) {
    if (LIB_PATH == null) {
      return Collections.emptySet();
    }
    File libFile = new File(LIB_PATH);
    if (libFile.exists()) {
      if (libFile.isDirectory()) {
        return FileUtils.listFiles(libFile, new String[] {"jar"}, true).stream()
            .map(File::toString)
            .collect(Collectors.toList());
      } else if (libFile.isFile()) {
        if (libFile.getName().endsWith(".jar")) {
          return Collections.singletonList(LIB_PATH);
        }
        logger.error(
            "Project not configured properly. Application library path {} is not a jar file.",
            libFile);
        System.exit(1);
      }
    }
    logger.error(
        "Project not configured properly. Application library path {} is not correct.", libFile);
    System.exit(1);
    return null;
  }

  private static Collection<String> getJreJars(String JRE) {
    if (JRE == null) {
      return Collections.emptySet();
    }
    final String jreLibDir = JRE + File.separator + "lib";
    return FileUtils.listFiles(new File(jreLibDir), new String[] {"jar"}, false).stream()
        .map(File::toString)
        .collect(Collectors.toList());
  }
}
