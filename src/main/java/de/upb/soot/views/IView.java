package de.upb.soot.views;

import de.upb.soot.Options;
import de.upb.soot.Scope;
import de.upb.soot.callgraph.ICallGraph;
import de.upb.soot.callgraph.ICallGraphAlgorithm;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.typehierarchy.ITypeHierarchy;
import de.upb.soot.types.Type;
import de.upb.soot.types.TypeFactory;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

/**
 * A view on code.
 *
 * @author Linghui Luo
 * @author Ben Hermann
 */
public interface IView {

  /** Return all classes in the view. */
  @Nonnull
  Collection<AbstractClass> getClasses();

  /**
   * Returns a stream of classes in the view.
   *
   * @return A stream of classes
   */
  @Nonnull
  Stream<AbstractClass> classes();

  /**
   * Return a class with given signature.
   *
   * @return A class with given signature.
   */
  @Nonnull
  Optional<AbstractClass> getClass(@Nonnull Type signature);

  /**
   * Provides the call graph using the default algorithm.
   *
   * @return A call graph valid in the view
   */
  @Nonnull
  ICallGraph createCallGraph();

  /**
   * Provides the call graph using a provided algorithm.
   *
   * @param algorithm A call graph algorithm
   * @return A call graph valid in the view
   */
  @Nonnull
  ICallGraph createCallGraph(ICallGraphAlgorithm algorithm);

  /**
   * Provides a type hierarchy.
   *
   * @return A type hierarchy valid in the view
   */
  @Nonnull
  ITypeHierarchy createTypeHierarchy();

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

  /** Returns the {@link SignatureFactory} for this view. */
  @Nonnull
  SignatureFactory getSignatureFactory();

  /**
   * Returns the {@link TypeFactory} for this view.
   */
  @Nonnull
  TypeFactory getTypeFactory();

  /** Return the {@link Options} of this view. */
  @Nonnull
  Options getOptions();

  // FIXME: [JMP] Adding classes violates the immutability rule!
  /** Add given class to the view. */
  void addClass(@Nonnull AbstractClass klass);

  boolean doneResolving();

  @Nonnull
  String quotedNameOf(@Nonnull String name);

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
  //      Optional<? extends IMethod> m = klass.getMethod(bsm);
  //      return m.map(c -> (SootMethod) c);
  //    }
  //    return Optional.empty();
  //  }
}
