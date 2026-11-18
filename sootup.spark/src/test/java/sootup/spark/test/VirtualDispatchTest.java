package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.spark.Spark;
import sootup.spark.SparkOptions;

public class VirtualDispatchTest {

  @Test
  public void privateMethodWithTheSameSignatureIsNotATargetOnTheFly() {
    ClassType mainClass = SparkTestUtil.idFactory.getClassType("PrivateShadow");
    MethodSignature mainSig =
        SparkTestUtil.idFactory.getMethodSignature(
            mainClass, SparkTestUtil.idFactory.getMainSubSignature());
    Spark spark =
        Spark.builder()
            .view(SparkTestUtil.view)
            .entryPoints(Collections.singletonList(mainSig))
            .sparkOptions(SparkOptions.builder().onFlyCallGraph(true).build())
            .build();
    spark.solve();
    CallGraph cg = spark.getCallGraph();

    MethodSignature defaultMethod =
        SparkTestUtil.idFactory.parseMethodSignature(
            "<PrivateShadow$Greeter: java.lang.Object greet()>");
    MethodSignature privateMethod =
        SparkTestUtil.idFactory.parseMethodSignature(
            "<PrivateShadow$Base: java.lang.Object greet()>");
    assertTrue(cg.callTargetsFrom(mainSig).contains(defaultMethod));
    assertFalse(cg.callTargetsFrom(mainSig).contains(privateMethod));
  }
}
