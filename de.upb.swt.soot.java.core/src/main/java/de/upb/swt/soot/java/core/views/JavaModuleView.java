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
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.core.*;
import de.upb.swt.soot.java.core.signatures.ModulePackageName;
import de.upb.swt.soot.java.core.signatures.ModuleSignature;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

/**
 * The JavaModuleView manages the Java classes of the application being analyzed for >=Java9
 *
 * @author Markus Schmidt
 */
public class JavaModuleView extends JavaView {

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
    JavaModuleInfo unnamedModuleInfo = JavaModuleInfo.getUnnamedModuleInfo();
    moduleInfoMap.put(unnamedModuleInfo.getModuleSignature(), unnamedModuleInfo);

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

  /**
   * returns true if packageName is exported by module of moduleSignature or if it is a package of
   * the very same module
   */
  private boolean isPackageDirectlyAccessibleByModule(
      ModuleSignature moduleSignature, ModulePackageName packageName) {

    if (packageName.getModuleSignature().equals(moduleSignature)) {
      return true;
    }

    Optional<JavaModuleInfo> moduleInfoOpt = getModuleInfo(moduleSignature);
    if (!moduleInfoOpt.isPresent()) {
      throw new ResolveException("ModuleDescriptor not available.");
    }
    JavaModuleInfo moduleInfo = moduleInfoOpt.get();

    if (moduleInfo.isAutomaticModule()) {
      // does not check if the package even exists in the automatic module!
      return true;
    }

    if (moduleInfo.equals(JavaModuleInfo.getUnnamedModuleInfo())) {
      // does not check if the package even exists!
      return true;
    }

    Collection<JavaModuleInfo.PackageReference> exports = moduleInfo.exports();
    Optional<JavaModuleInfo.PackageReference> filteredExportedPackages =
        exports.stream()
            .filter(packageReference -> packageReference.getPackageName().equals(packageName))
            .filter(pr -> pr.exportedTo(packageName.getModuleSignature()))
            .findAny();
    return filteredExportedPackages.isPresent();
  }

  @Nonnull
  public synchronized Optional<JavaSootClass> getClass(
      @Nonnull ModulePackageName entryPackage, @Nonnull JavaClassType type) {

    Optional<JavaModuleInfo> startOpt = getModuleInfo(entryPackage.getModuleSignature());
    if (!startOpt.isPresent()) {
      return Optional.empty();
    }

    JavaModuleInfo moduleInfo = startOpt.get();
    if (moduleInfo.equals(JavaModuleInfo.getUnnamedModuleInfo())) {
      // unnamed module

      // find type in all exported packages of modules on module path
      final List<AbstractClassSource<JavaSootClass>> foundClassSources =
          getAbstractClassSourcesForModules(type)
              .limit(1)
              .map(Optional::get)
              .collect(Collectors.toList());

      if (!foundClassSources.isEmpty()) {

        return buildClassFrom(foundClassSources.get(0));
      } else {
        // search in unnamed module itself
        // TODO: check if the correct IdentifierFactory (for Modules) is used to set a reference to
        // the unnamed module
        return super.getClass(type);
      }

    } else {
      // named module

      if (moduleInfo.isAutomaticModule()) {
        // automatic module can read every exported package of an explicit module

        // find the class in exported packages of modules
        final List<AbstractClassSource<JavaSootClass>> foundClassSources =
            getAbstractClassSourcesForModules(type)
                .limit(1)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (!foundClassSources.isEmpty()) {
          return buildClassFrom(foundClassSources.get(0));
        } else {
          // automatic module can access the unnamed module -> try to find in classpath (as if
          // modules do
          // not exist)
          return super.getClass(type);
        }
      } else {
        boolean targetIsSamePackage =
            type.getPackageName() instanceof ModulePackageName
                && ((ModulePackageName) type.getPackageName()).getModuleSignature()
                    == entryPackage.getModuleSignature();
        // explicit module
        final List<AbstractClassSource<JavaSootClass>> foundClassSources =
            getAbstractClassSourcesForModules(type)
                .map(Optional::get)
                // TODO: check implicit java.base from NON AsmModuleSurces
                .filter(
                    sc ->
                        targetIsSamePackage
                            || moduleInfo.requires().stream()
                                .anyMatch(
                                    req ->
                                        req.getModuleSignature()
                                            .equals(
                                                ((ModulePackageName)
                                                        sc.getClassType().getPackageName())
                                                    .getModuleSignature()))
                    /* || isTransitiveRequires(  moduleInfo, ((ModulePackageName)
                    sc.getClassType().getPackageName())
                    .getModuleSignature()) */ )
                .limit(1)
                .collect(Collectors.toList());

        if (!foundClassSources.isEmpty()) {
          return buildClassFrom(foundClassSources.get(0));
        } else {
          return Optional.empty();
        }
      }
    }
  }

  // TODO: expensive! cache results.. maybe union-find for transitive hull?
  private boolean isTransitiveRequires(
      JavaModuleInfo entryModuleInfo, ModuleSignature moduleSignature) {

    Optional<JavaModuleInfo> moduleInfoOpt = getModuleInfo(moduleSignature);

    if (!moduleInfoOpt.isPresent()) {
      return false;
    }
    JavaModuleInfo moduleInfo = moduleInfoOpt.get();

    if (moduleInfo.equals(entryModuleInfo)) {
      return true;
    }

    for (JavaModuleInfo.ModuleReference require : moduleInfo.requires()) {

      if (require.getModifiers().contains(ModuleModifier.REQUIRES_TRANSITIVE)) {
        return isTransitiveRequires(moduleInfo, require.getModuleSignature());
      }
    }

    return false;
  }

  @Nonnull
  private Stream<Optional<? extends AbstractClassSource<JavaSootClass>>>
      getAbstractClassSourcesForModules(@Nonnull JavaClassType type) {

    // find the class in exported packages of modules
    return getProject().getInputLocations().stream()
        .filter(inputLocation -> inputLocation instanceof ModuleInfoAnalysisInputLocation)
        .map(
            location -> {
              ClassLoadingOptions classLoadingOptions =
                  classLoadingOptionsSpecifier.apply(location);
              if (classLoadingOptions != null) {
                return location.getClassSource(type, classLoadingOptions);
              } else {
                return location.getClassSource(type);
              }
            })
        .filter(Optional::isPresent)
        .filter(
            cs -> {
              PackageName packageName = cs.get().getClassType().getPackageName();
              return packageName instanceof ModulePackageName
                  && isPackageDirectlyAccessibleByModule(
                      ((ModulePackageName) packageName).getModuleSignature(),
                      (ModulePackageName) type.getPackageName());
            });
  }

  @Nonnull
  public Set<ModuleSignature> getModules() {
    Set<ModuleSignature> modules = new HashSet<>();
    for (ModuleInfoAnalysisInputLocation moduleInputLocation : moduleInputLocations) {
      modules.addAll(moduleInputLocation.getModules());
    }
    return modules;
  }

  @Nonnull
  public synchronized Optional<JavaSootClass> getClasses(
      @Nonnull ModuleSignature startModuleSignature, @Nonnull ClassType type) {

    if (type.getPackageName() instanceof ModulePackageName) {
      // TODO: [ms] implement getting all classes that are visible from the startModuleSignature

    }

    return super.getClass(type);
  }
}
