package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import sootup.spark.node.SyntheticClassType;
import sootup.spark.node.ValueToNodeConversionVisitor;

public class SyntheticClassTypeTest {

  /** A class of the analyzed program with the same name is a different class. */
  @Test
  public void neverEqualsAProgramClass() {
    assertNotEquals(SyntheticClassType.GLOBAL, SparkTestUtil.idFactory.getClassType("GLOBAL"));
    assertNotEquals(SparkTestUtil.idFactory.getClassType("GLOBAL"), SyntheticClassType.GLOBAL);
    assertNotEquals(
        SparkTestUtil.idFactory.getMethodSignature(
            SparkTestUtil.idFactory.getClassType("GLOBAL"),
            SparkTestUtil.GLOBAL_SCOPE.getSubSignature()),
        ValueToNodeConversionVisitor.globalScope(SparkTestUtil.idFactory));
  }
}
