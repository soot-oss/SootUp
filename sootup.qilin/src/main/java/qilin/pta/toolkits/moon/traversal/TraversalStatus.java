package qilin.pta.toolkits.moon.traversal;

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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import qilin.core.pag.LocalVarNode;
import qilin.core.pag.PagNode;
import qilin.pta.toolkits.moon.graph.FlowKind;
import sootup.core.model.SootMethod;

public class TraversalStatus {
  public PagNode pointer;
  public int allocatorLevel;
  public int callStackLevel;
  private Stack<SootMethod> staticCallStack;
  private Map<FlowKind, Integer> visitedFlowKinds;
  public boolean needCheckFieldRelationWhenMeetNew = true;

  public TraversalStatus(
      PagNode pointer,
      int allocatorLevel,
      int callStackLevel,
      PagNode retPtrOfStaticMethod,
      TraversalStatus predTrace,
      FlowKind flowKind) {
    this(pointer, allocatorLevel, callStackLevel, predTrace, flowKind);
    if (retPtrOfStaticMethod instanceof LocalVarNode localVarNode) {
      if (staticCallStack == null) {
        staticCallStack = new Stack<>();
      }
      staticCallStack.push(localVarNode.getMethod());
    }
  }

  public TraversalStatus(
      PagNode pointer,
      int allocatorLevel,
      int callStackLevel,
      TraversalStatus predTrace,
      FlowKind flowKind) {
    this(pointer, allocatorLevel, callStackLevel);
    if (this.visitedFlowKinds == null) {
      visitedFlowKinds = new HashMap<>();
    }
    if (predTrace.visitedFlowKinds != null && !predTrace.visitedFlowKinds.isEmpty()) {
      this.visitedFlowKinds.putAll(predTrace.visitedFlowKinds);
    }
    int i = this.visitedFlowKinds.computeIfAbsent(flowKind, __ -> 0);
    i += 1;
    this.visitedFlowKinds.put(flowKind, i);
    if (predTrace.staticCallStack != null && !predTrace.staticCallStack.isEmpty()) {
      if (staticCallStack == null) {
        staticCallStack = new Stack<>();
      }
      staticCallStack.addAll(predTrace.staticCallStack);
    }

    this.needCheckFieldRelationWhenMeetNew = predTrace.needCheckFieldRelationWhenMeetNew;
  }

  public TraversalStatus(PagNode pointer, int allocatorLevel, int callStackLevel) {
    this.pointer = pointer;
    this.allocatorLevel = allocatorLevel;
    this.callStackLevel = callStackLevel;
  }

  public SootMethod getStaticCallStackTop() {
    if (staticCallStack != null && !staticCallStack.isEmpty()) {
      return staticCallStack.peek();
    }
    return null;
  }

  public int getVisitedFlowCounter(FlowKind flowKind) {
    if (this.visitedFlowKinds == null) return 0;
    return this.visitedFlowKinds.getOrDefault(flowKind, 0);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pointer, callStackLevel, allocatorLevel);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof TraversalStatus other) {
      if (o == this) return true;
      return this.pointer.equals(other.pointer)
          && this.allocatorLevel == other.allocatorLevel
          && this.callStackLevel == other.callStackLevel;
    } else {
      return false;
    }
  }
}
