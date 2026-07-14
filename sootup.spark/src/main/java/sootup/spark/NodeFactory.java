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

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import sootup.core.jimple.common.Value;
import sootup.core.signatures.MethodSignature;
import sootup.spark.node.Node;
import sootup.spark.node.ValueToNodeConversionVisitor;

/**
 * It is responsible for creating a SPARK representative nodes (nodes present in SPARK, AllocNode,
 * FieldRef Node, and VarNode)
 */
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NodeFactory {

  @NonNull SparkOptions sparkOptions;

  /**
   * creates a PAG node for a given Jimple value
   *
   * @param value a jimple value, typically a local, field ref, or an allocation expression
   * @return a PAG node
   */
  @NonNull
  public Optional<Node> createNode(@NonNull Value value, MethodSignature containingMethodSig) {
    ValueToNodeConversionVisitor visitor =
        new ValueToNodeConversionVisitor(containingMethodSig, sparkOptions);
    value.accept(visitor);
    return visitor.getResult();
  }
}
