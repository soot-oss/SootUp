package de.upb.soot.views;

import de.upb.soot.Options;
import de.upb.soot.Scope;
import de.upb.soot.callgraph.ICallGraph;
import de.upb.soot.callgraph.ICallGraphAlgorithm;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.signatures.ISignature;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.signatures.TypeSignature;
import de.upb.soot.typehierarchy.ITypeHierarchy;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A view on code.
 *
 * @author Linghui Luo
 * @author Ben Hermann
 *
 */
public interface IView {

  /**
   * Return all classes in the view.
   * 
   * @return
   */
  Collection<AbstractClass> getClasses();

  /**
   * Returns a stream of classes in the view.
   * 
   * @return A stream of classes
   */
  Stream<AbstractClass> classes();

  /**
   * Return a class with given signature.
   * 
   * @return A class with given signature.
   */
  Optional<AbstractClass> getClass(ISignature signature);

  /**
   * Provides the call graph using the default algorithm.
   * 
   * @return A call graph valid in the view
   */
  ICallGraph createCallGraph();

  /**
   * Provides the call graph using a provided algorithm.
   * 
   * @param algorithm
   *          A call graph algorithm
   * @return A call graph valid in the view
   */
  ICallGraph createCallGraph(ICallGraphAlgorithm algorithm);

  /**
   * Provides a type hierarchy.
   * 
   * @return A type hierarchy valid in the view
   */
  ITypeHierarchy createTypeHierarchy();

  /**
   * Returns the scope if the view is scoped.
   * 
   * @return The scope that led to the view
   */
  Optional<Scope> getScope();

  /**
   * Returns the {@link RefType} with given class Signature from the view. If there is no RefType with given className
   * exists, create a new instance.
   * 
   * @param className
   * @return
   */
  RefType getRefType(TypeSignature classSignature);

  /**
   * Return the {@link Type} wtih given signature from the view. If there is no Type with given signature exists, create a
   * new instance.
   * 
   * @param signature
   * @return
   */
  Type getType(TypeSignature signature);

  /**
   * Returns the {@link SignatureFactory} for this view.
   * 
   * @return
   */
  SignatureFactory getSignatureFacotry();

  /**
   * Return the {@link Options} of this view.
   * 
   * @return
   */
  Options getOptions();

  /**
   * Add given class to the view.
   * 
   * @param klass
   */
  void addClass(AbstractClass klass);

  boolean doneResolving();

  String quotedNameOf(String name);
}
