package de.upb.soot.views;

import de.upb.soot.Scope;
import de.upb.soot.callgraph.ICallGraph;
import de.upb.soot.callgraph.ICallGraphAlgorithm;
import de.upb.soot.core.SootClass;
import de.upb.soot.typehierarchy.ITypeHierarchy;

import java.util.List;
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
     * Returns all classes in the view.
     * @return A list of classes
     */
    List<SootClass> getClasses();

    /**
     * Returns a stream of classes in the view.
     * @return A stream of classes
     */
    Stream<SootClass> classes();

    /**
     * Provides the call graph using the default algorithm.
     * @return A call graph valid in the view
     */
    ICallGraph createCallGraph();

    /**
     * Provides the call graph using a provided algorithm.
     * @param algorithm A call graph algorithm
     * @return A call graph valid in the view
     */
    ICallGraph createCallGraph(ICallGraphAlgorithm algorithm);

    /**
     * Provides a type hierarchy.
     * @return A type hierarchy valid in the view
     */
    ITypeHierarchy createTypeHierarchy();

    /**
     * Returns the scope if the view is scoped.
     * @return The scope that led to the view
     */
    Optional<Scope> getScope();
}
