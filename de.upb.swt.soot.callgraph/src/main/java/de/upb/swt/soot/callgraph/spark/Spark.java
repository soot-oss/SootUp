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
import de.upb.swt.soot.callgraph.model.CallGraph;
import de.upb.swt.soot.callgraph.spark.builder.SparkOptions;
import de.upb.swt.soot.callgraph.spark.pag.PointerAssignmentGraph;
import de.upb.swt.soot.callgraph.spark.pag.nodes.AllocationNode;
import de.upb.swt.soot.callgraph.spark.pag.nodes.Node;
import de.upb.swt.soot.callgraph.spark.pag.nodes.VariableNode;
import de.upb.swt.soot.callgraph.spark.pointsto.PointsToAnalysis;
import de.upb.swt.soot.callgraph.spark.solver.Propagator;
import de.upb.swt.soot.callgraph.spark.solver.WorklistPropagator;
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

    // Propagate
    Propagator propagator = new WorklistPropagator(pag);
    propagator.propagate();
  }

  private void buildPointerAssignmentGraph() {
    pag = new PointerAssignmentGraph(view, callGraph, options);
  }

  private void simplifyPointerAssignmentGraph() {}

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

  private Set<Node> getPointsToSet(Set<Node> set, final SootField field) {
    if (field.isStatic()) {
      throw new RuntimeException("The parameter f must be an *instance* field.");
    }

    if(options.isFieldBased() || options.isVta()){
      VariableNode node = pag.getGlobalVariableNode(field);
      if(node == null){
        return Sets.newHashSet();
      }
      return node.getPointsToSet();
    }
    // TODO: propagator alias
    final Set<Node> result = new HashSet<>();
    for (Node node : set) {
      Node allocDotField = ((AllocationNode) node).dot(field);
      if (allocDotField != null) {
        result.addAll(allocDotField.getPointsToSet());
      }
    }
    return result;
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

    public void ignoreTypes(boolean ignoreTypes) {
      options.setIgnoreTypes(ignoreTypes);
    }


    public void vta(boolean vta) {
      options.setVta(vta);
    }


    public void rta(boolean rta) {
      options.setRta(rta);
    }


    public void fieldBased(boolean fieldBased) {
      options.setFieldBased(fieldBased);
    }

    public void typesForSites(boolean typesForSites) {
      options.setTypesForSites(typesForSites);
    }

    public void mergeStringBuffer(boolean mergeStringBuffer) {
      options.setMergeStringBuffer(mergeStringBuffer);
    }

    public void stringConstants(boolean stringConstants) {
      options.setStringConstants(stringConstants);
    }

    public void simulateNatives(boolean simulateNatives) {
      options.setSimulateNatives(simulateNatives);
    }

    public void emptiesAsAllocs(boolean emptiesAsAllocs) {
      options.setEmptiesAsAllocs(emptiesAsAllocs);
    }

    public void simpleEdgesBidirectional(boolean simpleEdgesBidirectional) {
      options.setSimpleEdgesBidirectional(simpleEdgesBidirectional);
    }

    public void onFlyCFG(boolean onFlyCFG) {
      options.setOnFlyCFG(onFlyCFG);
    }

    public void simplifyOffline(boolean simplifyOffline) {
      options.setSimplifyOffline(simplifyOffline);
    }

    public void simplifySCCS(boolean simplifySCCS) {
      options.setSimplifySCCS(simplifySCCS);
    }

    public void ignoreTypesForSCCS(boolean ignoreTypesForSCCS) {
      options.setIgnoreTypesForSCCS(ignoreTypesForSCCS);
    }

    public Spark build(){
      return new Spark(view, callGraph, options);
    }

  }

}
