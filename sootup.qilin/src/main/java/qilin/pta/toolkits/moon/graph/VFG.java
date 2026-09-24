package qilin.pta.toolkits.moon.graph;

/*-
 * #%L
 * SootUp - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt and others
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

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import qilin.core.pag.LocalVarNode;
import qilin.core.pag.PagNode;
import qilin.core.pag.SparkField;
import qilin.core.pag.VarNode;
import sootup.core.model.SootMethod;

public class VFG {

  private final Set<LocalVarNode> thisVars = ConcurrentHashMap.newKeySet();

  private final SetMultimap<PagNode, FlowEdge> outEdges =
      Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet);
  private final SetMultimap<PagNode, FlowEdge> inEdges =
      Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet);

  private final Set<LocalVarNode> containerVars = ConcurrentHashMap.newKeySet();

  private final Set<SootMethod> inlineCallers = ConcurrentHashMap.newKeySet();

  public void addFieldFlowEdge(FlowKind flowKind, PagNode src, PagNode tgt, SparkField field) {
    if (flowKind == FlowKind.FIELD_LOAD) {
      containerVars.add((LocalVarNode) src);
    } else if (flowKind == FlowKind.FIELD_STORE) {
      containerVars.add((LocalVarNode) tgt);
    } else {
      throw new RuntimeException("Unexpected flow kind: " + flowKind);
    }
    FieldEdge edge = new FieldEdge(src, tgt, flowKind, field);
    outEdges.put(src, edge);
    inEdges.put(tgt, edge);
  }

  public void addSimpleFlowEdge(FlowKind flowKind, PagNode src, PagNode tgt) {
    if (flowKind == FlowKind.CALL_LOAD) {
      containerVars.add((LocalVarNode) src);
    } else if (flowKind == FlowKind.CALL_STORE) {
      containerVars.add((LocalVarNode) tgt);
    }
    FlowEdge edge = new FlowEdge(src, tgt, flowKind);
    outEdges.put(src, edge);
    inEdges.put(tgt, edge);
  }

  public void recordThisVar(VarNode thisVar) {
    thisVars.add((LocalVarNode) thisVar);
  }

  public Set<LocalVarNode> getThisVars() {
    return thisVars;
  }

  public Set<FlowEdge> getPredsOf(PagNode node) {
    return inEdges.get(node);
  }

  public Set<FlowEdge> getSuccsOf(PagNode node) {
    return outEdges.get(node);
  }

  public Set<PagNode> getNodes() {
    Set<PagNode> ret = new HashSet<>();
    ret.addAll(inEdges.keySet());
    ret.addAll(outEdges.keySet());
    return ret;
  }

  public Set<LocalVarNode> getContainerVars() {
    return containerVars;
  }

  public void recordInlineMethod(SootMethod callee, SootMethod caller) {
    inlineCallers.add(caller);
  }

  public Set<SootMethod> getInlineCallers() {
    return inlineCallers;
  }
}
