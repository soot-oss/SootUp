package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.spark.Engine;
import sootup.spark.NodeFactory;
import sootup.spark.SparkOptions;
import sootup.spark.node.AllocationNode;
import sootup.spark.node.InstanceFieldRefNode;
import sootup.spark.node.StaticFieldRefNode;
import sootup.spark.node.VariableNode;

class ValueToNodeConversionTest {

  NodeFactory nodeFactory = new NodeFactory(SparkOptions.defaultOptions());
  ClassType aType = SparkTestUtil.simpleType("A");
  ClassType bType = SparkTestUtil.simpleType("B");
  FieldSignature fieldSig =
      JavaIdentifierFactory.getInstance().getFieldSignature("f", aType, bType);
  MethodSignature methodSig =
      JavaIdentifierFactory.getInstance()
          .getMethodSignature(aType, "test", "void", Collections.emptyList());

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
  }

  @Test
  void testLocalToNodeConversion() {
    // Local variable
    val local = new Local("a", aType);
    val varNodeOpt = nodeFactory.createNode(local, methodSig);
    assertTrue(varNodeOpt.isPresent());
    val varNode = varNodeOpt.get();
    assertTrue(varNode instanceof VariableNode);
    VariableNode variableNode = (VariableNode) varNode;
    assertEquals(aType, variableNode.getType());
    assertEquals(local.getName(), variableNode.getName());
    assertEquals("\"test{A a}\"", variableNode.toString());
  }

  @Test
  void testNewExprToNodeConversion() {
    // New allocation
    val newExpr = new JNewExpr(aType);
    val allocNodeOpt = nodeFactory.createNode(newExpr, methodSig);
    assertTrue(allocNodeOpt.isPresent());
    val allocNode = allocNodeOpt.get();
    assertTrue(allocNode instanceof AllocationNode);
    assertEquals(aType, allocNode.getType());
    assertEquals("\"test{1:new A}\"", allocNode.toString());
  }

  @Test
  void testInstanceFieldRefToNodeConversion() {
    // Instance FieldRef class A{B someB.f}
    val base = new Local("someB", aType);
    val instanceFieldRef = new JInstanceFieldRef(base, fieldSig);
    val instanceFieldRefNodeOpt = nodeFactory.createNode(instanceFieldRef, methodSig);
    assertTrue(instanceFieldRefNodeOpt.isPresent());
    val refNode = instanceFieldRefNodeOpt.get();
    assertTrue(refNode instanceof InstanceFieldRefNode);
    val instanceFieldRefNode = (InstanceFieldRefNode) refNode;
    assertEquals(bType, instanceFieldRefNode.getType());
    assertEquals(fieldSig, instanceFieldRefNode.getField());
    assertEquals(base.getName(), instanceFieldRefNode.getBase().getName());
    assertEquals(base.getType(), instanceFieldRefNode.getBase().getType());
    assertEquals("\"test{B (A someB).f}\"", instanceFieldRefNode.toString());
  }

  @Test
  void testStaticFieldRefToNodeConversion() {
    // Static FieldRef A{B f}
    val staticFieldRef = new JStaticFieldRef(fieldSig);
    val staticFieldRefNodeOpt = nodeFactory.createNode(staticFieldRef, methodSig);
    assertTrue(staticFieldRefNodeOpt.isPresent());
    val sRefNode = staticFieldRefNodeOpt.get();
    assertTrue(sRefNode instanceof StaticFieldRefNode);
    val staticRefNode = (StaticFieldRefNode) sRefNode;
    assertEquals(fieldSig, staticRefNode.getField());
    assertEquals(aType, staticRefNode.getType());
    assertEquals("\"test{B A.f}\"", staticRefNode.toString());
  }

  @Test
  void testArrayElementToNodeConversion() {
    // Array Element
    val arrayType = ArrayType.createArrayType(aType, 1);
    val arrayRef = new JArrayRef(new Local("array", arrayType), IntConstant.getInstance(42));
    val arrayRefNodeOpt = nodeFactory.createNode(arrayRef, methodSig);
    assertTrue(arrayRefNodeOpt.isPresent());
    val arrayNode = arrayRefNodeOpt.get();
    assertTrue(arrayNode instanceof InstanceFieldRefNode);
    val arrayRefNode = (InstanceFieldRefNode) arrayNode;
    assertEquals(arrayType, arrayRefNode.getBase().getType());
    assertEquals("array", arrayRefNode.getBase().getName());
    assertEquals("42", arrayRefNode.getField().getName());
    assertEquals(aType, arrayRef.getType());
    assertEquals("\"test{A (A[] array).42}\"", arrayRefNode.toString());
  }

  /** TODO: how to handle cast expr */
  @Test
  void testCastExprToNodeConversion() {
    val local = JavaJimple.newLocal("r0", bType);
    val jcastExpr = JavaJimple.newCastExpr(local, aType);
    val node = nodeFactory.createNode(jcastExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  /** TODO: how to handle new array expr */
  @Test
  void testNewArrayExprToNodeConversion() {
    val newArrayExpr =
        JavaJimple.newNewArrayExpr(
            aType, IntConstant.getInstance(1), JavaIdentifierFactory.getInstance());
    val node = nodeFactory.createNode(newArrayExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  /** TODO: how to handle new multi array expr */
  @Test
  void testNewMultiArrayExprToNodeConversion() {
    val arrayType = ArrayType.createArrayType(aType, 2);
    val newMultiArrayExpr =
        JavaJimple.newNewMultiArrayExpr(
            arrayType, List.of(IntConstant.getInstance(1), IntConstant.getInstance(1)));
    val node = nodeFactory.createNode(newMultiArrayExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testParameterRefToNodeConversion() {
    val paramRef = JavaJimple.newParameterRef(aType, 1);
    val node = nodeFactory.createNode(paramRef, methodSig);
    assertTrue(node.isPresent());
    assertTrue(node.get() instanceof VariableNode);
    assertEquals("\"test{A @parameter1}\"", node.get().toString());
  }

  @Test
  void testThisRefToNodeConversion() {
    val thisRef = JavaJimple.newThisRef(aType);
    val node = nodeFactory.createNode(thisRef, methodSig);
    assertTrue(node.isPresent());
    assertTrue(node.get() instanceof VariableNode);
    assertEquals("\"test{A @this}\"", node.get().toString());
  }
}
