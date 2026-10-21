package sootup.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Jonas Klauke
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

/**
 * A mutable call graph whose calls (edges) can carry labels of type {@code L}.
 *
 * @param <L> the type of the labels attached to the calls
 */
public interface MutableLabeledCallGraph<L> extends MutableCallGraph, LabeledCallGraph<L> {

  /**
   * This method attaches a label to a call in the call graph. A call can carry several labels. A
   * label that is already attached to the call is not added a second time.
   *
   * @param call the call the label is attached to. It must be contained in the call graph.
   * @param label the label that will be attached to the call
   * @throws IllegalArgumentException if the call is not contained in the call graph
   */
  void addLabel(@NonNull Call call, @NonNull L label);

  /**
   * This method removes a label from a call in the call graph.
   *
   * @param call the call the label is removed from
   * @param label the label that will be removed
   * @return true if the label was attached to the call, otherwise false
   */
  boolean removeLabel(@NonNull Call call, @NonNull L label);

  /**
   * This method adds a call to the call graph if it is not contained yet and attaches the given
   * label to it.
   *
   * @param call the call that will be added to the call graph
   * @param label the label that will be attached to the call
   */
  default void addCall(@NonNull Call call, @NonNull L label) {
    if (!containsCall(call)) {
      addCall(call);
    }
    addLabel(call, label);
  }
}
