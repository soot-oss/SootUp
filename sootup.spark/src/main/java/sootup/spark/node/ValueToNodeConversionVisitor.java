package sootup.spark.node;

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
 * {@link sootup.core.jimple.basic.Value} to {@link Node} converter. Supported notes according to
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
