package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphConfig;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.implicit.ImplicitCall;
import sootup.callgraph.implicit.ImplicitCallModel;
import sootup.callgraph.implicit.ImplicitCallTarget;
import sootup.callgraph.invokedynamic.DynamicInvokeResolver;
import sootup.callgraph.reflection.ReflectionModel;
import sootup.callgraph.scope.CallFilter;
import sootup.callgraph.scope.CallResolver;
import sootup.callgraph.scope.VirtualCallResolver;
import sootup.core.jimple.common.Local;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
import sootup.spark.Engine;
import sootup.spark.Spark;
import sootup.spark.SparkCallGraphConfig;
import sootup.spark.SparkOptions;

/** Spark follows the implicit calls of the configured model, with and without OTF. */
public class ImplicitCallTest {

  private final ClassType mainType = SparkTestUtil.idFactory.getClassType("ImplicitCall");
  private final ClassType jobType = SparkTestUtil.idFactory.getClassType("ImplicitCall$Job");
  private final ClassType libRunner =
      SparkTestUtil.idFactory.getClassType("ImplicitCall$LibRunner");
  private final MethodSignature mainSig =
      SparkTestUtil.idFactory.getMethodSignature(
          mainType, SparkTestUtil.idFactory.getMainSubSignature());
  private final MethodSignature submit = jobCall("ImplicitCall$Scheduler", "submit");
  private final MethodSignature runnerRun = jobCall("ImplicitCall$Runner", "run");
  private final MethodSignature passedWork = work("ImplicitCall$Passed");
  private final MethodSignature notPassedWork = work("ImplicitCall$NotPassed");
  private final MethodSignature pickedWork = work("ImplicitCall$Picked");

  /** {@code Scheduler.submit(Job)} and {@code Runner.run(Job)} call {@code work()} on the job */
  private final ImplicitCallModel callsWork =
      ImplicitCallModel.of(List.of(callsWork(submit), callsWork(runnerRun)));

  /** Does not follow the calls of {@code LibRunner} */
  private final CallResolver skipsLibRunner =
      method ->
          method.getDeclaringClassType().equals(libRunner) ? CallFilter.none() : CallFilter.all();

  private static ImplicitCall callsWork(MethodSignature source) {
    return new ImplicitCall(
        source,
        false,
        List.of(
            new ImplicitCallTarget.Direct(
                0,
                SparkTestUtil.idFactory.getMethodSubSignature(
                    "work", VoidType.getInstance(), List.of()))));
  }

  private static MethodSignature jobCall(String className, String name) {
    return SparkTestUtil.idFactory.getMethodSignature(
        SparkTestUtil.idFactory.getClassType(className),
        name,
        VoidType.getInstance(),
        List.of(SparkTestUtil.idFactory.getClassType("ImplicitCall$Job")));
  }

  private static MethodSignature work(String className) {
    return SparkTestUtil.idFactory.getMethodSignature(
        SparkTestUtil.idFactory.getClassType(className), "work", VoidType.getInstance(), List.of());
  }

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
  }

  private CallGraph sparkCallGraph(ImplicitCallModel model, boolean onFly) {
    return CallGraphConfig.builder()
        .view(SparkTestUtil.view)
        .entryPoints(List.of(mainSig))
        .implicitCallModel(model)
        .into(SparkCallGraphConfig::from)
        .sparkOptions(SparkOptions.builder().onFlyCallGraph(onFly).build())
        .build()
        .computeCallGraph();
  }

  private Spark spark(ImplicitCallModel model, boolean onFly, CallResolver callResolver) {
    return Spark.builder()
        .view(SparkTestUtil.view)
        .entryPoints(List.of(mainSig))
        .sparkOptions(SparkOptions.builder().onFlyCallGraph(onFly).build())
        .implicitCallModel(model)
        .callResolver(callResolver)
        .build();
  }

  private Set<?> thisTypes(Spark spark, MethodSignature method) {
    Local thisLocal = SparkTestUtil.view.getMethod(method).orElseThrow().getBody().getThisLocal();
    return spark.getPointsToAnalysis().reachingTypes(thisLocal, method);
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  public void implicitCallKeepsOnlyTheTargetsOfThePointedToObjects(boolean onFly) {
    CallGraph cg = sparkCallGraph(callsWork, onFly);
    assertTrue(cg.callTargetsFrom(mainSig).contains(submit));
    assertTrue(cg.callTargetsFrom(mainSig).contains(passedWork), "attributed to the submit call");
    assertFalse(cg.containsMethod(notPassedWork));
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  public void implicitReceiverFlowsIntoTheCallee(boolean onFly) {
    assertEquals(
        Set.of(SparkTestUtil.idFactory.getClassType("ImplicitCall$Passed")),
        thisTypes(spark(callsWork, onFly, null), passedWork));
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  public void noImplicitCallsWithoutTheModel(boolean onFly) {
    CallGraph cg = sparkCallGraph(ImplicitCallModel.none(), onFly);
    assertTrue(cg.callTargetsFrom(mainSig).contains(submit));
    assertFalse(cg.containsMethod(passedWork));
  }

  /** The rule fires where the call dispatches only to methods that the analysis follows */
  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  public void ruleDoesNotFireForAFollowedImplementation(boolean onFly) {
    assertFalse(sparkCallGraph(callsWork, onFly).containsMethod(pickedWork));
  }

  /** As in the CHA graph, the rule fires where the call reaches a method the resolver excludes */
  @Test
  public void ruleFiresForAnImplementationTheCallResolverExcludes() {
    Spark spark = spark(callsWork, false, skipsLibRunner);
    assertTrue(spark.getCallGraph().callTargetsFrom(mainSig).contains(pickedWork));
    // without OTF, a receiver reaches every target of the call graph at its call
    assertTrue(
        thisTypes(spark, pickedWork)
            .contains(SparkTestUtil.idFactory.getClassType("ImplicitCall$Picked")));
  }

  /** A pre-built graph brings its implicit calls: Spark needs no model to follow them precisely */
  @Test
  public void followsTheImplicitCallsOfAPreBuiltGraph() {
    CallGraph cha =
        new ClassHierarchyAnalysisAlgorithm(
                SparkTestUtil.view,
                new sootup.callgraph.scope.DefaultCallResolver(SparkTestUtil.view),
                VirtualCallResolver.all(),
                true,
                ReflectionModel.none(),
                DynamicInvokeResolver.bootstrapMethodHandles(),
                callsWork)
            .initialize(List.of(mainSig));
    assertTrue(cha.callTargetsFrom(mainSig).contains(notPassedWork), "CHA calls every Job");
    Spark spark =
        Spark.builder()
            .view(SparkTestUtil.view)
            .callGraph(cha)
            .implicitCallModel(ImplicitCallModel.none())
            .build();
    CallGraph cg = spark.getCallGraph();
    assertTrue(cg.callTargetsFrom(mainSig).contains(passedWork));
    assertFalse(cg.containsMethod(notPassedWork));
    assertTrue(
        cg.callsFrom(mainSig).stream()
            .filter(call -> call.targetMethodSignature().equals(passedWork))
            .allMatch(CallGraph.Call::isImplicit));
    assertEquals(
        Set.of(SparkTestUtil.idFactory.getClassType("ImplicitCall$Passed")),
        thisTypes(spark, passedWork));
  }
}
