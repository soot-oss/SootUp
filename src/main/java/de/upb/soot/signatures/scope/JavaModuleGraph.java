package de.upb.soot.signatures.scope;

import de.upb.soot.signatures.ModuleSignature;

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
  private final Set<JavaModule> modules;
  private final Map<ModuleSignature, JavaModule> nameToModule;
  private ModuleFinder moduleFinder;

  private JavaModuleGraph() {
    this.graph = Collections.emptyMap();
    this.modules = Collections.emptySet();
    this.nameToModule = Collections.emptyMap();
  }

  public JavaModuleGraph(ModuleFinder moduleFinder) {

    this.moduleFinder = moduleFinder;
    graph = new HashMap<>();
    modules = new HashSet<>();
    nameToModule = new HashMap<>();
  }

  public Optional<JavaModule> findModule(@Nonnull ModuleSignature name) {
    JavaModule m = nameToModule.get(name);
    if (m != null) return Optional.of(m);
    // FIXME: ...

    return Optional.empty();
  }

  public Collection<JavaModule> getAllModules() {
    return null;
  }
}
