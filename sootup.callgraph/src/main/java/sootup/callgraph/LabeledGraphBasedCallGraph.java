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

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.core.signatures.MethodSignature;

/**
 * This class implements a mutable call graph as a graph whose calls can carry labels of type {@code
 * L}. The labels of a call keep their insertion order.
 *
 * <p>Call graph algorithms create a plain {@link GraphBasedCallGraph}. Use {@link #of(CallGraph)}
 * to label their result, or override {@link AbstractCallGraphAlgorithm#initializeCallGraph(List,
 * List)} to return a labeled call graph that is filled during the construction.
 *
 * <p>{@link #diff(CallGraph)} compares the calls only and ignores labels.
 *
 * @param <L> the type of the labels attached to the calls
 */
public class LabeledGraphBasedCallGraph<L> extends GraphBasedCallGraph
    implements MutableLabeledCallGraph<L> {

  @NonNull private final SetMultimap<Call, L> labels;

  /**
   * The constructor of the labeled graph based call graph. it initializes an empty call graph.
   *
   * @param entryMethods the entry methods of the call graph
   */
  public LabeledGraphBasedCallGraph(@NonNull List<MethodSignature> entryMethods) {
    super(entryMethods);
    this.labels = LinkedHashMultimap.create();
  }

  protected LabeledGraphBasedCallGraph(
      @NonNull GraphBasedCallGraph callGraph, @NonNull SetMultimap<Call, L> labels) {
    super(callGraph);
    this.labels = labels;
  }

  /**
   * Creates a labeled copy of the given call graph. The given call graph is not modified. If it is
   * a {@link LabeledCallGraph} its labels are copied as well; they must be of type {@code L}.
   *
   * @param callGraph the call graph whose methods and calls are copied
   * @return a labeled call graph containing the methods and calls of the given call graph
   * @param <L> the type of the labels attached to the calls
   */
  @SuppressWarnings("unchecked")
  public static <L> @NonNull LabeledGraphBasedCallGraph<L> of(@NonNull CallGraph callGraph) {
    SetMultimap<Call, L> labels = LinkedHashMultimap.create();
    if (callGraph instanceof LabeledCallGraph<?> labeledCallGraph) {
      for (Call call : callGraph.getCalls()) {
        labels.putAll(call, (Set<L>) labeledCallGraph.getLabels(call));
      }
    }
    if (callGraph instanceof GraphBasedCallGraph graphBasedCallGraph) {
      return new LabeledGraphBasedCallGraph<>(graphBasedCallGraph, labels);
    }
    LabeledGraphBasedCallGraph<L> labeled =
        new LabeledGraphBasedCallGraph<>(new ArrayList<>(callGraph.getEntryMethods()));
    callGraph.getMethodSignatures().forEach(labeled::addMethod);
    callGraph.getCalls().forEach(labeled::addCall);
    labeled.labels.putAll(labels);
    return labeled;
  }

  @Override
  public void addLabel(@NonNull Call call, @NonNull L label) {
    if (!containsCall(call)) {
      throw new IllegalArgumentException("Call " + call + " is not contained in the call graph");
    }
    labels.put(call, label);
  }

  @Override
  public boolean removeLabel(@NonNull Call call, @NonNull L label) {
    return labels.remove(call, label);
  }

  @NonNull
  @Override
  public Set<L> getLabels(@NonNull Call call) {
    return Collections.unmodifiableSet(labels.get(call));
  }

  @NonNull
  @Override
  public LabeledGraphBasedCallGraph<L> copy() {
    return new LabeledGraphBasedCallGraph<>(this, LinkedHashMultimap.create(labels));
  }

  @Override
  protected String printCallingMethods(CallGraph.Call call) {
    return super.printCallingMethods(call) + printLabels(call);
  }

  @Override
  protected String printCalledMethods(CallGraph.Call call) {
    return super.printCalledMethods(call) + printLabels(call);
  }

  /**
   * This returns the string that is used in the toString Method to print the labels of a call
   *
   * @param call The data of the call
   * @return the labels of the call prefixed by a space, or an empty string if the call has no
   *     labels
   */
  protected String printLabels(CallGraph.Call call) {
    Set<L> callLabels = getLabels(call);
    return callLabels.isEmpty() ? "" : " " + callLabels;
  }
}
