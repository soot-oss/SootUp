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
import de.upb.swt.soot.core.inputlocation.EmptyClassLoadingOptions;
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

  @Nonnull
  protected Function<AnalysisInputLocation<? extends JavaSootClass>, ClassLoadingOptions>
      classLoadingOptionsSpecifier;

  public JavaModuleView(@Nonnull Project<? extends JavaView, ? extends JavaSootClass> project) {
    this(project, analysisInputLocation -> EmptyClassLoadingOptions.Default);
  }

  /**
   * Creates a new instance of the {@link JavaModuleView} class.
   *
   * @param classLoadingOptionsSpecifier To use the default {@link ClassLoadingOptions} for an
   *     {@link AnalysisInputLocation}, simply return <code>null</code>, otherwise the desired
   *     options.
   */
  public JavaModuleView(
      @Nonnull Project<? extends JavaView, ? extends JavaSootClass> project,
      @Nonnull
          Function<AnalysisInputLocation<? extends JavaSootClass>, ClassLoadingOptions>
              classLoadingOptionsSpecifier) {
    super(project);
    this.classLoadingOptionsSpecifier = classLoadingOptionsSpecifier;
    JavaModuleInfo unnamedModuleInfo = getUnnamedModuleInfo();
    moduleInfoMap.put(unnamedModuleInfo.getModuleSignature(), unnamedModuleInfo);
  }

  @Nonnull
  @Override
  public JavaModuleIdentifierFactory getIdentifierFactory() {
    return JavaModuleIdentifierFactory.getInstance();
  }

  @Nonnull
  @Override
  public JavaModuleProject getProject() {
    return (JavaModuleProject) super.getProject();
  }

  public Optional<JavaModuleInfo> getModuleInfo(ModuleSignature sig) {
    JavaModuleInfo moduleInfo = moduleInfoMap.get(sig);
    if (moduleInfo != null) {
      return Optional.of(moduleInfo);
    }

    for (ModuleInfoAnalysisInputLocation inputLocation :
        getProject().getModuleInfoAnalysisInputLocation()) {
      Optional<JavaModuleInfo> moduleInfoOpt = inputLocation.getModuleInfo(sig);
      if (moduleInfoOpt.isPresent()) {
        moduleInfoMap.put(sig, moduleInfoOpt.get());
        return moduleInfoOpt;
      }
    }
    return Optional.empty();
  }

  /**
   * returns true if packageName is exported by module from packageName (to moduleSignature) r if it
   * is a package of the very same module
   */
  private boolean isPackageVisibleToModule(
      ModuleSignature moduleSignature, ModulePackageName packageName) {

    // is package in the same module? then no export is needed to access it
    if (packageName.getModuleSignature().equals(moduleSignature)) {
      return true;
    }

    Optional<JavaModuleInfo> moduleInfoOpt = getModuleInfo(packageName.getModuleSignature());
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
            .findAny();
    return filteredExportedPackages.isPresent();
  }

  @Override
  @Nonnull
  protected Optional<? extends AbstractClassSource<? extends JavaSootClass>> getAbstractClass(
      @Nonnull ClassType type) {

    Optional<? extends AbstractClassSource<JavaSootClass>> cs =
        getProject().getModuleInfoAnalysisInputLocation().stream()
            .map(location -> location.getClassSource(type, this))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findAny();

    if (cs.isPresent()) {
      return cs;
    }

    return super.getAbstractClass(type);
  }

  @Nonnull
  public synchronized Optional<JavaSootClass> getClass(
      @Nonnull ModulePackageName entryPackage, @Nonnull JavaClassType type) {

    System.out.println("called 2");
    Optional<JavaModuleInfo> startOpt = getModuleInfo(entryPackage.getModuleSignature());
    if (!startOpt.isPresent()) {
      return Optional.empty();
    }

    JavaModuleInfo moduleInfo = startOpt.get();
    if (moduleInfo.isUnnamedModule()) {
      // unnamed module

      // find type in all exported packages of modules on module path first
      final List<AbstractClassSource<JavaSootClass>> foundClassSources =
          getAbstractClassSourcesForModules(entryPackage.getModuleSignature(), type)
              .limit(1)
              .map(Optional::get)
              .collect(Collectors.toList());

      if (!foundClassSources.isEmpty()) {

        return buildClassFrom(foundClassSources.get(0));
      } else {
        PackageName packageName = type.getPackageName();
        if (packageName instanceof ModulePackageName
            && ((ModulePackageName) packageName).getModuleSignature().isUnnamedModule()) {
          // if not already found on module path AND the target class is in unnamed module: search
          // in unnamed module itself
          return getClass(type);
        }
      }

    } else {
      // named module

      if (moduleInfo.isAutomaticModule()) {
        // automatic module can read every exported package of an explicit module

        // find the class in exported packages of modules
        final List<AbstractClassSource<JavaSootClass>> foundClassSources =
            getAbstractClassSourcesForModules(entryPackage.getModuleSignature(), type)
                .limit(1)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (!foundClassSources.isEmpty()) {
          return buildClassFrom(foundClassSources.get(0));
        } else {
          // automatic module can access the unnamed module -> try to find in classpath (as if
          // modules do not exist)
          return super.getClass(type);
        }
      } else {
        // explicit module
        boolean targetIsFromSameModule =
            type.getPackageName() instanceof ModulePackageName
                && ((ModulePackageName) type.getPackageName()).getModuleSignature()
                    == entryPackage.getModuleSignature();

        final Optional<? extends AbstractClassSource<JavaSootClass>> foundClassSources =
            getAbstractClassSourcesForModules(entryPackage.getModuleSignature(), type)
                .map(Optional::get)
                .filter(
                    sc -> {
                      if (targetIsFromSameModule) {
                        return true;
                      }
                      // does the current module have a reads relation to the target module
                      if (moduleInfo.requires().stream()
                          .anyMatch(
                              req ->
                                  req.getModuleSignature()
                                      .equals(
                                          ((ModulePackageName) sc.getClassType().getPackageName())
                                              .getModuleSignature()))) {
                        return true;
                      }

                      // or is it accessible via a transitive relation
                      return isTransitiveRequires(
                              moduleInfo,
                              ((ModulePackageName) sc.getClassType().getPackageName())
                                  .getModuleSignature())
                          || isProvidedInterfaceImplementation((JavaClassType) sc.getClassType());
                    })
                .findAny();

        if (foundClassSources.isPresent()) {
          return buildClassFrom(foundClassSources.get());
        } else {
          return Optional.empty();
        }
      }
    }

    return Optional.empty();
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

      if (moduleInfo.isAutomaticModule() || moduleInfo.isUnnamedModule()) {
        // automatic module can read everything but its not "forwarding" require transitive!
        continue;
      }

      for (ModuleReference require : moduleInfo.requires()) {
        ModuleSignature requireModuleSig = require.getModuleSignature();
        if (moduleSignature.equals(requireModuleSig)) {
          return true;
        } else {
          if (!visited.contains(requireModuleSig)) {
            stack.add(requireModuleSig);
          }
        }
        visited.add(requireModuleSig);
      }
    }

    return false;
  }

  /** return the classes which belong to the moduleSignature */
  @Nonnull
  public synchronized Collection<JavaSootClass> getModuleClasses(
      @Nonnull ModuleSignature moduleSignature) {

    Optional<JavaModuleInfo> startOpt = getModuleInfo(moduleSignature);
    if (!startOpt.isPresent()) {
      return Collections.emptyList();
    }

    Stream<? extends AbstractClassSource<? extends JavaSootClass>> stream;
    JavaModuleInfo moduleInfo = startOpt.get();
    if (moduleInfo.isUnnamedModule()) {
      // unnamed module
      stream =
          getProject().getInputLocations().stream()
              .flatMap(input -> input.getClassSources(getIdentifierFactory(), this).stream());

    } else {
      // named module
      if (moduleInfo.isAutomaticModule()) {
        // the automatic module
        stream =
            Stream.concat(
                getProject().getInputLocations().stream()
                    .flatMap(
                        input -> {
                          // classpath
                          return input.getClassSources(getIdentifierFactory(), this).stream()
                              .filter(
                                  cs ->
                                      moduleSignature.equals(
                                          ((ModulePackageName) cs.getClassType().getPackageName())
                                              .getModuleSignature()));
                        }),
                getProject().getModuleInfoAnalysisInputLocation().stream()
                    .flatMap(
                        input -> {
                          // modulepath
                          return ((ModuleInfoAnalysisInputLocation) input)
                              .getModulesClassSources(moduleSignature, getIdentifierFactory(), this)
                                  .stream();
                        }));

      } else {
        // explicit module
        stream =
            getProject().getModuleInfoAnalysisInputLocation().stream()
                .flatMap(
                    input ->
                        input.getModulesClassSources(moduleSignature, getIdentifierFactory(), this)
                            .stream());
      }
    }
    return stream
        .map(this::buildClassFrom)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  /*
      @Nonnull
      public synchronized Collection<JavaSootClass> getTransitiveClasses(@Nonnull ModuleSignature moduleSignature) {

          Optional<JavaModuleInfo> startOpt = getModuleInfo(moduleSignature);
          if (!startOpt.isPresent()) {
              return Collections.emptyList();
          }

          Stream<? extends AbstractClassSource<JavaSootClass>> stream;
          JavaModuleInfo moduleInfo = startOpt.get();
          if (moduleInfo.isUnnamedModule()) {
              // unnamed module -> access to all (non)modules

              stream = getProject().getInputLocations().stream().flatMap(input -> input.getClassSources(getIdentifierFactory()).stream());
              // FIXME: needs generics PR to be merged: uncomment stream = Stream.concat( super.getClasses().stream(), stream);

          } else {
              // named module

              if (moduleInfo.isAutomaticModule()) {
                  // automatic module can read every exported package of an explicit module and the unnamed module

                  stream = getProject().getInputLocations().stream().flatMap(input ->
                  {
                      if (input instanceof ModuleInfoAnalysisInputLocation) {
                          // modulepath
                          return input.getClassSources(getIdentifierFactory()).stream(); // .filter(cs -> isTransitiveRequires(moduleInfo, ((ModulePackageName) cs.getClassType().getPackageName()).getModuleSignature()));
                      } else {
                          // classpath
                          return input.getClassSources(getIdentifierFactory()).stream();
                      }
                  });

              } else {
                  // explicit module

                  stream = getProject().getModuleInfoAnalysisInputLocation().stream().flatMap(input ->
                          input.getClassSources(getIdentifierFactory()).stream().filter(cs -> isTransitiveRequires(moduleInfo, ((ModulePackageName) cs.getClassType().getPackageName()).getModuleSignature())));

              }
          }

          return stream.map(this::buildClassFrom).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
      }
  */

  @Nonnull
  private Stream<? extends Optional<? extends AbstractClassSource<JavaSootClass>>>
      getAbstractClassSourcesForModules(ModuleSignature moduleSig, @Nonnull JavaClassType type) {

    // find the class in exported packages of modules
    return getProject().getModuleInfoAnalysisInputLocation().stream()
        .map(location -> location.getClassSource(type, this))
        .filter(Optional::isPresent)
        .filter(
            cs -> {
              // check if the package is exported by or living in the same module
              return isPackageVisibleToModule(moduleSig, (ModulePackageName) type.getPackageName())
                  || isProvidedInterfaceImplementation(type);
            });
  }

  private boolean isProvidedInterfaceImplementation(@Nonnull JavaClassType type) {
    ModulePackageName packageName = (ModulePackageName) type.getPackageName();
    JavaModuleInfo moduleInfo = getModuleInfo(packageName.getModuleSignature()).get();

    for (InterfaceReference provides : moduleInfo.provides()) {
      JavaClassType interfaceType = provides.getInterfaceType();
      String packageName1 = interfaceType.getPackageName().getPackageName();
      String packageName2 = type.getPackageName().getPackageName();
      if (packageName1.equals(packageName2)) {
        if (interfaceType.getClassName().equals(type.getClassName())) {
          return true;
        }
      }
    }

    return false;
  }

  @Nonnull
  public Set<ModuleSignature> getNamedModules() {
    Set<ModuleSignature> modules = new HashSet<>();
    for (ModuleInfoAnalysisInputLocation moduleInputLocation :
        getProject().getModuleInfoAnalysisInputLocation()) {
      modules.addAll(moduleInputLocation.getModules());
    }
    return modules;
  }

  @Override
  protected synchronized void resolveAll() {
    if (isFullyResolved) {
      return;
    }

    getProject().getInputLocations().stream()
        .flatMap(location -> location.getClassSources(getIdentifierFactory(), this).stream())
        .forEach(this::buildClassFrom);

    getProject().getModuleInfoAnalysisInputLocation().stream()
        .flatMap(location -> location.getClassSources(getIdentifierFactory(), this).stream())
        .forEach(this::buildClassFrom);
    isFullyResolved = true;
  }
}
