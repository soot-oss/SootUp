package sootup.spark.node;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2002-2025 Ondrej Lhotak, Kadiray Karakaya, Palaniappan Muthuraman
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
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.expr.JNewMultiArrayExpr;
import sootup.core.jimple.common.ref.*;
import sootup.core.jimple.visitor.AbstractValueVisitor;
import sootup.java.core.JavaIdentifierFactory;

/**
 * {@link sootup.core.jimple.basic.Value} to {@link Node} converter. Supported nodes according to
 * the Spark thesis:
 *
 * <ul>
 *   <li>AllocationNode
 *   <li>VariableNode
 *   <li>FieldRefNode
 * </ul>
 */
@Slf4j
public class ValueToNodeConversionVisitor extends AbstractValueVisitor {

  private Node node;

  /**
   * returns a node as a result of the value to node conversion
   *
   * @return an optional PAG node
   */
  public Optional<Node> getResult() {
    return Optional.ofNullable(node);
  }

  @Override
  public void caseCastExpr(JCastExpr expr) {
    // TODO: wip
    defaultCaseValue(expr);
  }

  @Override
  public void caseNewArrayExpr(JNewArrayExpr expr) {
    // TODO: wip
    defaultCaseValue(expr);
  }

  @Override
  public void caseNewMultiArrayExpr(JNewMultiArrayExpr expr) {
    // TODO: wip
    defaultCaseValue(expr);
  }

  @Override
  public void caseNewExpr(JNewExpr expr) {
    this.node = AllocationNode.builder().type(expr.getType()).build();
  }

  @Override
  public void caseStaticFieldRef(JStaticFieldRef ref) {
    this.node =
        StaticFieldRefNode.builder()
            .field(ref.getFieldSignature())
            .type(ref.getFieldSignature().getDeclClassType())
            .build();
  }

  @Override
  public void caseInstanceFieldRef(JInstanceFieldRef ref) {
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
  public void caseArrayRef(JArrayRef ref) {
    val base =
        VariableNode.builder().name(ref.getBase().getName()).type(ref.getBase().getType()).build();
    val declaringClassType =
        JavaIdentifierFactory.getInstance().getClassType(ref.getBase().getType().toString());
    val field =
        JavaIdentifierFactory.getInstance()
            .getFieldSignature(String.valueOf(ref.getIndex()), declaringClassType, ref.getType());
    this.node = InstanceFieldRefNode.builder().base(base).field(field).type(ref.getType()).build();
  }

  @Override
  public void caseParameterRef(JParameterRef ref) {
    // TODO: wip
    defaultCaseValue(ref);
  }

  @Override
  public void caseThisRef(JThisRef ref) {
    // TODO: wip
    defaultCaseValue(ref);
  }

  @Override
  public void caseLocal(Local local) {
    this.node = VariableNode.builder().type(local.getType()).name(local.getName()).build();
  }

  @Override
  public void defaultCaseValue(@NonNull Value v) {
    log.warn("Unimplemented node conversion for value: {} of type: {}", v, v.getClass());
  }
}
