package sootup.spark.test.options;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphConfig;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.spark.SparkCallGraphConfig;
import sootup.spark.test.SparkTestUtil;

/**
 * End-to-end test of {@code sootup.callgraph.CallGraphConfig.builder()....into(SparkCallGraphConfig::from)}
 * - the Spark-specific stage of the unified staged {@link CallGraphConfig} builder chain.
 */
public class CallGraphConfigTest {

  ClassType mainClass = SparkTestUtil.idFactory.getClassType("InstanceMethodCall");
  MethodSignature mainSig =
      SparkTestUtil.idFactory.getMethodSignature(
          mainClass, SparkTestUtil.idFactory.getMainSubSignature());
  ClassType containerType = SparkTestUtil.idFactory.getClassType("InstanceMethodCall$Container");
  ClassType valueType = SparkTestUtil.idFactory.getClassType("InstanceMethodCall$Value");
  MethodSignature getValueSig =
      SparkTestUtil.idFactory.getMethodSignature(
          containerType, "getValue", valueType, List.of(valueType));

  @Test
  public void testIntoSparkStageBuildsCallGraph() {
    CallGraph cg =
        CallGraphConfig.builder()
            .view(SparkTestUtil.view)
            .entryPoints(Collections.singletonList(mainSig))
            .into(SparkCallGraphConfig::from)
            .build()
            .computeCallGraph();

    assertTrue(cg.containsMethod(mainSig));
    assertTrue(
        cg.containsMethod(getValueSig),
        "the resolved virtual callee must be present, same as Spark's own CHA path");
  }
}
