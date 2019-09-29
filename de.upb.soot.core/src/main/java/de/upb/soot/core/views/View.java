package de.upb.soot.core.views;

import de.upb.soot.core.IdentifierFactory;
import de.upb.soot.core.Options;
import de.upb.soot.core.Scope;
import de.upb.soot.core.frontend.AbstractClassSource;
import de.upb.soot.callgraph.CallGraph;
import de.upb.soot.callgraph.CallGraphAlgorithm;
import de.upb.soot.core.model.AbstractClass;
import de.upb.soot.core.types.JavaClassType;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A view on code.
 *
 * @author Linghui Luo
 * @author Ben Hermann
 */
public interface View {

  /** Return all classes in the view. */
  @Nonnull
  Collection<AbstractClass<? extends AbstractClassSource>> getClasses();

  /** Return all classes in the view. */
  @Nonnull
  default Stream<AbstractClass<? extends AbstractClassSource>> getClassesStream() {
    return getClasses().stream();
  }

  /**
   * Return a class with given signature.
   *
   * @return A class with given signature.
   */
  @Nonnull
  Optional<AbstractClass<? extends AbstractClassSource>> getClass(@Nonnull JavaClassType signature);

  /**
   * Provides the call graph using the default algorithm.
   *
   * @return A call graph valid in the view
   */
  @Nonnull
  CallGraph createCallGraph();

  /**
   * Provides the call graph using a provided algorithm.
   *
   * @param algorithm A call graph algorithm
   * @return A call graph valid in the view
   */
  @Nonnull
  CallGraph createCallGraph(CallGraphAlgorithm algorithm);

  /**
   * Returns the scope if the view is scoped.
   *
   * @return The scope that led to the view
   */
  @Nonnull
  Optional<Scope> getScope();

  //  /**
  //   * Returns the {@link JavaClassType} with given class Signature from the view. If there
  // is no RefType with given className
  //   * exists, create a new instance.
  //   */
  //  @Nonnull
  //  JavaClassType getRefType(@Nonnull Type classSignature);

  /** Returns the {@link IdentifierFactory} for this view. */
  @Nonnull
  IdentifierFactory getIdentifierFactory();

  /** Return the {@link Options} of this view. */
  @Nonnull
  Options getOptions();

  boolean doneResolving();

  @Nonnull
  String quotedNameOf(@Nonnull String name);

  <T> void putModuleData(@Nonnull ModuleDataKey<T> key, @Nonnull T value);

  @Nullable
  <T> T getModuleData(@Nonnull ModuleDataKey<T> key);

  default <T> T getOrComputeModuleData(@Nonnull ModuleDataKey<T> key, Supplier<T> dataSupplier) {
    T moduleData = getModuleData(key);
    if (moduleData != null) {
      return moduleData;
    }

    T computedModuleData = dataSupplier.get();
    putModuleData(key, computedModuleData);
    return computedModuleData;
  }

  //  // TODO: [JMP] Move type resolving into view.
  //  /**
  //   * Returns a backed list of the exceptions thrown by this methodRef.
  //   */
  //  public @Nonnull Collection<SootClass> getExceptions() {
  //    return this.exceptions.stream()
  //             .map(e -> this.getView().getClass(e))
  //             .filter(Optional::isPresent).map(Optional::get)
  //             .map(it -> (SootClass) it).collect(Collectors.toSet());
  //  }

  //  // TODO: This was placed in `JDynamicInvokeExpr`
  //  public Optional<SootMethod> getBootstrapMethod() {
  //    JavaClassType signature = bsm.declClassSignature;
  //    Optional<AbstractClass> op = this.getView().getClass(signature);
  //    if (op.isPresent()) {
  //      AbstractClass klass = op.get();
  //      Optional<? extends Method> m = klass.getMethod(bsm);
  //      return m.map(c -> (SootMethod) c);
  //    }
  //    return Optional.empty();
  //  }

  @SuppressWarnings("unused") // Used in modules
  interface ModuleDataKey<T> {}
}
