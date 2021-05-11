package de.upb.swt.soot.callgraph.spark;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2002-2021 Ondrej Lhotak, Kadiray Karakaya and others
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

import com.google.common.collect.Sets;
import de.upb.swt.soot.callgraph.algorithm.CallGraphAlgorithm;
import de.upb.swt.soot.callgraph.model.CallGraph;
import de.upb.swt.soot.callgraph.spark.builder.PropagatorEnum;
import de.upb.swt.soot.callgraph.spark.builder.SparkOptions;
import de.upb.swt.soot.callgraph.spark.pag.PointerAssignmentGraph;
import de.upb.swt.soot.callgraph.spark.pag.nodes.AllocationNode;
import de.upb.swt.soot.callgraph.spark.pag.nodes.Node;
import de.upb.swt.soot.callgraph.spark.pag.nodes.VariableNode;
import de.upb.swt.soot.callgraph.spark.pointsto.PointsToAnalysis;
import de.upb.swt.soot.callgraph.spark.solver.*;
import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.views.View;
import java.util.HashSet;
import java.util.Set;

public class Spark implements PointsToAnalysis {

  private View<? extends SootClass> view;
  private CallGraph callGraph;
  private SparkOptions options;

  private PointerAssignmentGraph pag;

  private PointsToAnalysis analysis;

  private Spark(View<? extends SootClass> view, CallGraph callGraph, SparkOptions options) {
    this.view = view;
    this.callGraph = callGraph;
    this.options = options;
  }

  public void analyze() {
    // Build PAG
    buildPointerAssignmentGraph();

    // Simplify
    collapsePointerAssigmentGraph();

    // Propagate
    propagatePointerAssignmentGraph();

    // TODO: VTA cg
    refineCallGraph();
  }

  private void refineCallGraph() {
    if (options.isVta()) {
      CallGraphAlgorithm cga =
          new VariableTypeAnalysisWithSpark(view, new ViewTypeHierarchy(view), callGraph, pag);
      CallGraph refinedCallGraph = cga.initialize(callGraph.getEntryPoints());
      this.callGraph = refinedCallGraph;
    } else if (options.isRta()) {
      CallGraphAlgorithm cga =
          new RapidTypeAnalysisWithSpark(view, new ViewTypeHierarchy(view), callGraph, pag);
      CallGraph refinedCallGraph = cga.initialize(callGraph.getEntryPoints());
      this.callGraph = refinedCallGraph;
    }
  }

  private void buildPointerAssignmentGraph() {
    pag = new PointerAssignmentGraph(view, callGraph, options);
  }

  private void collapsePointerAssigmentGraph() {
    if ((options.isSimplifySCCS() && !options.isOnFlyCG()) || options.isVta()) {
      new SCCCollapser(pag, options.isIgnoreTypesForSCCS()).collapse();
    }
    if (options.isSimplifyOffline() && !options.isOnFlyCG()) {
      new EBBCollapser(pag).collapse();
    }
    // old soot had if (true || opts.simplify_sccs() || opts.vta() || opts.simplify_offline())
    // pag.cleanUpMerges();
  }

  private void propagatePointerAssignmentGraph() {
    Propagator propagator = null;
    switch (options.getPropagator()) {
      case ITER:
        propagator = new IterationPropagator(pag);
        break;
      case WORKLIST:
        propagator = new WorklistPropagator(pag);
        break;
      case CYCLE:
        propagator = new CyclePropagator(pag);
        break;
      case MERGE:
        propagator = new MergePropagator(pag);
        break;
      case ALIAS:
        propagator = new AliasPropagator(pag);
      case NONE:
        break;
      default:
        throw new RuntimeException("Propagator not defined");
    }

    if (propagator != null) {
      propagator.propagate();
    }
  }

  @Override
  public Set<Node> getPointsToSet(Local local) {
    VariableNode node = pag.getLocalVariableNode(local);
    if (node == null) {
      return Sets.newHashSet();
    }
    return node.getPointsToSet();
  }

  public Set<Node> getPointsToSet(Object value) {
    VariableNode node = pag.getLocalVariableNode(value);
    if (node == null) {
      return Sets.newHashSet();
    }
    return node.getPointsToSet();
  }

  public Set<Node> getPointsToSet(Local local, SootField field) {
    Set<Node> pointsToSetOfLocal = getPointsToSet(local);
    return getPointsToSet(pointsToSetOfLocal, field);
  }

  /**
   * Returns the set of objects pointed to by instance field f of the objects in the PointsToSet s.
   */
  public Set<Node> getPointsToSet(Set<Node> set, final SootField field) {
    if (field.isStatic()) {
      throw new RuntimeException("The parameter f must be an *instance* field.");
    }

    if (options.isFieldBased() || options.isVta()) {
      VariableNode node = pag.getGlobalVariableNode(field);
      if (node == null) {
        return Sets.newHashSet();
      }
      return node.getPointsToSet();
    }
    // TODO: propagator alias
    if (options.getPropagator() == PropagatorEnum.ALIAS) {
      throw new RuntimeException(
          "The alias edge propagator does not compute points-to information for instance fields!"
              + "Use a different propagator.");
    }
    final Set<Node> result = new HashSet<>();
    for (Node node : set) {
      Node allocDotField = ((AllocationNode) node).dot(field);
      if (allocDotField != null) {
        result.addAll(allocDotField.getPointsToSet());
      }
    }
    return result;
  }

  public CallGraph getCallGraph() {
    return callGraph;
  }

  public static class Builder {

    private SparkOptions options;

    private View<? extends SootClass> view;
    private CallGraph callGraph;

    // VTA: Setting VTA to true has the effect of setting:
    // - field-based,
    // - types-for-sites,
    // - simplify-sccs to true,
    // - on-fly-cg to false, to simulate Variable Type Analysis,

    // RTA: Setting RTA to true sets
    // - types-for-sites to true,
    // - causes Spark to use a single points-to set for all variables

    public Builder(View<? extends SootClass> view, CallGraph callGraph) {
      this.view = view;
      this.callGraph = callGraph;
      options = new SparkOptions();
    }

    public Builder ignoreTypes(boolean ignoreTypes) {
      options.setIgnoreTypes(ignoreTypes);
      return this;
    }

    public Builder vta(boolean vta) {
      options.setVta(vta);
      if (vta) {
        options.setTypesForSites(true);
        options.setFieldBased(true);
        options.setSimplifySCCS(true);
        options.setOnFlyCG(false);
      }
      return this;
    }

    public Builder rta(boolean rta) {
      options.setRta(rta);
      return this;
    }

    public Builder fieldBased(boolean fieldBased) {
      options.setFieldBased(fieldBased);
      return this;
    }

    public Builder typesForSites(boolean typesForSites) {
      options.setTypesForSites(typesForSites);
      return this;
    }

    public Builder mergeStringBuffer(boolean mergeStringBuffer) {
      options.setMergeStringBuffer(mergeStringBuffer);
      return this;
    }

    public Builder stringConstants(boolean stringConstants) {
      options.setStringConstants(stringConstants);
      return this;
    }

    public Builder simulateNatives(boolean simulateNatives) {
      options.setSimulateNatives(simulateNatives);
      return this;
    }

    public Builder emptiesAsAllocs(boolean emptiesAsAllocs) {
      options.setEmptiesAsAllocs(emptiesAsAllocs);
      return this;
    }

    public Builder simpleEdgesBidirectional(boolean simpleEdgesBidirectional) {
      options.setSimpleEdgesBidirectional(simpleEdgesBidirectional);
      return this;
    }

    public Builder onFlyCFG(boolean onFlyCFG) {
      options.setOnFlyCG(onFlyCFG);
      return this;
    }

    public Builder simplifyOffline(boolean simplifyOffline) {
      options.setSimplifyOffline(simplifyOffline);
      return this;
    }

    public Builder simplifySCCS(boolean simplifySCCS) {
      options.setSimplifySCCS(simplifySCCS);
      return this;
    }

    public Builder ignoreTypesForSCCS(boolean ignoreTypesForSCCS) {
      options.setIgnoreTypesForSCCS(ignoreTypesForSCCS);
      return this;
    }

    public Builder propagator(PropagatorEnum propagator) {
      options.setPropagator(propagator);
      return this;
    }

    public Spark build() {
      options.validate();
      return new Spark(view, callGraph, options);
    }
  }
}
