package de.upb.soot.views;

import de.upb.soot.IdentifierFactory;
import de.upb.soot.Options;
import de.upb.soot.Project;
import de.upb.soot.Scope;
import de.upb.soot.callgraph.ICallGraph;
import de.upb.soot.callgraph.ICallGraphAlgorithm;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SourceType;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.namespaces.JavaModulePathNamespace;
import de.upb.soot.typehierarchy.ITypeHierarchy;
import de.upb.soot.types.GlobalTypeScope;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.JavaClassTypeScope;
import de.upb.soot.types.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class JavaModuleView implements IView {

  /*
   * holds the references to SootClass 1: Type the class signature 2: Map<String, AbstractClass>: The String represents the module
   * that holds the corresponding RefType since multiple modules may contain the same class this is a map (for fast look ups)
   * TODO: evaluate if Guava's multimap is faster
   */
  @Nonnull private final Map<JavaClassTypeScope, Map<Type, AbstractClass>> map = new HashMap<>();

  private final Project<JavaModulePathNamespace> project;

  private volatile boolean _isFullyResolved;

  public JavaModuleView(Project<JavaModulePathNamespace> project) {
    this.project = project;
    this.map.put(GlobalTypeScope.getInstance(), new HashMap<>());
  }

  boolean isFullyResolved() {
    return this._isFullyResolved;
  }

  public Project<JavaModulePathNamespace> getProject() {
    return project;
  }

  private AbstractClass addClassToMap(
      JavaClassTypeScope scope, Type type, AbstractClass abstractClass) {
    // FIXME: everything is in the global scope...
    map.get(GlobalTypeScope.getInstance()).put(type, abstractClass);
    return map.getOrDefault(scope, new HashMap<>()).put(type, abstractClass);
  }

  @Nonnull
  @Override
  public Collection<AbstractClass> getClasses() {
    List<AbstractClass> initialized = new ArrayList<>();

    for (JavaClassTypeScope scope : map.keySet()) {
      initialized.addAll(map.get(scope).values());
    }
    return initialized;
  }

  @Nonnull
  @Override
  public Stream<AbstractClass> classes() {
    return null;
  }

  @Nonnull
  @Override
  public Optional<AbstractClass> getClass(@Nonnull JavaClassType signature) {
    return getClass(signature, signature.getScope());
  }

  public Optional<AbstractClass> getClass(
      @Nonnull JavaClassType signature, @Nonnull JavaClassTypeScope classTypeScope) {

    AbstractClass sootClass = map.get(classTypeScope).get(signature);

    if (sootClass != null) return Optional.of(sootClass);
    else if (this.isFullyResolved()) return Optional.empty();
    else return this.__resolveSootClass(signature, classTypeScope);
  }

  @Nullable
  private Optional<AbstractClass> __resolveSootClass(
      @Nonnull JavaClassType signature, JavaClassTypeScope classTypeScope) {
    Optional<AbstractClass> abstractClass =
        this.getProject()
            .getNamespace()
            .getClassSource(signature)
            .map(
                it -> {
                  // TODO Don't use a fixed SourceType here.
                  if (it instanceof ClassSource) {
                    // FIXME: use wrapper??
                    return new SootClass((ClassSource) it, SourceType.Application);
                  }
                  return null;
                });
    abstractClass.ifPresent(it -> addClassToMap(classTypeScope, it.getType(), it));
    return abstractClass;
  }

  @Nonnull
  @Override
  public ICallGraph createCallGraph() {
    return null;
  }

  @Nonnull
  @Override
  public ICallGraph createCallGraph(ICallGraphAlgorithm algorithm) {
    return null;
  }

  @Nonnull
  @Override
  public ITypeHierarchy createTypeHierarchy() {
    return null;
  }

  @Nonnull
  @Override
  public Optional<Scope> getScope() {
    return Optional.empty();
  }

  @Nonnull
  @Override
  public IdentifierFactory getIdentifierFactory() {
    return null;
  }

  @Nonnull
  @Override
  public Options getOptions() {
    return null;
  }

  @Override
  public boolean doneResolving() {
    return false;
  }

  @Nonnull
  @Override
  public String quotedNameOf(@Nonnull String name) {
    return null;
  }
}
