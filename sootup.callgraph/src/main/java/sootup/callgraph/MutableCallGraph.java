package sootup.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Linghui Luo, Christian Brüggemann, Ben Hermann, Markus Schmidt
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

import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.signatures.MethodSignature;

/**
 * This interface defines a mutable call graph. this means a call graph that can be modified after
 * the creation.
 */
public interface MutableCallGraph extends CallGraph {

  /**
   * This method enables to add method that are nodes in the call graph.
   *
   * @param calledMethod the method that will be added to the call graph.
   */
  void addMethod(@NonNull MethodSignature calledMethod);

  /**
   * This method enables to add calls that are edges in the call graph.
   *
   * @param sourceMethod this parameter defines the source node of the edge in the call graph.
   * @param targetMethod this paramter defines the target node of the edge in the call graph.
   * @param invokableStmt this paramter defines the invoke statement of the edge in the call graph.
   */
  void addCall(
      @NonNull MethodSignature sourceMethod,
      @NonNull MethodSignature targetMethod,
      @NonNull InvokableStmt invokableStmt);

  /**
   * This method enables to add calls that are edges in the call graph.
   *
   * @param call this parameter defines the call that is transformed to the edge in the call graph.
   */
  void addCall(@NonNull Call call);

  /**
   * This method attaches a label to a call in the call graph. A call can carry several labels. A
   * label that is already attached to the call is not added a second time.
   *
   * @param call the call the label is attached to. It must be contained in the call graph.
   * @param label the label that will be attached to the call
   * @throws IllegalArgumentException if the call is not contained in the call graph
   * @throws UnsupportedOperationException if the call graph does not support labels
   */
  default void addLabel(@NonNull Call call, @NonNull CallLabel label) {
    throw new UnsupportedOperationException(
        getClass().getSimpleName() + " does not support call labels");
  }

  /**
   * This method adds a call to the call graph if it is not contained yet and attaches the given
   * label to it.
   *
   * @param call the call that will be added to the call graph
   * @param label the label that will be attached to the call
   */
  default void addCall(@NonNull Call call, @NonNull CallLabel label) {
    if (!containsCall(call)) {
      addCall(call);
    }
    addLabel(call, label);
  }
}
