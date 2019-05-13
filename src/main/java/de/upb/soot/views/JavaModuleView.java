package de.upb.soot.views;

import de.upb.soot.IdentifierFactory;
import de.upb.soot.Options;
import de.upb.soot.Project;
import de.upb.soot.Scope;
import de.upb.soot.callgraph.ICallGraph;
import de.upb.soot.callgraph.ICallGraphAlgorithm;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.namespaces.JavaModulePathNamespace;
import de.upb.soot.typehierarchy.ITypeHierarchy;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.JavaClassTypeScope;
import de.upb.soot.types.Type;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
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

  public JavaModuleView(Project<JavaModulePathNamespace> project) {}

  @Nonnull
  @Override
  public Collection<AbstractClass> getClasses() {
    return null;
  }

  @Nonnull
  @Override
  public Stream<AbstractClass> classes() {
    return null;
  }

  @Nonnull
  @Override
  public Optional<AbstractClass> getClass(@Nonnull JavaClassType signature) {
    // FIXME: maybe special treatment for global scope??

    return Optional.of(map.get(signature.getScope()).get(signature));
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
