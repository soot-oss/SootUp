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

import static de.upb.swt.soot.java.core.JavaModuleInfo.*;

import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.signatures.PackageName;
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
    JavaModuleInfo unnamedModuleInfo = getUnnamedModuleInfo();
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
  private boolean isPackageVisibleToModule(
      ModuleSignature moduleSignature, ModulePackageName packageName) {

    // is package in the same module? then no export is needed to access it
    if (packageName.getModuleSignature().equals(moduleSignature)) {
      return true;
    }

    Optional<JavaModuleInfo> moduleInfoOpt = getModuleInfo(moduleSignature);
    if (!moduleInfoOpt.isPresent()) {
      throw new ResolveException("ModuleDescriptor not available.");
    }
    JavaModuleInfo moduleInfo = moduleInfoOpt.get();

    // is the package exported by its module?
    if (moduleInfo.isAutomaticModule()) {
      // an automatic module exports all its packages
      // does not check if the package even exists in the automatic module!
      return true;
    }

    // is the package exported by its module?
    if (moduleInfo.isUnnamedModule()) {
      // the unnamed module exports all its packages
      // does not check if the package exists in the unnamed module!
      return true;
    }

    // is the package exported by its module?
    Collection<PackageReference> exports = moduleInfo.exports();
    Optional<PackageReference> filteredExportedPackages =
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
    if (moduleInfo.equals(getUnnamedModuleInfo())) {
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
        // explicit module

        boolean targetIsFromSameModule =
            type.getPackageName() instanceof ModulePackageName
                && ((ModulePackageName) type.getPackageName()).getModuleSignature()
                    == entryPackage.getModuleSignature();
        final Optional<? extends AbstractClassSource<JavaSootClass>> foundClassSources =
            getAbstractClassSourcesForModules(type)
                .map(Optional::get)
                .filter(
                    sc ->
                        targetIsFromSameModule
                            ||
                            // does the current module have a reads relation to the target module
                            moduleInfo.requires().stream()
                                .anyMatch(
                                    req ->
                                        req.getModuleSignature()
                                            .equals(
                                                ((ModulePackageName)
                                                        sc.getClassType().getPackageName())
                                                    .getModuleSignature()))

                            // or is it accessible via a transitive relation
                            || isTransitiveRequires(
                                moduleInfo,
                                ((ModulePackageName) sc.getClassType().getPackageName())
                                    .getModuleSignature()))
                .findAny();

        if (foundClassSources.isPresent()) {
          return buildClassFrom(foundClassSources.get());
        } else {
          return Optional.empty();
        }
      }
    }
  }

  // find a transitive relation from entryModuleInfo to moduleSignature
  private boolean isTransitiveRequires(
      JavaModuleInfo entryModuleInfo, ModuleSignature moduleSignature) {

    // TODO: expensive! cache results.. maybe union-find for transitive hull?

    Set<ModuleSignature> visited = new HashSet<>();
    visited.add(entryModuleInfo.getModuleSignature());

    Deque<ModuleSignature> stack = new ArrayDeque<>();
    stack.add(entryModuleInfo.getModuleSignature());

    while (!stack.isEmpty()) {
      Optional<JavaModuleInfo> moduleInfoOpt = getModuleInfo(stack.pop());
      if (!moduleInfoOpt.isPresent()) {
        continue;
      }
      JavaModuleInfo moduleInfo = moduleInfoOpt.get();

      for (ModuleReference require : moduleInfo.requires()) {
        ModuleSignature currentModuleSig = require.getModuleSignature();
        if (currentModuleSig.equals(moduleSignature)) {
          return true;
        } else {
          // TODO: check more specific? e.g. for ModuleModifier.REQUIRES_TRANSITIVE ||
          // require.getModifiers().contains(ModuleModifier.REQUIRES_MANDATED
          if (!visited.contains(currentModuleSig)) {
            stack.add(currentModuleSig);
          }
        }
        visited.add(currentModuleSig);
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
              // check if the package is exported by or living in the same module
              PackageName packageName = cs.get().getClassType().getPackageName();
              return packageName instanceof ModulePackageName
                  && isPackageVisibleToModule(
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

  /*
  /**
   * retrieves all visible classes starting from a module
   * * /
  @Nonnull
  public synchronized Collection<JavaSootClass> getClasses(
          @Nonnull ModuleSignature startModuleSignature, @Nonnull ClassType type) {

    Optional<JavaModuleInfo> moduleInfoOpt = getModuleInfo(startModuleSignature);

    if (!moduleInfoOpt.isPresent()) {
      // bad
      return Collections.emptyList();
    }

    JavaModuleInfo moduleInfo = moduleInfoOpt.get();
    if( moduleInfo.isUnnamedModule() ){
      return super.getClasses();
    }else {
      if (moduleInfo.isAutomaticModule()) {

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
                .filter(Optional::isPresent).map( cs -> buildClassFrom(cs.get()) )
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

      }else{
        // explicit module
       return super.getClass(type);

      }
    }


  }
  */
}
