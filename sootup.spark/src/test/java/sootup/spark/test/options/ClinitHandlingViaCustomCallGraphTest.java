package sootup.spark.test.options;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.scope.CallResolver;
import sootup.callgraph.scope.SuppressClinitCallResolver;
import sootup.callgraph.scope.VirtualCallResolver;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.spark.Spark;
import sootup.spark.test.SparkTestUtil;

/**
 * Spark has no {@code <clinit>}-handling knob of its own, but its CHA-delegating (non-OTF) path
 * already accepts any client-supplied {@link CallGraph} via {@code Spark.builder().callGraph(...)}
 * (see {@link CustomCallGraphTest}). That escape hatch is enough to drive Spark with any of the
 * four {@code <clinit>} handling modes {@code ClassHierarchyAnalysisAlgorithm} now supports (see
 * {@code AbstractCallGraphAlgorithm}'s four-arg constructor): pre-build the CHA graph with the
 * desired {@code seedEntryPointClinits}/{@link VirtualCallResolver} combination and hand it to
 * Spark instead of letting Spark build its own default CHA graph.
 */
public class ClinitHandlingViaCustomCallGraphTest {

  MethodSignature mainSig =
      SparkTestUtil.idFactory.getMethodSignature(
          SparkTestUtil.idFactory.getClassType("ClinitTrigger"),
          SparkTestUtil.idFactory.getMainSubSignature());
  ClassType triggeredType = SparkTestUtil.idFactory.getClassType("ClinitTrigger$Triggered");

  @Test
  public void onTheFlyLikeModeStillModelsTheDiscoveredClinit() {
    MethodSignature triggeredClinit =
        SparkTestUtil.idFactory.getStaticInitializerSignature(triggeredType);

    CallGraph cg =
        new ClassHierarchyAnalysisAlgorithm(SparkTestUtil.view)
            .initialize(Collections.singletonList(mainSig));
    Spark spark = Spark.builder().view(SparkTestUtil.view).callGraph(cg).build();

    assertTrue(
        spark.getCallGraph().containsMethod(triggeredClinit),
        "default CHA-built graph must still model the <clinit> triggered by the static field read");
  }

  @Test
  public void suppressedModeDropsTheDiscoveredClinitBeforeReachingSpark() {
    MethodSignature triggeredClinit =
        SparkTestUtil.idFactory.getStaticInitializerSignature(triggeredType);

    CallGraph cg =
        new ClassHierarchyAnalysisAlgorithm(
                SparkTestUtil.view,
                CallResolver.all(),
                new SuppressClinitCallResolver(SparkTestUtil.view),
                /* seedEntryPointClinits= */ false)
            .initialize(Collections.singletonList(mainSig));
    Spark spark = Spark.builder().view(SparkTestUtil.view).callGraph(cg).build();

    assertFalse(
        spark.getCallGraph().containsMethod(triggeredClinit),
        "a client-supplied graph built with NONE-mode clinit handling must not have the <clinit>,"
            + " and Spark must not add it back");
  }
}
