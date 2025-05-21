package sootup.spark.node;

import lombok.NonNull;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.*;

import java.util.Optional;

public class NodeValueVisitor extends AbstractNodeValueVisitor {

    private Node node;

    @NonNull
    public Optional<Node> get() {
        return Optional.of(node);
    }

    @Override
    public void caseSpecialInvokeExpr(JSpecialInvokeExpr expr) {

    }

    @Override
    public void caseVirtualInvokeExpr(JVirtualInvokeExpr expr) {

    }

    @Override
    public void caseInterfaceInvokeExpr(JInterfaceInvokeExpr expr) {

    }

    @Override
    public void caseStaticInvokeExpr(JStaticInvokeExpr expr) {

    }

    @Override
    public void caseDynamicInvokeExpr(JDynamicInvokeExpr expr) {

    }

    @Override
    public void caseCastExpr(JCastExpr expr) {

    }

    @Override
    public void caseNewArrayExpr(JNewArrayExpr expr) {

    }

    @Override
    public void caseNewMultiArrayExpr(JNewMultiArrayExpr expr) {

    }

    @Override
    public void caseNewExpr(JNewExpr expr) {
        this.node = AllocationNode.builder().type(expr.getType()).build();
    }

    @Override
    public void caseStaticFieldRef(JStaticFieldRef ref) {

    }

    @Override
    public void caseInstanceFieldRef(JInstanceFieldRef ref) {

    }

    @Override
    public void caseArrayRef(JArrayRef ref) {

    }

    @Override
    public void caseParameterRef(JParameterRef ref) {

    }

    @Override
    public void caseCaughtExceptionRef(JCaughtExceptionRef ref) {

    }

    @Override
    public void caseThisRef(JThisRef ref) {

    }

    @Override
    public void caseLocal(Local local) {
        this.node = VariableNode.builder().type(local.getType()).name(local.getName()).build();
    }

}
