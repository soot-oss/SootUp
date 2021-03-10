package de.upb.swt.soot.callgraph.spark.pag;

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

import de.upb.swt.soot.callgraph.spark.pag.nodes.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InternalEdges {

  /** n2=n1: an edge from n1 to n2 indicates that n1 is added to the points-to set of n2 */
  protected Map<VariableNode, Set<VariableNode>> simpleEdges = new HashMap<>();

  protected Map<VariableNode, Set<VariableNode>> simpleEdgesInv = new HashMap<>();

  protected Map<FieldReferenceNode, Set<VariableNode>> loadEdges = new HashMap<>();
  protected Map<VariableNode, Set<FieldReferenceNode>> loadEdgesInv = new HashMap<>();

  protected Map<VariableNode, Set<FieldReferenceNode>> storeEdges = new HashMap<>();
  protected Map<FieldReferenceNode, Set<VariableNode>> storeEdgesInv = new HashMap<>();

  protected Map<AllocationNode, Set<VariableNode>> allocationEdges = new HashMap<>();
  protected Map<VariableNode, Set<AllocationNode>> allocationEdgesInv = new HashMap<>();

  protected Map<VariableNode, Set<NewInstanceNode>> newInstanceEdges = new HashMap<>();
  protected Map<NewInstanceNode, Set<VariableNode>> newInstanceEdgesInv = new HashMap<>();

  protected Map<NewInstanceNode, Set<VariableNode>> assignInstanceEdges = new HashMap<>();
  protected Map<VariableNode, Set<NewInstanceNode>> assignInstanceEdgesInv = new HashMap<>();

  // TODO: SPARK_OPT simple-edges-bidirectional
  // TODO: Inv edges

  public void addEdge(Node source, Node target) {
    source = source.getReplacement();
    target = target.getReplacement();
    if (source instanceof VariableNode) {
      if (target instanceof VariableNode) {
        addSimpleEdge((VariableNode) source, (VariableNode) target);
      } else if (target instanceof FieldReferenceNode) {
        addStoreEdge((VariableNode) source, (FieldReferenceNode) target);
      } else if (target instanceof NewInstanceNode) {
        addNewInstanceEdge((VariableNode) source, (NewInstanceNode) target);
      } else {
        throw new RuntimeException("Invalid node type:" + target);
      }
    } else if (source instanceof FieldReferenceNode) {
      addLoadEdge((FieldReferenceNode) source, (VariableNode) target);
    } else if (source instanceof NewInstanceNode) {
      addAssignInstanceEdge((NewInstanceNode) source, (VariableNode) target);
    } else {
      addAllocationEdge((AllocationNode) source, (VariableNode) target);
    }
  }

  public boolean addSimpleEdge(VariableNode source, VariableNode target) {
    boolean isNew;
    isNew = simpleEdges.computeIfAbsent(source, v -> new HashSet<>()).add(target);
    isNew |= simpleEdgesInv.computeIfAbsent(target, v -> new HashSet<>()).add(source);
    return isNew;
  }

  public boolean addStoreEdge(VariableNode source, FieldReferenceNode target) {
    boolean isNew;
    isNew = storeEdges.computeIfAbsent(source, v -> new HashSet<>()).add(target);
    isNew |= storeEdgesInv.computeIfAbsent(target, v -> new HashSet<>()).add(source);
    return isNew;
  }

  public boolean addLoadEdge(FieldReferenceNode source, VariableNode target) {
    boolean isNew;
    isNew = loadEdges.computeIfAbsent(source, v -> new HashSet<>()).add(target);
    isNew |= loadEdgesInv.computeIfAbsent(target, v -> new HashSet<>()).add(source);
    return isNew;
  }

  public boolean addAllocationEdge(AllocationNode source, VariableNode target) {
    boolean isNew;
    isNew = allocationEdges.computeIfAbsent(source, v -> new HashSet<>()).add(target);
    isNew |= allocationEdgesInv.computeIfAbsent(target, v -> new HashSet<>()).add(source);
    return isNew;
  }

  public boolean addNewInstanceEdge(VariableNode source, NewInstanceNode target) {
    boolean isNew;
    isNew = newInstanceEdges.computeIfAbsent(source, v -> new HashSet<>()).add(target);
    isNew |= newInstanceEdgesInv.computeIfAbsent(target, v -> new HashSet<>()).add(source);
    return isNew;
  }

  public boolean addAssignInstanceEdge(NewInstanceNode source, VariableNode target) {
    boolean isNew;
    isNew = assignInstanceEdges.computeIfAbsent(source, v -> new HashSet<>()).add(target);
    isNew |= assignInstanceEdgesInv.computeIfAbsent(target, v -> new HashSet<>()).add(source);
    return isNew;
  }
}
