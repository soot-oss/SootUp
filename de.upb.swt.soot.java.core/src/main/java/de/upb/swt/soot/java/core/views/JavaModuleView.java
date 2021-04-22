package de.upb.swt.soot.java.core.views;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Markus Schmidt
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

import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.core.JavaModuleInfo;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.ModuleInfoAnalysisInputLocation;
import de.upb.swt.soot.java.core.signatures.ModulePackageName;
import de.upb.swt.soot.java.core.signatures.ModuleSignature;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * The JavaModuleView manages the Java classes of the application being analyzed for >=Java9
 *
 * @author Markus Schmidt
 */
public class JavaModuleView extends JavaView {

  @Nonnull final JavaModuleInfo unnamedModule = JavaModuleInfo.getUnnamedModuleInfo();
  @Nonnull final HashMap<ModuleSignature, JavaModuleInfo> moduleInfoMap = new HashMap<>();
  @Nonnull final List<ModuleInfoAnalysisInputLocation> moduleInputLocations;

  @Nonnull
  protected Function<AnalysisInputLocation<JavaSootClass>, ClassLoadingOptions>
      classLoadingOptionsSpecifier;

  public JavaModuleView(@Nonnull Project<? extends JavaView, JavaSootClass> project) {
    this(project, analysisInputLocation -> null);
  }

  /**
   * Creates a new instance of the {@link JavaModuleView} class.
   *
   * @param classLoadingOptionsSpecifier To use the default {@link ClassLoadingOptions} for an
   *     {@link AnalysisInputLocation}, simply return <code>null</code>, otherwise the desired
   *     options.
   */
  public JavaModuleView(
      @Nonnull Project<? extends JavaView, JavaSootClass> project,
      @Nonnull
          Function<AnalysisInputLocation<JavaSootClass>, ClassLoadingOptions>
              classLoadingOptionsSpecifier) {
    super(project);
    this.classLoadingOptionsSpecifier = classLoadingOptionsSpecifier;
    moduleInfoMap.put(unnamedModule.getModuleSignature(), unnamedModule);

    // store module input locations differently so that we can access the JavaModuleInfo
    moduleInputLocations =
        project.getInputLocations().stream()
            .filter(inputLocation -> inputLocation instanceof ModuleInfoAnalysisInputLocation)
            .map(inputLocation -> (ModuleInfoAnalysisInputLocation) inputLocation)
            .collect(Collectors.toList());
  }

  public Optional<JavaModuleInfo> getModuleInfo(ModuleSignature sig) {
    JavaModuleInfo moduleInfo = moduleInfoMap.get(sig);
    if (moduleInfo != null) {
      return Optional.of(moduleInfo);
    }

    for (ModuleInfoAnalysisInputLocation inputLocation : moduleInputLocations) {
      Optional<JavaModuleInfo> moduleInfoOpt = inputLocation.getModuleInfo(sig);
      if (moduleInfoOpt.isPresent()) {
        moduleInfoMap.put(sig, moduleInfoOpt.get());
        return moduleInfoOpt;
      }
    }
    return Optional.empty();
  }

  @Nonnull
  public synchronized Optional<JavaSootClass> getClass(
      @Nonnull ModulePackageName entryPackage, @Nonnull JavaClassType type) {
    // TODO: implement Accessibility checks here and

    return getClass(type);
  }

  @Nonnull
  public synchronized Optional<JavaSootClass> getClass(@Nonnull JavaClassType type) {

    PackageName packageName = type.getPackageName();
    if (packageName instanceof ModulePackageName) {
      ((ModulePackageName) packageName).getModuleSignature();
    }

    Optional<JavaSootClass> aClass = super.getClass(type);
    if (aClass.isPresent()) {

      aClass.get().getClassSource().getSourcePath();
      // FIXME [ms] get info from modulefinder/javamoduleview
      // moduleDependencyGraph.putIfAbsent(startModule, md );
    }

    return aClass;
  }

  @Nonnull
  public synchronized Optional<JavaSootClass> getClasses(
      @Nonnull ClassType scope, @Nonnull ClassType type) {

    if (type.getPackageName() instanceof ModulePackageName) {
      // use ModulePackageName instead of startModule
    }

    // TODO: [ms] implement getting all classes for that scope
    return super.getClass(type);
  }
}
