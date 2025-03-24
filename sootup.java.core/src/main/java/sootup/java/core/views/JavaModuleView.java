package sootup.java.core.views;

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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.core.cache.provider.ClassCacheProvider;
import sootup.core.cache.provider.FullCacheProvider;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.java.core.*;
import sootup.java.core.signatures.ModulePackageName;
import sootup.java.core.signatures.ModuleSignature;
import sootup.java.core.types.JavaClassType;

/**
 * The JavaModuleView manages the Java classes of the application being analyzed for &gt;=Java9
 *
 * @author Markus Schmidt
 */
public class JavaModuleView extends JavaView {

  @NonNull private final List<ModuleInfoAnalysisInputLocation> moduleInfoAnalysisInputLocations;
  @NonNull final HashMap<ModuleSignature, JavaModuleInfo> moduleInfoMap = new HashMap<>();

  public JavaModuleView(
      @NonNull List<AnalysisInputLocation> inputLocations,
      @NonNull List<ModuleInfoAnalysisInputLocation> moduleInputLocations) {
    this(inputLocations, moduleInputLocations, new FullCacheProvider());
  }

  /** Creates a new instance of the {@link JavaModuleView} class. */
  public JavaModuleView(
      @NonNull List<AnalysisInputLocation> inputLocations,
      @NonNull List<ModuleInfoAnalysisInputLocation> moduleInputLocations,
      @NonNull ClassCacheProvider cacheProvider) {
    super(inputLocations, cacheProvider, JavaModuleIdentifierFactory.getInstance());
    this.moduleInfoAnalysisInputLocations = moduleInputLocations;
    JavaModuleInfo unnamedModuleInfo = JavaModuleInfo.getUnnamedModuleInfo();
    moduleInfoMap.put(unnamedModuleInfo.getModuleSignature(), unnamedModuleInfo);
  }

  @NonNull
  public Optional<JavaModuleInfo> getModuleInfo(ModuleSignature sig) {
    JavaModuleInfo moduleInfo = moduleInfoMap.get(sig);
    if (moduleInfo != null) {
      return Optional.of(moduleInfo);
    }

    for (ModuleInfoAnalysisInputLocation inputLocation : moduleInfoAnalysisInputLocations) {
      Optional<JavaModuleInfo> moduleInfoOpt = inputLocation.getModuleInfo(sig, this);
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
    if (moduleInfoOpt.isEmpty()) {
      throw new IllegalStateException("ModuleDescriptor not available.");
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
    Collection<JavaModuleInfo.PackageReference> exports = moduleInfo.exports();
    Optional<JavaModuleInfo.PackageReference> filteredExportedPackages =
        exports.stream()
            .filter(packageReference -> packageReference.getPackageName().equals(packageName))
            .findAny();
    return filteredExportedPackages.isPresent();
  }

  @Override
  @NonNull
  protected Optional<JavaSootClassSource> getClassSource(@NonNull ClassType type) {

    Optional<JavaSootClassSource> cs =
        moduleInfoAnalysisInputLocations.stream()
            .map(location -> location.getClassSource(type, this))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(src -> (JavaSootClassSource) src)
            .findAny();

    if (cs.isPresent()) {
      return cs;
    }

    return super.getClassSource(type);
  }

  @NonNull
  public synchronized Optional<JavaSootClass> getClass(
      @NonNull ModulePackageName entryPackage, @NonNull JavaClassType type) {

    Optional<JavaModuleInfo> startOpt = getModuleInfo(entryPackage.getModuleSignature());
    if (startOpt.isEmpty()) {
      return Optional.empty();
    }

    JavaModuleInfo moduleInfo = startOpt.get();
    if (moduleInfo.isUnnamedModule()) {
      // unnamed module

      // find type in all exported packages of modules on module path first
      final List<JavaSootClassSource> foundClassSources =
          getAbstractClassSourcesForModules(entryPackage.getModuleSignature(), type)
              .filter(Optional::isPresent)
              .limit(1)
              .map(Optional::get)
              .collect(Collectors.toList());

      if (!foundClassSources.isEmpty()) {

        return Optional.of(buildClassFrom(foundClassSources.get(0)));
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
        final List<JavaSootClassSource> foundClassSources =
            getAbstractClassSourcesForModules(entryPackage.getModuleSignature(), type)
                .filter(Optional::isPresent)
                .limit(1)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (!foundClassSources.isEmpty()) {
          return Optional.of(buildClassFrom(foundClassSources.get(0)));
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

        final Optional<JavaSootClassSource> foundClassSources =
            getAbstractClassSourcesForModules(entryPackage.getModuleSignature(), type)
                .filter(Optional::isPresent)
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

        return foundClassSources.map(this::buildClassFrom);
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
      if (moduleInfoOpt.isEmpty()) {
        continue;
      }
      JavaModuleInfo moduleInfo = moduleInfoOpt.get();

      if (moduleInfo.isAutomaticModule() || moduleInfo.isUnnamedModule()) {
        // automatic module can read everything but its not "forwarding" require transitive!
        continue;
      }

      for (JavaModuleInfo.ModuleReference require : moduleInfo.requires()) {
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
  @NonNull
  public synchronized Collection<JavaSootClass> getModuleClasses(
      @NonNull ModuleSignature moduleSignature) {

    Optional<JavaModuleInfo> startOpt = getModuleInfo(moduleSignature);
    if (startOpt.isEmpty()) {
      return Collections.emptyList();
    }

    Stream<JavaSootClassSource> stream;
    JavaModuleInfo moduleInfo = startOpt.get();
    if (moduleInfo.isUnnamedModule()) {
      // unnamed module
      stream =
          inputLocations.stream()
              .flatMap(
                  input -> {
                    return input.getClassSources(this).map(src -> (JavaSootClassSource) src);
                  });

    } else {
      // named module
      if (moduleInfo.isAutomaticModule()) {
        // the automatic module
        stream =
            Stream.concat(
                    inputLocations.stream()
                        .flatMap(
                            input -> {
                              // classpath
                              return input
                                  .getClassSources(this)
                                  .filter(
                                      cs ->
                                          moduleSignature.equals(
                                              ((ModulePackageName)
                                                      cs.getClassType().getPackageName())
                                                  .getModuleSignature()));
                            }),
                    moduleInfoAnalysisInputLocations.stream()
                        .flatMap(
                            input -> {
                              // modulepath
                              Stream<? extends SootClassSource> modulesClassSources =
                                  input.getModulesClassSources(moduleSignature, this);
                              return modulesClassSources;
                            }))
                .map(src -> (JavaSootClassSource) src);

      } else {
        // explicit module
        stream =
            moduleInfoAnalysisInputLocations.stream()
                .flatMap(
                    input ->
                        input
                            .getModulesClassSources(moduleSignature, this)
                            .map(src -> (JavaSootClassSource) src));
      }
    }
    return stream.map(this::buildClassFrom).collect(Collectors.toList());
  }

  /*
      @NonNull
      public synchronized Collection<JavaSootClass> getTransitiveClasses(@NonNull  ModuleSignature moduleSignature) {

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

  @NonNull
  @Override
  public JavaModuleIdentifierFactory getIdentifierFactory() {
    return (JavaModuleIdentifierFactory) identifierFactory;
  }

  @NonNull
  private Stream<Optional<JavaSootClassSource>> getAbstractClassSourcesForModules(
      ModuleSignature moduleSig, @NonNull JavaClassType type) {

    // find the class in exported packages of modules
    return moduleInfoAnalysisInputLocations.stream()
        .map(location -> location.getClassSource(type, this))
        .filter(Optional::isPresent)
        .filter(
            cs -> {
              // check if the package is exported by or living in the same module
              return isPackageVisibleToModule(moduleSig, (ModulePackageName) type.getPackageName())
                  || isProvidedInterfaceImplementation(type);
            })
        .map(srcOpt -> srcOpt.map(src -> (JavaSootClassSource) src));
  }

  private boolean isProvidedInterfaceImplementation(@NonNull JavaClassType type) {
    ModulePackageName packageName = (ModulePackageName) type.getPackageName();
    JavaModuleInfo moduleInfo = getModuleInfo(packageName.getModuleSignature()).get();

    for (JavaModuleInfo.InterfaceReference provides : moduleInfo.provides()) {
      JavaClassType interfaceType = provides.getInterfaceType();
      String packageName1 = interfaceType.getPackageName().getName();
      String packageName2 = type.getPackageName().getName();
      if (packageName1.equals(packageName2)) {
        if (interfaceType.getClassName().equals(type.getClassName())) {
          return true;
        }
      }
    }

    return false;
  }

  @NonNull
  public Set<ModuleSignature> getNamedModules() {
    Set<ModuleSignature> modules = new HashSet<>();
    for (ModuleInfoAnalysisInputLocation moduleInputLocation : moduleInfoAnalysisInputLocations) {
      modules.addAll(moduleInputLocation.getModules(this));
    }
    return modules;
  }
}
