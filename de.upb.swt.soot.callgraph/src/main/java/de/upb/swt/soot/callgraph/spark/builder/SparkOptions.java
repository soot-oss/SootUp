package de.upb.swt.soot.callgraph.spark.builder;

import de.upb.swt.soot.callgraph.spark.pag.PointerAssignmentGraph;
import de.upb.swt.soot.callgraph.spark.solver.Propagator;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2003-2021 Ondrej Lhotak, Kadiray Karakaya and others
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
public class SparkOptions {

  private boolean ignoreTypes = false;
  private boolean vta = false;
  private boolean rta = false;
  private boolean fieldBased = false;
  private boolean typesForSites = false;
  private boolean mergeStringBuffer = true;
  private boolean stringConstants = false;
  private boolean simulateNatives = true; // TODO: ContextInsensitiveBuilder MethodPAG
  private boolean emptiesAsAllocs = false;
  private boolean simpleEdgesBidirectional = false;
  private boolean onFlyCG = false; // TODO: SparkTransformer ContextInsBuilder PAG
  private boolean simplifyOffline = false;
  private boolean simplifySCCS = false;
  private boolean ignoreTypesForSCCS = false;

  PropagatorEnum propagator = PropagatorEnum.WORKLIST;

  public boolean isIgnoreTypes() {
    return ignoreTypes;
  }

  /**
   * Ignore Types Entirely -- Make Spark completely ignore declared types of variables.
   *
   * <p>When this option is set to true, all parts of Spark completely ignore declared types of
   * variables and casts.
   */
  public void setIgnoreTypes(boolean ignoreTypes) {
    this.ignoreTypes = ignoreTypes;
  }

  public boolean isVta() {
    return vta;
  }

  /**
   * VTA -- Emulate Variable Type Analysis.
   *
   * <p>Setting VTA to true has the effect of setting field-based, types-for-sites, and
   * simplify-sccs to true, and on-fly-cg to false, to simulate Variable Type Analysis, described in
   * our OOPSLA 2000 paper. Note that the algorithm differs from the original VTA in that it handles
   * array elements more precisely.
   */
  public void setVta(boolean vta) {
    this.vta = vta;
    if (vta) {
      typesForSites = true;
      fieldBased = true;
      simplifySCCS = true;
      onFlyCG = false;
    }
  }

  public boolean isRta() {
    return rta;
  }

  /**
   * RTA -- Emulate Rapid Type Analysis.
   *
   * <p>Setting RTA to true sets types-for-sites to true, and causes Spark to use a single points-to
   * set for all variables, giving Rapid Type Analysis.
   */
  public void setRta(boolean rta) {
    this.rta = rta;
  }

  public boolean isFieldBased() {
    return fieldBased;
  }

  /**
   * Field Based -- Use a field-based rather than field-sensitive representation.
   *
   * <p>When this option is set to true, fields are represented by variable (Green) nodes, and the
   * object that the field belongs to is ignored (all objects are lumped together), giving a
   * field-based analysis. Otherwise, fields are represented by field reference (Red) nodes, and the
   * objects that they belong to are distinguished, giving a field-sensitive analysis.
   */
  public void setFieldBased(boolean fieldBased) {
    this.fieldBased = fieldBased;
  }

  public boolean isTypesForSites() {
    return typesForSites;
  }

  /**
   * Types For Sites -- Represent objects by their actual type rather than allocation site.
   *
   * <p>When this option is set to true, types rather than allocation sites are used as the elements
   * of the points-to sets.
   */
  public void setTypesForSites(boolean typesForSites) {
    this.typesForSites = typesForSites;
  }

  public boolean isMergeStringBuffer() {
    return mergeStringBuffer;
  }

  /**
   * Merge String Buffer -- Represent all StringBuffers as one object.
   *
   * <p>When this option is set to true, all allocation sites creating java.lang.StringBuffer
   * objects are grouped together as a single allocation site.
   */
  public void setMergeStringBuffer(boolean mergeStringBuffer) {
    this.mergeStringBuffer = mergeStringBuffer;
  }

  public boolean isStringConstants() {
    return stringConstants;
  }

  /**
   * Propagate All String Constants -- Propagate all string constants, not just class names.
   *
   * <p>When this option is set to false, Spark only distinguishes string constants that may be the
   * name of a class loaded dynamically using reflection, and all other string constants are lumped
   * together into a single string constant node. Setting this option to true causes all string
   * constants to be propagated individually.
   */
  public void setStringConstants(boolean stringConstants) {
    this.stringConstants = stringConstants;
  }

  public boolean isSimulateNatives() {
    return simulateNatives;
  }

  /**
   * Simulate Natives -- Simulate effects of native methods in standard class library.
   *
   * <p>When this option is set to true, the effects of native methods in the standard Java class
   * library are simulated.
   */
  public void setSimulateNatives(boolean simulateNatives) {
    this.simulateNatives = simulateNatives;
  }

  public boolean isEmptiesAsAllocs() {
    return emptiesAsAllocs;
  }

  /**
   * Treat EMPTY as Alloc -- Treat singletons for empty sets etc. as allocation sites.
   *
   * <p>When this option is set to true, Spark treats references to EMPTYSET, EMPTYMAP, and
   * EMPTYLIST as allocation sites for HashSet, HashMap and LinkedList objects respectively, and
   * references to Hashtable.emptyIterator as allocation sites for Hashtable.EmptyIterator. This
   * enables subsequent analyses to differentiate different uses of Java's immutable empty
   * collections.
   */
  public void setEmptiesAsAllocs(boolean emptiesAsAllocs) {
    this.emptiesAsAllocs = emptiesAsAllocs;
  }

  public boolean isSimpleEdgesBidirectional() {
    return simpleEdgesBidirectional;
  }

  /**
   * Simple Edges Bidirectional -- Equality-based analysis between variable nodes.
   *
   * <p>When this option is set to true, all edges connecting variable (Green) nodes are made
   * bidirectional, as in Steensgaard's analysis.
   */
  public void setSimpleEdgesBidirectional(boolean simpleEdgesBidirectional) {
    this.simpleEdgesBidirectional = simpleEdgesBidirectional;
  }

  public boolean isOnFlyCG() {
    return onFlyCG;
  }

  /**
   * On Fly Call Graph -- Build call graph as receiver types become known.
   *
   * <p>When this option is set to true, the call graph is computed on-the-fly as points-to
   * information is computed. Otherwise, an initial CHA approximation to the call graph is used.
   */
  public void setOnFlyCG(boolean onFlyCG) {
    this.onFlyCG = onFlyCG;
  }

  public boolean isSimplifyOffline() {
    return simplifyOffline;
  }

  /**
   * Simplify Offline -- Collapse single-entry subgraphs of the PAG.
   *
   * <p>When this option is set to true, variable (Green) nodes which form single-entry subgraphs
   * (so they must have the same points-to set) are merged before propagation begins.
   */
  public void setSimplifyOffline(boolean simplifyOffline) {
    this.simplifyOffline = simplifyOffline;
  }

  public boolean isSimplifySCCS() {
    return simplifySCCS;
  }

  /**
   * Simplify SCCs -- Collapse strongly-connected components of the PAG.
   *
   * <p>When this option is set to true, variable (Green) nodes which form strongly-connected
   * components (so they must have the same points-to set) are merged before propagation begins.
   */
  public void setSimplifySCCS(boolean simplifySCCS) {
    this.simplifySCCS = simplifySCCS;
  }

  public boolean isIgnoreTypesForSCCS() {
    return ignoreTypesForSCCS;
  }

  /**
   * Ignore Types For SCCs -- Ignore declared types when determining node equivalence for SCCs.
   *
   * <p>When this option is set to true, when collapsing strongly-connected components, nodes
   * forming SCCs are collapsed regardless of their declared type. The collapsed SCC is given the
   * most general type of all the nodes in the component. When this option is set to false, only
   * edges connecting nodes of the same type are considered when detecting SCCs. This option has no
   * effect unless simplify-sccs is true.
   */
  public void setIgnoreTypesForSCCS(boolean ignoreTypesForSCCS) {
    this.ignoreTypesForSCCS = ignoreTypesForSCCS;
  }

  public PropagatorEnum getPropagator() {
    return propagator;
  }

  /**
   * Set the {@link Propagator} that will be used to propagate {@link PointerAssignmentGraph}
   *
   * @param propagator
   */
  public void setPropagator(PropagatorEnum propagator) {
    this.propagator = propagator;
  }

  public void validate() {
    if (rta && onFlyCG) {
      throw new RuntimeException("Incompatible options rta=true and on-fly-cg=true");
    }
  }
}
