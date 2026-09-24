package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.BooleanConstant;
import sootup.core.jimple.common.constant.DoubleConstant;
import sootup.core.jimple.common.constant.FloatConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.jimple.common.constant.MethodHandle;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.expr.JPhiExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
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

  @Test
  void testCastExprToNodeConversion() {
    val local = JavaJimple.newLocal("r0", bType);
    val jcastExpr = JavaJimple.newCastExpr(local, aType);
    val node = nodeFactory.createNode(jcastExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testNewArrayExprToNodeConversion() {
    val newArrayExpr =
        JavaJimple.newNewArrayExpr(
            aType, IntConstant.getInstance(1), JavaIdentifierFactory.getInstance());
    val node = nodeFactory.createNode(newArrayExpr, methodSig);
    assertTrue(node.isEmpty());
  }

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

  @Test
  void testBooleanConstantToNodeConversion() {
    val booleanConstant = BooleanConstant.getInstance(true);
    val node = nodeFactory.createNode(booleanConstant, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testClassConstantToNodeConversion() {
    val classConstant = JavaJimple.newClassConstant(aType.toString());
    val node = nodeFactory.createNode(classConstant, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testDoubleConstantToNodeConversion() {
    val doubleConstant = DoubleConstant.getInstance(3.14);
    val node = nodeFactory.createNode(doubleConstant, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testFloatConstantToNodeConversion() {
    val floatConstant = FloatConstant.getInstance(3.14f);
    val node = nodeFactory.createNode(floatConstant, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testIntConstantToNodeConversion() {
    val intConstant = IntConstant.getInstance(42);
    val node = nodeFactory.createNode(intConstant, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testLongConstantToNodeConversion() {
    val longConstant = LongConstant.getInstance(42L);
    val node = nodeFactory.createNode(longConstant, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testNullConstantToNodeConversion() {
    val nullConstant = NullConstant.getInstance();
    val node = nodeFactory.createNode(nullConstant, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testAddExprToNodeConversion() {
    val local1 = new Local("a", aType);
    val local2 = new Local("b", aType);
    val addExpr = JavaJimple.newAddExpr(local1, local2);
    val node = nodeFactory.createNode(addExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testAndExprToNodeConversion() {
    val local1 = new Local("a", aType);
    val local2 = new Local("b", aType);
    val andExpr = JavaJimple.newAndExpr(local1, local2);
    val node = nodeFactory.createNode(andExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testCmpExprToNodeConversion() {
    val local1 = new Local("a", aType);
    val local2 = new Local("b", aType);
    val cmpExpr = JavaJimple.newCmpExpr(local1, local2);
    val node = nodeFactory.createNode(cmpExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testCmpgExprToNodeConversion() {
    val local1 = new Local("a", aType);
    val local2 = new Local("b", aType);
    val cmpgExpr = JavaJimple.newCmpgExpr(local1, local2);
    val node = nodeFactory.createNode(cmpgExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testCmplExprToNodeConversion() {
    val local1 = new Local("a", aType);
    val local2 = new Local("b", aType);
    val cmplExpr = JavaJimple.newCmplExpr(local1, local2);
    val node = nodeFactory.createNode(cmplExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testDivExprToNodeConversion() {
    val local1 = new Local("a", aType);
    val local2 = new Local("b", aType);
    val divExpr = JavaJimple.newDivExpr(local1, local2);
    val node = nodeFactory.createNode(divExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testEqExprToNodeConversion() {
    val local1 = new Local("a", aType);
    val local2 = new Local("b", aType);
    val eqExpr = JavaJimple.newEqExpr(local1, local2);
    val node = nodeFactory.createNode(eqExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testGeExprToNodeConversion() {
    val local1 = new Local("a", aType);
    val local2 = new Local("b", aType);
    val geExpr = JavaJimple.newGeExpr(local1, local2);
    val node = nodeFactory.createNode(geExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testGtExprToNodeConversion() {
    val local1 = new Local("a", aType);
    val local2 = new Local("b", aType);
    val gtExpr = JavaJimple.newGtExpr(local1, local2);
    val node = nodeFactory.createNode(gtExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testInstanceOfExprToNodeConversion() {
    val local = new Local("a", aType);
    val instanceOfExpr = JavaJimple.newInstanceOfExpr(local, bType);
    val node = nodeFactory.createNode(instanceOfExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testLeExprToNodeConversion() {
    val local1 = new Local("a", aType);
    val local2 = new Local("b", aType);
    val leExpr = JavaJimple.newLeExpr(local1, local2);
    val node = nodeFactory.createNode(leExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testLengthExprToNodeConversion() {
    val arrayType = ArrayType.createArrayType(aType, 1);
    val arrayLocal = new Local("array", arrayType);
    val lengthExpr = JavaJimple.newLengthExpr(arrayLocal);
    val node = nodeFactory.createNode(lengthExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testLtExprToNodeConversion() {
    val local1 = new Local("a", aType);
    val local2 = new Local("b", aType);
    val ltExpr = JavaJimple.newLtExpr(local1, local2);
    val node = nodeFactory.createNode(ltExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testMulExprToNodeConversion() {
    val local1 = new Local("a", aType);
    val local2 = new Local("b", aType);
    val mulExpr = JavaJimple.newMulExpr(local1, local2);
    val node = nodeFactory.createNode(mulExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testNeExprToNodeConversion() {
    val local1 = new Local("a", aType);
    val local2 = new Local("b", aType);
    val neExpr = JavaJimple.newNeExpr(local1, local2);
    val node = nodeFactory.createNode(neExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testNegExprToNodeConversion() {
    val local = new Local("a", aType);
    val negExpr = JavaJimple.newNegExpr(local);
    val node = nodeFactory.createNode(negExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testOrExprToNodeConversion() {
    val local1 = new Local("a", aType);
    val local2 = new Local("b", aType);
    val orExpr = JavaJimple.newOrExpr(local1, local2);
    val node = nodeFactory.createNode(orExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testRemExprToNodeConversion() {
    val local1 = new Local("a", aType);
    val local2 = new Local("b", aType);
    val remExpr = JavaJimple.newRemExpr(local1, local2);
    val node = nodeFactory.createNode(remExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testShlExprToNodeConversion() {
    val local1 = new Local("a", aType);
    val local2 = new Local("b", aType);
    val shlExpr = JavaJimple.newShlExpr(local1, local2);
    val node = nodeFactory.createNode(shlExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testShrExprToNodeConversion() {
    val local1 = new Local("a", aType);
    val local2 = new Local("b", aType);
    val shrExpr = JavaJimple.newShrExpr(local1, local2);
    val node = nodeFactory.createNode(shrExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testSubExprToNodeConversion() {
    val local1 = new Local("a", aType);
    val local2 = new Local("b", aType);
    val subExpr = JavaJimple.newSubExpr(local1, local2);
    val node = nodeFactory.createNode(subExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testUshrExprToNodeConversion() {
    val local1 = new Local("a", aType);
    val local2 = new Local("b", aType);
    val ushrExpr = JavaJimple.newUshrExpr(local1, local2);
    val node = nodeFactory.createNode(ushrExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testXorExprToNodeConversion() {
    val local1 = new Local("a", aType);
    val local2 = new Local("b", aType);
    val xorExpr = JavaJimple.newXorExpr(local1, local2);
    val node = nodeFactory.createNode(xorExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testCaughtExceptionRefToNodeConversion() {
    val caughtExceptionRef = JavaJimple.newCaughtExceptionRef();
    val node = nodeFactory.createNode(caughtExceptionRef, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testEnumConstantToNodeConversion() {
    val enumConstant = JavaJimple.newEnumConstant("VALUE", "MyEnum");
    val node = nodeFactory.createNode(enumConstant, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testMethodHandleToNodeConversion() {
    val methodHandle = JavaJimple.newMethodHandle(methodSig, MethodHandle.Kind.REF_INVOKE_VIRTUAL);
    val node = nodeFactory.createNode(methodHandle, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testMethodTypeToNodeConversion() {
    val methodType = JavaJimple.newMethodType(Collections.emptyList(), VoidType.getInstance());
    val node = nodeFactory.createNode(methodType, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testPhiExprToNodeConversion() {
    val local1 = new Local("a", aType);
    val local2 = new Local("b", aType);
    List<Local> locals = new java.util.ArrayList<>();
    locals.add(local1);
    locals.add(local2);
    java.util.Map<Local, sootup.core.graph.BasicBlock<?>> blockMap = new java.util.HashMap<>();
    val phiExpr = new JPhiExpr(locals, blockMap);
    val node = nodeFactory.createNode(phiExpr, methodSig);
    assertTrue(node.isEmpty());
  }

  @Test
  void testStringConstantToNodeConversion() {
    val stringConstant = JavaJimple.newStringConstant("hello");
    val nodeOpt = nodeFactory.createNode(stringConstant, methodSig);
    assertTrue(nodeOpt.isPresent());
    val node = nodeOpt.get();
    assertTrue(node instanceof AllocationNode);
    assertEquals(stringConstant.getType(), node.getType());
  }

  @Test
  void testGetResult() {
    val local = new Local("a", aType);
    val varNodeOpt = nodeFactory.createNode(local, methodSig);
    assertTrue(varNodeOpt.isPresent());
  }
}
