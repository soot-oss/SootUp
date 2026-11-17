package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.Jimple;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.spark.Engine;
import sootup.spark.PointsToAnalysis;
import sootup.spark.node.AllocationNode;

/** Queries for the objects that a field of given objects may point to */
public class FieldPointsToTest {

  private final ClassType classSig = SparkTestUtil.idFactory.getClassType("Basic");
  private final MethodSignature mainSig =
      SparkTestUtil.idFactory.getMethodSignature(
          classSig, SparkTestUtil.idFactory.getMainSubSignature());
  private final ClassType fieldType = SparkTestUtil.idFactory.getClassType("Basic$Field");
  private final ClassType containerType = SparkTestUtil.idFactory.getClassType("Basic$Container");
  private final FieldSignature field =
      SparkTestUtil.idFactory.getFieldSignature("field", containerType, fieldType);

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
  }

  @Test
  public void fieldOfObjectsPointsToStoredObjects() {
    PointsToAnalysis pta = SparkTestUtil.solveMainWithSpark(mainSig).getPointsToAnalysis();
    AllocationNode newField = SparkTestUtil.alloc(fieldType, 1L, mainSig);
    AllocationNode newContainer = SparkTestUtil.alloc(containerType, 2L, mainSig);

    // c1.field = f2: the field of the container object points to the field object
    Set<AllocationNode> containers =
        pta.reachingObjects(Jimple.newLocal("l4", containerType), mainSig);
    assertEquals(Set.of(newContainer), containers);
    assertEquals(Set.of(newField), pta.reachingObjects(containers, field));
  }

  @Test
  public void fieldOfOtherObjectsIsEmpty() {
    PointsToAnalysis pta = SparkTestUtil.solveMainWithSpark(mainSig).getPointsToAnalysis();
    AllocationNode newField = SparkTestUtil.alloc(fieldType, 1L, mainSig);
    // the field object has no field "field"
    assertTrue(pta.reachingObjects(Set.of(newField), field).isEmpty());
    assertTrue(pta.reachingObjects(Collections.emptySet(), field).isEmpty());
  }
}
