package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.spark.Engine;
import sootup.spark.Spark;
import sootup.spark.SparkOptions;
import sootup.spark.node.AllocationNode;

/**
 * Static and special invocations name the class through which the method is called, which can be a
 * subclass of the class that declares the method. The on-the-fly call graph must call the declared
 * method and pass the arguments to it.
 */
public class InheritedMethodTest {

  private final ClassType mainType = SparkTestUtil.idFactory.getClassType("InheritedMethod");
  private final ClassType superType = SparkTestUtil.idFactory.getClassType("InheritedMethod$Super");
  private final ClassType subType = SparkTestUtil.idFactory.getClassType("InheritedMethod$Sub");
  private final ClassType valueType = SparkTestUtil.idFactory.getClassType("InheritedMethod$Value");
  private final MethodSignature mainSig =
      SparkTestUtil.idFactory.getMethodSignature(
          mainType, SparkTestUtil.idFactory.getMainSubSignature());
  private final MethodSignature declaredId =
      SparkTestUtil.idFactory.getMethodSignature(superType, "id", valueType, List.of(valueType));
  private final MethodSignature declaredGet =
      SparkTestUtil.idFactory.getMethodSignature(superType, "get", valueType, List.of(valueType));
  private final MethodSignature viaSuper =
      SparkTestUtil.idFactory.getMethodSignature(
          subType, "viaSuper", valueType, List.of(valueType));

  private Spark spark;

  @BeforeEach
  public void solve() {
    Engine.resetAllocCount();
    spark =
        Spark.builder()
            .view(SparkTestUtil.view)
            .entryPoints(Collections.singletonList(mainSig))
            .sparkOptions(SparkOptions.builder().onFlyCallGraph(true).build())
            .build();
    spark.solve();
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
  public void staticCallThroughSubclassCallsDeclaredMethod() {
    assertTrue(spark.getCallGraph().callTargetsFrom(mainSig).contains(declaredId));

    AllocationNode newValue = SparkTestUtil.alloc(valueType, 1L, mainSig);
    assertEquals(
        Set.of(newValue), spark.getPointsToAnalysis().reachingObjects(resultOf("id"), mainSig));
  }

  @Test
  public void superCallThroughDirectSuperclassCallsDeclaredMethod() {
    assertTrue(spark.getCallGraph().callTargetsFrom(viaSuper).contains(declaredGet));

    AllocationNode newValue = SparkTestUtil.alloc(valueType, 1L, mainSig);
    assertEquals(
        Set.of(newValue),
        spark.getPointsToAnalysis().reachingObjects(resultOf("viaSuper"), mainSig));
  }
}
