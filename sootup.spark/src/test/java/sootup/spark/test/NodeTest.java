package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.signatures.PackageName;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.spark.NodeFactory;
import sootup.spark.node.AllocationNode;
import sootup.spark.node.FieldRefNode;
import sootup.spark.node.VariableNode;


public class NodeTest {

    private ClassType simpleType(String name){
        return new ClassType() {
            @Override
            public String getFullyQualifiedName() {
                return name;
            }

            @Override
            public String getClassName() {
                return name;
            }

            @Override
            public PackageName getPackageName() {
                return new PackageName("");
            }
        };
    }

    @Test
    public void testValueToNodeConversions(){
        val aType = simpleType("A");
        val bType = simpleType("B");

        // Local variable
        val local = new Local("a", aType);
        val varNodeOpt = NodeFactory.createNode(local);
        assertTrue(varNodeOpt.isPresent());
        val varNode = varNodeOpt.get();
        assertTrue(varNode instanceof VariableNode);
        VariableNode variableNode = (VariableNode) varNode;
        assertEquals(aType, variableNode.getType());
        assertEquals(local.getName(), variableNode.getName());

        // New allocation
        val newExpr = new JNewExpr(aType);
        val allocNodeOpt = NodeFactory.createNode(newExpr);
        assertTrue(allocNodeOpt.isPresent());
        val allocNode  = allocNodeOpt.get();
        assertTrue(allocNode instanceof AllocationNode);
        assertEquals(aType, allocNode.getType());

        // Instance FieldRef class A{B someB.f}
        val fieldSig = JavaIdentifierFactory.getInstance().getFieldSignature("f", aType, bType);
        val base = new Local("someB", aType);
        val instanceFieldRef = new JInstanceFieldRef(base, fieldSig);
        val instanceFieldRefNodeOpt = NodeFactory.createNode(instanceFieldRef);
        assertTrue(instanceFieldRefNodeOpt.isPresent());
        val refNode = instanceFieldRefNodeOpt.get();
        assertTrue(refNode instanceof FieldRefNode);
        val instanceFieldRefNode = (FieldRefNode) refNode;
        assertEquals(bType, instanceFieldRefNode.getType());
        assertEquals(fieldSig, instanceFieldRefNode.getField());
        assertEquals(base.getName(), instanceFieldRefNode.getBase().getName());
        assertEquals(base.getType(), instanceFieldRefNode.getBase().getType());

        // Static FieldRef A{B f}
        val staticFieldRef = new JStaticFieldRef(fieldSig);
        val staticFieldRefNodeOpt = NodeFactory.createNode(staticFieldRef);
        assertTrue(staticFieldRefNodeOpt.isPresent());
        val sRefNode = staticFieldRefNodeOpt.get();
        assertTrue(sRefNode instanceof FieldRefNode);
        val staticRefNode = (FieldRefNode) sRefNode;
        assertEquals(fieldSig, staticRefNode.getField());
        assertEquals(null, staticRefNode.getBase());
        assertEquals(bType, staticRefNode.getType());

        // Array Element
        val arrayType = ArrayType.createArrayType(aType, 1);
        val arrayRef = new JArrayRef(new Local("array", arrayType), IntConstant.getInstance(42));
        val arrayRefNodeOpt = NodeFactory.createNode(arrayRef);
        assertTrue(arrayRefNodeOpt.isPresent());
        val arrayNode = arrayRefNodeOpt.get();
        assertTrue(arrayNode instanceof FieldRefNode);
        val arrayRefNode = (FieldRefNode) arrayNode;
        assertEquals(arrayType, arrayRefNode.getBase().getType());
        assertEquals("array", arrayRefNode.getBase().getName());
        assertEquals("42", arrayRefNode.getField().getName());
        assertEquals(aType, arrayRef.getType());
    }

    public void testStatementToEdgeConversions(){
        LValue left = new Local("a", simpleType("A"));
        Value right = new JNewExpr(simpleType("A"));
        JAssignStmt assignStmt = new JAssignStmt(left, right, StmtPositionInfo.getNoStmtPositionInfo());
    }

}
