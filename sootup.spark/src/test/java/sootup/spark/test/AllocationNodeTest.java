package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.spark.node.AllocationNode;

class AllocationNodeTest {

  ClassType aType = SparkTestUtil.simpleType("A");
  ClassType bType = SparkTestUtil.simpleType("B");
  MethodSignature methodSig =
      JavaIdentifierFactory.getInstance()
          .getMethodSignature(aType, "test", "void", Collections.emptyList());
  MethodSignature otherMethodSig =
      JavaIdentifierFactory.getInstance()
          .getMethodSignature(aType, "other", "void", Collections.emptyList());

  @Test
  void toString_withAllocationSite() {
    var node =
        AllocationNode.builder()
            .type(aType)
            .allocationSite(1L)
            .containingMethodSig(methodSig)
            .build();
    assertEquals("\"test{1:new A}\"", node.toString());
  }

  @Test
  void toString_withoutAllocationSite() {
    var node = AllocationNode.builder().type(aType).containingMethodSig(methodSig).build();
    assertEquals("\"test{A}\"", node.toString());
  }

  @Test
  void equals_sameAllocSite_sameMethod_sameType_isEqual() {
    var n1 =
        AllocationNode.builder()
            .type(aType)
            .allocationSite(1L)
            .containingMethodSig(methodSig)
            .build();
    var n2 =
        AllocationNode.builder()
            .type(aType)
            .allocationSite(1L)
            .containingMethodSig(methodSig)
            .build();
    assertEquals(n1, n2);
  }

  @Test
  void equals_differentAllocSite_isNotEqual() {
    var n1 =
        AllocationNode.builder()
            .type(aType)
            .allocationSite(1L)
            .containingMethodSig(methodSig)
            .build();
    var n2 =
        AllocationNode.builder()
            .type(aType)
            .allocationSite(2L)
            .containingMethodSig(methodSig)
            .build();
    assertNotEquals(n1, n2);
  }

  @Test
  void equals_differentType_sameAllocSite_isNotEqual() {
    var n1 =
        AllocationNode.builder()
            .type(aType)
            .allocationSite(1L)
            .containingMethodSig(methodSig)
            .build();
    var n2 =
        AllocationNode.builder()
            .type(bType)
            .allocationSite(1L)
            .containingMethodSig(methodSig)
            .build();
    assertNotEquals(n1, n2);
  }

  @Test
  void equals_nullAllocSite_sameType_isEqual_regardlessOfMethod() {
    var n1 = AllocationNode.builder().type(aType).containingMethodSig(methodSig).build();
    var n2 = AllocationNode.builder().type(aType).containingMethodSig(otherMethodSig).build();
    assertEquals(n1, n2);
  }

  @Test
  void equals_nullAllocSite_differentType_isNotEqual() {
    var n1 = AllocationNode.builder().type(aType).containingMethodSig(methodSig).build();
    var n2 = AllocationNode.builder().type(bType).containingMethodSig(methodSig).build();
    assertNotEquals(n1, n2);
  }

  @Test
  void equals_oneNullAllocSite_oneNot_isNotEqual() {
    var n1 = AllocationNode.builder().type(aType).containingMethodSig(methodSig).build();
    var n2 =
        AllocationNode.builder()
            .type(aType)
            .allocationSite(1L)
            .containingMethodSig(methodSig)
            .build();
    assertNotEquals(n1, n2);
  }

  @Test
  void hashCode_sameAllocSite_sameType_sameMethod_isEqual() {
    var n1 =
        AllocationNode.builder()
            .type(aType)
            .allocationSite(1L)
            .containingMethodSig(methodSig)
            .build();
    var n2 =
        AllocationNode.builder()
            .type(aType)
            .allocationSite(1L)
            .containingMethodSig(methodSig)
            .build();
    assertEquals(n1.hashCode(), n2.hashCode());
  }

  @Test
  void hashCode_nullAllocSite_sameType_isEqual_regardlessOfMethod() {
    var n1 = AllocationNode.builder().type(aType).containingMethodSig(methodSig).build();
    var n2 = AllocationNode.builder().type(aType).containingMethodSig(otherMethodSig).build();
    assertEquals(n1.hashCode(), n2.hashCode());
  }

  @Test
  void hashCode_nullAllocSite_differentType_isDifferent() {
    var n1 = AllocationNode.builder().type(aType).containingMethodSig(methodSig).build();
    var n2 = AllocationNode.builder().type(bType).containingMethodSig(methodSig).build();
    assertNotEquals(n1.hashCode(), n2.hashCode());
  }
}
