package de.upb.soot.signatures.scope;

import de.upb.soot.signatures.ModuleSignature;
import de.upb.soot.types.JavaClassType;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class JavaModuleGraph {

  private final Map<JavaModule, Set<JavaModule>> graph;
  private final Map<ModuleSignature, JavaModule> nameToModule;
  private ModuleFinder moduleFinder;

  private JavaModuleGraph() {
    this.graph = Collections.emptyMap();
    this.nameToModule = Collections.emptyMap();
  }

  public JavaModuleGraph(ModuleFinder moduleFinder) {

    this.moduleFinder = moduleFinder;
    graph = new HashMap<>();
    nameToModule = new HashMap<>();
  }

  public Optional<JavaModule> findModule(@Nonnull ModuleSignature name) {
    JavaModule m = nameToModule.get(name);
    if (m != null) return Optional.of(m);
    JavaModule javaModule = moduleFinder.discoverModule(name.getModuleName(), this);
    nameToModule.put(name, javaModule);
    graph.put(javaModule, new HashSet<>());

    return Optional.ofNullable(javaModule);
  }

  public Collection<JavaModule> getAllModules() {
    return nameToModule.values();
  }

  public Set<JavaModule> getRequiredModules(ModuleSignature moduleSignature) {
    Optional<JavaModule> foundModule = this.findModule(moduleSignature);

    return foundModule.map(x -> getRequiredModules(x)).orElse(Collections.emptySet());
  }

  public Set<JavaModule> getRequiredModules(JavaModule javaModule) {

    Set<JavaModule> requiredModules = graph.get(javaModule);

    if (requiredModules.isEmpty() || (requiredModules.size() != javaModule.getRequires().size())) {
      // we have not yet resolved all required modules...
      // thus, let us do it now...

      for (ModuleSignature requiredSignature : javaModule.getRequires()) {
        Optional<JavaModule> requiredModule = this.findModule(requiredSignature);
        requiredModule.ifPresent(x -> requiredModules.add(x));
      }

      graph.put(javaModule, requiredModules);
    }

    return requiredModules;
  }

  public Set<JavaModule> getRequiredTransitive(JavaModule javaModule) {
    // get/init all required Modules
    getRequiredModules(javaModule);
    HashSet<JavaModule> transitiveRequiredModules = new HashSet<>();

    for (ModuleSignature requiredSignature : javaModule.getTransitiveRequires()) {
      Optional<JavaModule> requiredModule = this.findModule(requiredSignature);
      requiredModule.ifPresent(x -> transitiveRequiredModules.add(x));
    }

    return transitiveRequiredModules;
  }

  /**
   * Finds the module that exports the given class to the given module
   *
   * @param className the requested class
   * @param toModuleName the module from which the request is made
   * @return the module's name that exports the class to the given module
   */
  public final JavaModule findModuleThatExports(
      JavaClassType className, ModuleSignature toModuleName) {
    JavaModule toModule =
        findModule(toModuleName)
            .orElseThrow(
                () -> new RuntimeException("Cannot find a module with name " + toModuleName));

    if (className == ModuleSignature.MODULE_INFO_CLASS) {
      return toModule;
    }

    String packageName = className.getPackageName().getPackageName();

    // check if it is my own package
    if (toModule.getAllModulePackages().contains(packageName)) {
      return toModule;
    }

    // shortcut, an automatic module is allowed to access any other class

    if (toModule.isAutomaticModule()) {
      // get the module that exports this package
      for (JavaModule module : this.nameToModule.values()) {
        if (module.getPublicExportedPackages().contains(packageName)) {
          return module;
          // in the worst case the module has not been resolved yet...
          // FIXME: then we will never find it...
        }
      }
    }

    // the class is not contained in the module itself
    // thus, we have to go through the module graph....
    Set<JavaModule> requiredModules = getRequiredModules(toModuleName);

    for (JavaModule requModule : requiredModules) {
      // check if the required module exports the package to the requesting module javaModule
      if (requModule.exportsPackage(packageName, toModule, this)) {
        return requModule;
      }
      // check if the requested package is contained in a module that is declared "requires
      // transitive"
      for (JavaModule requiredTransitiveModule : this.getRequiredTransitive(requModule)) {
        return findModuleThatExports(className, requiredTransitiveModule.getModuleSignature());
      }
    }

    // should not be called...

    // if the class is not exported by any package, it has to internal to this module
    return null;
  }
}
