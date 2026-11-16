package sootup.examples.callgraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphConfig;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.invokedynamic.DynamicInvokeResolver;
import sootup.callgraph.reflection.ReflectionModel;
import sootup.callgraph.scope.DefaultCallResolver;
import sootup.callgraph.scope.VirtualCallResolver;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.Type;
import sootup.java.core.views.JavaView;
import sootup.spark.PointsToAnalysis;
import sootup.spark.Spark;
import sootup.spark.SparkCallGraphConfig;
import sootup.spark.SparkOptions;

/** Spark examples, included in {@code docs/callgraph-spark.md}. */
public class SparkCallGraphTest {

  final JavaView view =
      CallGraphOptionsTest.viewOf("src/test/resources/CallgraphComparison/binary");
  final MethodSignature mainMethod =
      CallGraphOptionsTest.sig(view, "<Main: void main(java.lang.String[])>");

  Set<String> areaTargets(CallGraph cg, int line) {
    return cg.callsFrom(mainMethod).stream()
        .filter(call -> call.getLineNumber() == line)
        .map(call -> call.targetMethodSignature())
        .filter(target -> target.getName().equals("area"))
        .map(target -> target.getDeclClassType().getClassName())
        .collect(Collectors.toSet());
  }

  @Test
  public void allOptions() {
    // --8<-- [start:options]
    SparkOptions options =
        SparkOptions.builder()
            .onFlyCallGraph(true) // grow the call graph with the points-to sets (default: false)
            .ignoreBaseObjects(false) // true: field-based instead of field-sensitive
            .typesForSites(false) // true: one abstract object per type, not per `new`
            .simpleEdgesBidirectional(false) // true: unification-based (Steensgaard-like)
            // accepted, but not implemented yet:
            .parmsAsFields(false)
            .returnsAsFields(false)
            .mergeStringBuffer(true)
            .simulateNatives(true)
            .build();
    // --8<-- [end:options]
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(mainMethod))
            .into(SparkCallGraphConfig::from)
            .sparkOptions(options)
            .build()
            .computeCallGraph();
    assertEquals(
        Set.of("Square", "Triangle"), areaTargets(cg, CallGraphComparisonTest.CALL_SITE_2));
  }

  @Test
  public void defaultsAreChaSeeded() {
    // --8<-- [start:cha-seeded]
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(mainMethod))
            .into(SparkCallGraphConfig::from)
            .sparkOptions(SparkOptions.defaultOptions()) // onFlyCallGraph = false
            .build()
            .computeCallGraph(); // = the CHA graph Spark was seeded with
    // --8<-- [end:cha-seeded]
    assertEquals(
        Set.of("Circle", "Square", "Triangle", "Hexagon"),
        areaTargets(cg, CallGraphComparisonTest.CALL_SITE_2));
  }

  @Test
  public void typesForSites() {
    // --8<-- [start:types-for-sites]
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(mainMethod))
            .into(SparkCallGraphConfig::from)
            .sparkOptions(SparkOptions.builder().onFlyCallGraph(true).typesForSites(true).build())
            .build()
            .computeCallGraph();
    // --8<-- [end:types-for-sites]
    assertEquals(
        Set.of("Square", "Triangle"), areaTargets(cg, CallGraphComparisonTest.CALL_SITE_2));
  }

  @Test
  public void facadeWithPointsTo() {
    // --8<-- [start:facade]
    Spark spark =
        Spark.builder()
            .view(view)
            .entryPoints(List.of(mainMethod))
            .sparkOptions(SparkOptions.builder().onFlyCallGraph(true).build())
            .reflectionModel(ReflectionModel.none()) // default
            .dynamicInvokeResolver(DynamicInvokeResolver.bootstrapMethodHandles()) // default
            .build();
    CallGraph cg = spark.getCallGraph(); // solves on first access
    PointsToAnalysis pta = spark.getPointsToAnalysis();
    // --8<-- [end:facade]

    // --8<-- [start:points-to]
    // the receiver of `square.area()` in main
    Local square =
        view.getMethod(mainMethod).get().getBody().getStmts().stream()
            .filter(stmt -> stmt instanceof InvokableStmt)
            .filter(stmt -> stmt.getPositionInfo().getStmtPosition().getFirstLine() == 13)
            .map(stmt -> ((InvokableStmt) stmt).getInvokeExpr().get())
            .map(invoke -> ((AbstractInstanceInvokeExpr) invoke).getBase())
            .findFirst()
            .get();

    Set<Type> types = pta.reachingTypes(square, mainMethod); // {Square, Triangle}
    // --8<-- [end:points-to]
    assertEquals(
        Set.of(
            view.getIdentifierFactory().getClassType("Square"),
            view.getIdentifierFactory().getClassType("Triangle")),
        types);
    assertTrue(cg.containsMethod(mainMethod));
  }

  @Test
  public void facadeWithOwnCallGraph() {
    // --8<-- [start:facade-own-cg]
    ReflectionModel reflection = ReflectionModel.none();
    DynamicInvokeResolver indy = DynamicInvokeResolver.bootstrapMethodHandles();
    // e.g. a CHA graph with custom <clinit>/scoping settings
    CallGraph cha =
        new ClassHierarchyAnalysisAlgorithm(
                view,
                new DefaultCallResolver(view),
                VirtualCallResolver.all(),
                true, // seedEntryPointClinits
                reflection,
                indy)
            .initialize(List.of(mainMethod));

    Spark spark =
        Spark.builder()
            .view(view)
            .callGraph(cha) // used as-is; ignored when onFlyCallGraph is true
            .reflectionModel(reflection) // use the same instances the graph was built with
            .dynamicInvokeResolver(indy)
            .build();
    PointsToAnalysis pta = spark.getPointsToAnalysis();
    // --8<-- [end:facade-own-cg]
    assertTrue(!pta.getPointsToMap().isEmpty());
  }
}
