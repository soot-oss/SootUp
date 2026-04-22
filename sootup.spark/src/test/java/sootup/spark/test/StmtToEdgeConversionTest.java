package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.LValue;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.spark.Engine;
import sootup.spark.MethodPAGStmtVisitor;
import sootup.spark.PAG;
import sootup.spark.PAGEdge;
import sootup.spark.node.AllocationNode;
import sootup.spark.node.InstanceFieldRefNode;
import sootup.spark.node.VariableNode;

public class StmtToEdgeConversionTest {

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
  public void testAllocEdge() {
    LValue left = new Local("a", aType);
    Value right = new JNewExpr(SparkTestUtil.simpleType("A"));
    val methodPAG = new PAG();
    val edge = doAssignment(right, left, methodPAG, PAGEdge.EdgeType.ALLOCATION);

    val source = methodPAG.getDelegate().getEdgeSource(edge);
    val target = methodPAG.getDelegate().getEdgeTarget(edge);
    val expectedTarget =
        VariableNode.builder().type(aType).name("a").containingMethodSig(methodSig).build();
    val expectedSource =
        AllocationNode.builder()
            .type(aType)
            .allocationSite(1L)
            .containingMethodSig(methodSig)
            .build();
    assertEquals(expectedTarget, target);
    assertEquals(expectedSource, source);
  }

  @Test
  public void testAssignEdge() {
    LValue left = new Local("b", aType);
    Value right = new Local("a", aType);
    val methodPAG = new PAG();
    val edge = doAssignment(right, left, methodPAG, PAGEdge.EdgeType.ASSIGNMENT);

    val source = methodPAG.getDelegate().getEdgeSource(edge);
    val target = methodPAG.getDelegate().getEdgeTarget(edge);
    val expectedTarget =
        VariableNode.builder().type(aType).name("b").containingMethodSig(methodSig).build();
    val expectedSource =
        VariableNode.builder().type(aType).name("a").containingMethodSig(methodSig).build();
    assertEquals(expectedTarget, target);
    assertEquals(expectedSource, source);
  }

  @Test
  public void testStoreEdge() {
    val aType = SparkTestUtil.simpleType("A");
    val base = new Local("someB", aType);
    val right = new JInstanceFieldRef(base, fieldSig);
    LValue left = new Local("b", aType);
    val methodPAG = new PAG();
    val edge = doAssignment(right, left, methodPAG, PAGEdge.EdgeType.LOAD);

    val source = methodPAG.getDelegate().getEdgeSource(edge);
    val target = methodPAG.getDelegate().getEdgeTarget(edge);
    val expectedTarget =
        VariableNode.builder().type(aType).name("b").containingMethodSig(methodSig).build();
    val baseNode =
        VariableNode.builder().type(aType).name("someB").containingMethodSig(methodSig).build();
    val expectedSource =
        InstanceFieldRefNode.builder()
            .type(fieldSig.getType())
            .base(baseNode)
            .field(fieldSig)
            .containingMethodSig(methodSig)
            .build();
    assertEquals(expectedTarget, target);
    assertEquals(expectedSource, source);
  }

  @Test
  public void testLoadEdge() {
    val aType = SparkTestUtil.simpleType("A");
    val base = new Local("someB", aType);
    val left = new JInstanceFieldRef(base, fieldSig);
    LValue right = new Local("b", aType);
    val methodPAG = new PAG();
    val edge = doAssignment(right, left, methodPAG, PAGEdge.EdgeType.STORE);

    val source = methodPAG.getDelegate().getEdgeSource(edge);
    val target = methodPAG.getDelegate().getEdgeTarget(edge);
    val baseNode =
        VariableNode.builder().type(aType).name("someB").containingMethodSig(methodSig).build();
    val expectedTarget =
        InstanceFieldRefNode.builder()
            .type(fieldSig.getType())
            .base(baseNode)
            .field(fieldSig)
            .containingMethodSig(methodSig)
            .build();
    val expectedSource =
        VariableNode.builder().type(aType).name("b").containingMethodSig(methodSig).build();
    assertEquals(expectedTarget, target);
    assertEquals(expectedSource, source);
  }

  private PAGEdge doAssignment(Value right, LValue left, PAG methodPAG, PAGEdge.EdgeType edgeType) {
    JAssignStmt assignStmt = new JAssignStmt(left, right, StmtPositionInfo.getNoStmtPositionInfo());
    MethodPAGStmtVisitor stmtVisitor =
        MethodPAGStmtVisitor.builder().PAG(methodPAG).methodSignature(methodSig).build();
    assignStmt.accept(stmtVisitor);
    val edgeOpt =
        methodPAG.getDelegate().edgeSet().stream()
            .filter(PAGEdge.class::isInstance)
            .filter(e -> edgeType.equals(e.getEdgeType()))
            .findFirst();
    assertTrue(edgeOpt.isPresent());
    return edgeOpt.get();
  }
}
