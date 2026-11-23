package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.PrimitiveType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JrtFileSystemAnalysisInputLocation;
import sootup.java.core.views.JavaView;
import sootup.spark.Spark;
import sootup.spark.SparkOptions;

/** A call on an array runs the method of {@code java.lang.Object}. */
public class ArrayReceiverTest {

  @Test
  public void callOnArrayRunsObjectMethod() {
    JavaView view =
        new JavaView(
            List.of(
                new JavaClassPathAnalysisInputLocation("src/test/resources/pta/binary"),
                new JrtFileSystemAnalysisInputLocation()));
    MethodSignature mainSig =
        SparkTestUtil.idFactory.getMethodSignature(
            SparkTestUtil.idFactory.getClassType("ArrayReceiver"),
            SparkTestUtil.idFactory.getMainSubSignature());
    MethodSignature objectEquals =
        SparkTestUtil.idFactory.getMethodSignature(
            SparkTestUtil.idFactory.getClassType("java.lang.Object"),
            "equals",
            PrimitiveType.getBoolean(),
            List.of(SparkTestUtil.idFactory.getClassType("java.lang.Object")));

    Spark spark =
        Spark.builder()
            .view(view)
            .entryPoints(List.of(mainSig))
            .sparkOptions(SparkOptions.builder().onFlyCallGraph(true).build())
            .build();

    assertTrue(spark.getCallGraph().callTargetsFrom(mainSig).contains(objectEquals));
  }
}
