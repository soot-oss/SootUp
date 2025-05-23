package sootup.spark.node;

import lombok.NonNull;
import lombok.val;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.*;
import sootup.java.core.JavaIdentifierFactory;

import java.util.Optional;

/**
 * {@link sootup.core.jimple.basic.Value} to {@link Node} converter.
 * Supported notes according to the Spark thesis:
 * <ul>
 *     <li>AllocationNode</li>
 *     <li>VariableNode</li>
 *     <li>FieldRefNode</li>
 */
public class ValueToNodeConversionVisitor extends AbstractValueToNodeConversionVisitor {

    private Node node;

    @NonNull
    public Optional<Node> get() {
        return Optional.of(node);
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
        this.node = FieldRefNode.builder().field(ref.getFieldSignature()).type(ref.getType()).build();
    }

    @Override
    public void caseInstanceFieldRef(JInstanceFieldRef ref) {
        val base = VariableNode.builder().name(ref.getBase().getName()).type(ref.getBase().getType()).build();
        this.node = FieldRefNode.builder().base(base).field(ref.getFieldSignature()).type(ref.getType()).build();
    }

    @Override
    public void caseArrayRef(JArrayRef ref) {
        val base = VariableNode.builder().name(ref.getBase().getName()).type(ref.getBase().getType()).build();
        val declaringClassType = JavaIdentifierFactory.getInstance().getClassType(ref.getBase().getType().toString());
        val field = JavaIdentifierFactory.getInstance().getFieldSignature(String.valueOf(ref.getIndex()), declaringClassType, ref.getType());
        this.node = FieldRefNode.builder().base(base).field(field).type(ref.getType()).build();
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

}
