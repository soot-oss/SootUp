package sootup.spark;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2002-2025 Ondrej Lhotak, Kadiray Karakaya and others
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

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import sootup.core.IdentifierFactory;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.expr.JNewMultiArrayExpr;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ArrayType;
import sootup.spark.node.AllocationNode;
import sootup.spark.node.Node;
import sootup.spark.node.ValueToNodeConversionVisitor;
import sootup.spark.node.VariableNode;

/**
 * It is responsible for creating a SPARK representative nodes (nodes present in SPARK, AllocNode,
 * FieldRef Node, and VarNode)
 */
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NodeFactory {

  @NonNull SparkOptions sparkOptions;
  @NonNull IdentifierFactory identifierFactory;

  /**
   * creates a PAG node for a given Jimple value
   *
   * @param value a jimple value, typically a local, field ref, or an allocation expression
   * @return a PAG node
   */
  @NonNull
  public Optional<Node> createNode(@NonNull Value value, MethodSignature containingMethodSig) {
    ValueToNodeConversionVisitor visitor =
        new ValueToNodeConversionVisitor(containingMethodSig, sparkOptions, identifierFactory);
    value.accept(visitor);
    return visitor.getResult();
  }

  /**
   * Returns the edges that model the inner arrays of {@code target = new T[n1][n2]...}: like Soot,
   * each dimension below the outermost one gets an allocation, which is stored into the elements of
   * the enclosing array, also for dimensions without a given size.
   *
   * @param expr the multi-dimensional array creation
   * @param target the local that the outermost array is assigned to
   * @return the edges, from source to target
   */
  @NonNull
  public List<Map.Entry<Node, Node>> createInnerArrayEdges(
      @NonNull JNewMultiArrayExpr expr,
      @NonNull Local target,
      @NonNull MethodSignature containingMethodSig) {
    ValueToNodeConversionVisitor visitor =
        new ValueToNodeConversionVisitor(containingMethodSig, sparkOptions, identifierFactory);
    List<Map.Entry<Node, Node>> edges = new ArrayList<>();
    String enclosingName = target.getName();
    ArrayType enclosingType = (ArrayType) expr.getType();
    while (enclosingType.getElementType() instanceof ArrayType innerType) {
      AllocationNode inner = visitor.allocation(innerType);
      VariableNode innerVariable =
          VariableNode.builder()
              .name(enclosingName + "[]")
              .type(innerType)
              .containingMethodSig(containingMethodSig)
              .build();
      edges.add(new SimpleImmutableEntry<>(inner, innerVariable));
      edges.add(
          new SimpleImmutableEntry<>(
              innerVariable, visitor.arrayElements(enclosingName, enclosingType)));
      enclosingName = innerVariable.getName();
      enclosingType = innerType;
    }
    return edges;
  }
}
