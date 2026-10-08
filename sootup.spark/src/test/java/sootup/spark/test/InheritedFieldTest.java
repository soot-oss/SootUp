package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.spark.Engine;
import sootup.spark.NodeFactory;
import sootup.spark.PointsToAnalysis;
import sootup.spark.SparkOptions;
import sootup.spark.node.AllocationNode;
import sootup.spark.node.InstanceFieldRefNode;
import sootup.spark.node.Node;
import sootup.spark.node.StaticFieldRefNode;

/**
 * Field references name the class through which the field is accessed, which can be a subclass of
 * the class that declares the field. All references to the same field must share one field node.
 */
public class InheritedFieldTest {

  private final ClassType mainType = SparkTestUtil.idFactory.getClassType("InheritedField");
  private final ClassType superType = SparkTestUtil.idFactory.getClassType("InheritedField$Super");
  private final ClassType subType = SparkTestUtil.idFactory.getClassType("InheritedField$Sub");
  private final ClassType valueType = SparkTestUtil.idFactory.getClassType("InheritedField$Value");
  private final MethodSignature mainSig =
      SparkTestUtil.idFactory.getMethodSignature(
          mainType, SparkTestUtil.idFactory.getMainSubSignature());

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
  }

  /** The local that is assigned the result of the call to the given method in main */
  private Local resultOf(String methodName) {
    for (Stmt stmt : SparkTestUtil.view.getMethod(mainSig).get().getBody().getStmts()) {
      if (stmt instanceof JAssignStmt assign
          && assign.getLeftOp() instanceof Local local
          && assign.getInvokeExpr().isPresent()
          && assign.getInvokeExpr().get().getMethodSignature().getName().equals(methodName)) {
        return local;
      }
    }
    throw new AssertionError("no call to " + methodName);
  }

  @Test
  public void loadThroughSuperclassSeesStoreThroughSubclass() {
    PointsToAnalysis pta = SparkTestUtil.solveMainWithSpark(mainSig).getPointsToAnalysis();

    // s.f = v stores into <Sub: f>, read(s) loads <Super: f>: both are the same field
    AllocationNode newValue = SparkTestUtil.alloc(valueType, 2L, mainSig);
    Set<AllocationNode> reachingR = pta.reachingObjects(resultOf("read"), mainSig);
    assertEquals(Set.of(newValue), reachingR);
  }

  @Test
  public void staticLoadThroughSuperclassSeesStoreThroughSubclass() {
    PointsToAnalysis pta = SparkTestUtil.solveMainWithSpark(mainSig).getPointsToAnalysis();

    // Sub.sf = new Value() stores into <Sub: sf>, q = Super.sf loads <Super: sf>
    AllocationNode newValue = SparkTestUtil.alloc(valueType, 3L, mainSig);
    Local q = null;
    for (Stmt stmt : SparkTestUtil.view.getMethod(mainSig).get().getBody().getStmts()) {
      if (stmt instanceof JAssignStmt assign
          && assign.getLeftOp() instanceof Local local
          && assign.getRightOp() instanceof JStaticFieldRef) {
        q = local;
      }
    }
    assertEquals(Set.of(newValue), pta.reachingObjects(q, mainSig));
  }

  @Test
  public void fieldNodesUseDeclaredField() {
    NodeFactory nodeFactory = new NodeFactory(SparkOptions.defaultOptions(), SparkTestUtil.view);
    FieldSignature viaSub = SparkTestUtil.idFactory.getFieldSignature("f", subType, valueType);
    FieldSignature declared = SparkTestUtil.idFactory.getFieldSignature("f", superType, valueType);
    Optional<Node> instanceNode =
        nodeFactory.createNode(
            new JInstanceFieldRef(Jimple.newLocal("s", subType), viaSub), mainSig);
    assertTrue(instanceNode.isPresent());
    assertEquals(declared, ((InstanceFieldRefNode) instanceNode.get()).getField());

    FieldSignature staticViaSub =
        SparkTestUtil.idFactory.getFieldSignature("sf", subType, valueType);
    FieldSignature staticDeclared =
        SparkTestUtil.idFactory.getFieldSignature("sf", superType, valueType);
    Optional<Node> staticNode = nodeFactory.createNode(new JStaticFieldRef(staticViaSub), mainSig);
    assertTrue(staticNode.isPresent());
    assertEquals(staticDeclared, ((StaticFieldRefNode) staticNode.get()).getField());
  }

  @Test
  public void unresolvableFieldKeepsReference() {
    NodeFactory nodeFactory = new NodeFactory(SparkOptions.defaultOptions(), SparkTestUtil.view);
    ClassType missing = SparkTestUtil.idFactory.getClassType("DoesNotExist");
    FieldSignature ref = SparkTestUtil.idFactory.getFieldSignature("g", missing, valueType);
    Optional<Node> node = nodeFactory.createNode(new JStaticFieldRef(ref), mainSig);
    assertTrue(node.isPresent());
    assertEquals(ref, ((StaticFieldRefNode) node.get()).getField());
  }
}
