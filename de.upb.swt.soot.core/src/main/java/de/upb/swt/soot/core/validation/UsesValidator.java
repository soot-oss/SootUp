package de.upb.swt.soot.core.validation;

import de.upb.swt.soot.core.model.Body;
import java.util.List;

public class UsesValidator implements BodyValidator {

  /** Verifies that each use in this Body has a def. */
  @Override
  public void validate(Body body, List<ValidationException> exception) {
    // TODO: auto generated stub
    /*
     * // Conservative validation of uses: add edges to exception handlers // even if they are not reachable. // // class C {
     * // int a; // public void m() { // try { // a = 2; // } catch (Exception e) { // System.out.println("a: "+ a); // } //
     * } // } // // In a graph generated from the Jimple representation there would // be no edge from "a = 2;" to "catch..."
     * because "a = 2" can only // generate an Error, a subclass of Throwable and not of Exception. // Use of 'a' in
     * "System.out.println" would thus trigger a 'no defs // for value' RuntimeException. // To avoid this we create an
     * ExceptionalUnitGraph that considers all // exception handlers (even unreachable ones as the one in the code // snippet
     * above) by using a PedanticThrowAnalysis and setting the // parameter 'omitExceptingUnitEdges' to false. // // Note
     * that unreachable traps can be removed by setting jb.uce's // "remove-unreachable-traps" option to true.
     *
     * ThrowAnalysis throwAnalysis = PedanticThrowAnalysis.v(); UnitGraph g = new ExceptionalUnitGraph(body, throwAnalysis,
     * false); LocalDefs ld = LocalDefs.Factory.newLocalDefs(g, true);
     *
     * Collection<Local> locals = body.getLocals(); for (Unit u : body.getUnits()) { for (ValueBox box : u.getUseBoxes()) {
     * Value v = box.getValue(); if (v instanceof Local) { Local l = (Local) v;
     *
     * if (!locals.contains(l)) { String msg = "Local " + v + " is referenced here but not in body's local-chain. (" +
     * body.getMethod() + ")"; exception.add(new ValidationException(u, msg, msg)); }
     *
     * if (ld.getDefsOfAt(l, u).isEmpty()) { // abroken graph is also a possible reason for undefined locals! assert
     * graphEdgesAreValid(g, u) : "broken graph found: " + u;
     *
     * exception.add(new ValidationException(u, "There is no path from a definition of " + v + " to this statement.", "(" +
     * body.getMethod() + ") no defs for value: " + l + "!")); } } } }
     *
     * }
     *
     * private boolean graphEdgesAreValid(UnitGraph g, Unit u) { /* for (Unit p : g.getPredsOf(u)) { if
     * (!g.getSuccsOf(p).contains(u)) { return false; } } return true;
     */
  }

  @Override
  public boolean isBasicValidator() {
    return false;
  }
}
