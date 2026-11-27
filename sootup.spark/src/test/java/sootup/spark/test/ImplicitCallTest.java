package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphConfig;
import sootup.callgraph.implicit.ImplicitCall;
import sootup.callgraph.implicit.ImplicitCallModel;
import sootup.callgraph.implicit.ImplicitCallTarget;
import sootup.core.jimple.common.Local;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
import sootup.spark.Engine;
import sootup.spark.Spark;
import sootup.spark.SparkCallGraphConfig;

/** Spark without an on-the-fly call graph follows the implicit calls of the configured model. */
public class ImplicitCallTest {

  private final ClassType mainType = SparkTestUtil.idFactory.getClassType("ImplicitCall");
  private final ClassType jobType = SparkTestUtil.idFactory.getClassType("ImplicitCall$Job");
  private final MethodSignature mainSig =
      SparkTestUtil.idFactory.getMethodSignature(
          mainType, SparkTestUtil.idFactory.getMainSubSignature());
  private final MethodSignature submit =
      SparkTestUtil.idFactory.getMethodSignature(
          SparkTestUtil.idFactory.getClassType("ImplicitCall$Scheduler"),
          "submit",
          VoidType.getInstance(),
          List.of(jobType));
  private final MethodSignature passedWork = work("ImplicitCall$Passed");
  private final MethodSignature notPassedWork = work("ImplicitCall$NotPassed");

  /** {@code Scheduler.submit(Job)} calls {@code work()} on its argument */
  private final ImplicitCallModel submitCallsWork =
      ImplicitCallModel.of(
          List.of(
              new ImplicitCall(
                  submit,
                  false,
                  List.of(
                      new ImplicitCallTarget.Direct(
                          0,
                          SparkTestUtil.idFactory.getMethodSubSignature(
                              "work", VoidType.getInstance(), List.of()))))));

  private MethodSignature work(String className) {
    return SparkTestUtil.idFactory.getMethodSignature(
        SparkTestUtil.idFactory.getClassType(className), "work", VoidType.getInstance(), List.of());
  }

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
  }

  private CallGraph sparkCallGraph(ImplicitCallModel model) {
    return CallGraphConfig.builder()
        .view(SparkTestUtil.view)
        .entryPoints(List.of(mainSig))
        .implicitCallModel(model)
        .into(SparkCallGraphConfig::from)
        .build()
        .computeCallGraph();
  }

  @Test
  public void implicitCallKeepsOnlyTheTargetsOfThePointedToObjects() {
    CallGraph cg = sparkCallGraph(submitCallsWork);
    assertTrue(cg.callTargetsFrom(mainSig).contains(submit));
    assertTrue(cg.callTargetsFrom(mainSig).contains(passedWork));
    assertFalse(cg.containsMethod(notPassedWork));
  }

  @Test
  public void implicitReceiverFlowsIntoTheCallee() {
    Spark spark =
        Spark.builder()
            .view(SparkTestUtil.view)
            .entryPoints(List.of(mainSig))
            .implicitCallModel(submitCallsWork)
            .build();
    Local thisLocal =
        SparkTestUtil.view.getMethod(passedWork).orElseThrow().getBody().getThisLocal();
    assertEquals(
        Set.of(SparkTestUtil.idFactory.getClassType("ImplicitCall$Passed")),
        spark.getPointsToAnalysis().reachingTypes(thisLocal, passedWork));
  }

  @Test
  public void noImplicitCallsWithoutTheModel() {
    CallGraph cg = sparkCallGraph(ImplicitCallModel.none());
    assertTrue(cg.callTargetsFrom(mainSig).contains(submit));
    assertFalse(cg.containsMethod(passedWork));
  }
}
