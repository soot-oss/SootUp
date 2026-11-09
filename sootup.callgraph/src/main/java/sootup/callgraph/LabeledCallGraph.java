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

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import sootup.core.signatures.MethodSignature;

/**
 * A call graph whose calls (edges) can carry labels of type {@code L}. The label type is not
 * restricted, e.g. an enum, a record or a sealed interface to combine several kinds of labels:
 *
 * <pre>{@code
 * enum Hotness { HOT, COLD }
 *
 * LabeledCallGraph<Hotness> cg = LabeledGraphBasedCallGraph.of(callGraph);
 * Set<Hotness> labels = cg.getLabels(call);
 * }</pre>
 *
 * Labels are compared with {@link Object#equals(Object)}, so a label is stored at most once per
 * call. Call graphs that do not need labels should keep using {@link CallGraph}, which has no
 * overhead for labels.
 *
 * @param <L> the type of the labels attached to the calls
 */
public interface LabeledCallGraph<L> extends CallGraph {

  /**
   * This method returns all labels attached to a call in the call graph.
   *
   * @param call the requested call in the call graph
   * @return an unmodifiable set of the labels of the call. It is empty if the call has no labels or
   *     is not contained in the call graph.
   */
  @NonNull Set<L> getLabels(@NonNull Call call);

  /**
   * This method checks if a given label is attached to a call in the call graph.
   *
   * @param call the requested call in the call graph
   * @param label the requested label
   * @return true if the label is attached to the call, otherwise false
   */
  default boolean hasLabel(@NonNull Call call, @NonNull L label) {
    return getLabels(call).contains(label);
  }

  /**
   * This method returns all outgoing calls of a given method signature that carry at least one
   * label matching the given filter.
   *
   * @param sourceMethod the method signature of the requested node in the call graph
   * @param labelFilter the filter the labels of the calls are tested against
   * @return a set of outgoing calls with at least one matching label
   */
  default @NonNull Set<Call> callsFrom(
      @NonNull MethodSignature sourceMethod, @NonNull Predicate<? super L> labelFilter) {
    return callsFrom(sourceMethod).stream()
        .filter(call -> getLabels(call).stream().anyMatch(labelFilter))
        .collect(Collectors.toSet());
  }

  /**
   * This method returns all incoming calls of a given method signature that carry at least one
   * label matching the given filter.
   *
   * @param targetMethod the method signature of the requested node in the call graph
   * @param labelFilter the filter the labels of the calls are tested against
   * @return a set of incoming calls with at least one matching label
   */
  default @NonNull Set<Call> callsTo(
      @NonNull MethodSignature targetMethod, @NonNull Predicate<? super L> labelFilter) {
    return callsTo(targetMethod).stream()
        .filter(call -> getLabels(call).stream().anyMatch(labelFilter))
        .collect(Collectors.toSet());
  }

  /**
   * exports a call of the call graph to an edge in a dot file. The edge label contains the line
   * number of the call and, if present, the labels of the call.
   *
   * @param call the data of the call
   * @return an edge defining the call in the dot file
   */
  @Override
  default StringBuilder toDotEdge(Call call) {
    StringBuilder dotEdge =
        new StringBuilder("\"")
            .append(call.sourceMethodSignature())
            .append("\"->\"")
            .append(call.targetMethodSignature())
            .append("\"[label=\"")
            .append(call.getLineNumber());
    Set<L> labels = getLabels(call);
    if (!labels.isEmpty()) {
      dotEdge
          .append(": ")
          .append(
              labels.stream()
                  .map(label -> label.toString().replace("\"", "\\\""))
                  .collect(Collectors.joining(", ")));
    }
    return dotEdge.append("\"]");
  }

  /**
   * This method copies the call graph including the labels of its calls.
   *
   * @return a mutable copy of the call graph with the same labels
   */
  @Override
  @NonNull MutableLabeledCallGraph<L> copy();
}
