package sootup.spark.node;

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
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.expr.JNewMultiArrayExpr;
import sootup.core.jimple.common.ref.*;
import sootup.core.jimple.visitor.AbstractValueVisitor;
import sootup.core.signatures.FieldSignature;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.spark.Engine;

/**
 * {@link Value} to {@link Node} converter. Supported nodes according to the Spark thesis:
 *
 * <ul>
 *   <li>AllocationNode
 *   <li>VariableNode
 *   <li>FieldRefNode
 * </ul>
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ValueToNodeConversionVisitor extends AbstractValueVisitor {

  Node node;

  /**
   * returns a node as a result of the value to node conversion
   *
   * @return an optional PAG node
   */
  public Optional<Node> getResult() {
    return Optional.ofNullable(node);
  }

  @Override
  public void caseCastExpr(@NonNull JCastExpr expr) {
    // TODO: wip
    defaultCaseValue(expr);
  }

  @Override
  public void caseNewArrayExpr(@NonNull JNewArrayExpr expr) {
    // TODO: wip
    defaultCaseValue(expr);
  }

  @Override
  public void caseNewMultiArrayExpr(@NonNull JNewMultiArrayExpr expr) {
    // TODO: wip
    defaultCaseValue(expr);
  }

  @Override
  public void caseNewExpr(@NonNull JNewExpr expr) {
    long allocCount = Engine.incrementAndGetAllocCount();
    this.node = AllocationNode.builder().type(expr.getType()).allocationSite(allocCount).build();
  }

  @Override
  public void caseStaticFieldRef(@NonNull JStaticFieldRef ref) {
    this.node =
        StaticFieldRefNode.builder()
            .field(ref.getFieldSignature())
            .type(ref.getFieldSignature().getDeclClassType())
            .build();
  }

  @Override
  public void caseInstanceFieldRef(@NonNull JInstanceFieldRef ref) {
    val base =
        VariableNode.builder().name(ref.getBase().getName()).type(ref.getBase().getType()).build();
    this.node =
        InstanceFieldRefNode.builder()
            .base(base)
            .field(ref.getFieldSignature())
            .type(ref.getType())
            .build();
  }

  @Override
  public void caseArrayRef(@NonNull JArrayRef ref) {
    val type = ref.getBase().getType();
    if (type instanceof ArrayType arrayType) {
      val baseType = arrayType.getBaseType();
      if (baseType instanceof ClassType declaringClassType) {
        val field =
            new FieldSignature(declaringClassType, String.valueOf(ref.getIndex()), ref.getType());
        val base =
            VariableNode.builder()
                .name(ref.getBase().getName())
                .type(ref.getBase().getType())
                .build();
        this.node =
            InstanceFieldRefNode.builder().base(base).field(field).type(ref.getType()).build();
      }
    }
  }

  @Override
  public void caseParameterRef(@NonNull JParameterRef ref) {
    // TODO: wip
    defaultCaseValue(ref);
  }

  @Override
  public void caseThisRef(@NonNull JThisRef ref) {
    // TODO: wip
    defaultCaseValue(ref);
  }

  @Override
  public void caseLocal(@NonNull Local local) {
    this.node = VariableNode.builder().type(local.getType()).name(local.getName()).build();
  }

  @Override
  public void defaultCaseValue(@NonNull Value v) {
    log.warn("Unimplemented node conversion for value: {} of type: {}", v, v.getClass());
  }
}
