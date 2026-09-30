package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphConfig;
import sootup.callgraph.reflection.ReflectionModel;
import sootup.callgraph.reflection.TamiflexReflectionModel;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.Type;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;
import sootup.spark.Spark;
import sootup.spark.SparkCallGraphConfig;
import sootup.spark.SparkOptions;

/**
 * Spark (CHA-based and on-the-fly) with a TamiFlex reflection model; log recorded with the TamiFlex
 * Play-Out agent, see resources/reflection/README.md. No JDK on the class path: OTF has no call
 * resolver and would otherwise crawl the runtime.
 */
public class ReflectionSparkTest {

  private static final String DIR = "src/test/resources/reflection/";

  private final JavaView view =
      new JavaView(new JavaClassPathAnalysisInputLocation(DIR + "binary"));

  private MethodSignature sig(String cls, String name, String ret, String... params) {
    return view.getIdentifierFactory()
        .getMethodSignature("tfx." + cls, name, ret, Arrays.asList(params));
  }

  private MethodSignature main(String cls) {
    return sig(cls, "main", "void", "java.lang.String[]");
  }

  private Spark spark(MethodSignature entry, boolean otf, ReflectionModel model) {
    return Spark.builder()
        .view(view)
        .entryPoints(List.of(entry))
        .sparkOptions(SparkOptions.builder().onFlyCallGraph(otf).build())
        .reflectionModel(model)
        .build();
  }

  /**
   * Types the result of the reflective {@code newInstance()} call in {@code entry} may point to.
   */
  private Set<String> newInstanceResultTypes(Spark spark, MethodSignature entry) {
    Local result =
        (Local)
            view.getMethod(entry).get().getBody().getStmts().stream()
                .filter(s -> s instanceof JAssignStmt a && a.getInvokeExpr().isPresent())
                .map(s -> (JAssignStmt) s)
                .filter(
                    a ->
                        a.getInvokeExpr()
                            .get()
                            .getMethodSignature()
                            .getName()
                            .equals("newInstance"))
                .findFirst()
                .get()
                .getLeftOp();
    return spark.getPointsToAnalysis().reachingTypes(result, entry).stream()
        .map(Type::toString)
        .collect(Collectors.toSet());
  }

  @Test
  public void classNewInstanceAllocatesLoggedClass() {
    MethodSignature entry = main("LegacyNewInstance");
    ReflectionModel model = new TamiflexReflectionModel(view, DIR + "refl.log");
    for (boolean otf : new boolean[] {false, true}) {
      assertTrue(
          newInstanceResultTypes(spark(entry, otf, ReflectionModel.none()), entry).isEmpty());
      assertEquals(Set.of("tfx.PluginA"), newInstanceResultTypes(spark(entry, otf, model), entry));
    }
  }

  @Test
  public void otfConstructsBothLoggedPlugins() {
    MethodSignature load = sig("PluginLoader", "load", "tfx.Plugin", "java.lang.String");
    ReflectionModel model = new TamiflexReflectionModel(view, DIR + "refl.log");
    List<MethodSignature> inits =
        List.of(sig("PluginA", "<init>", "void"), sig("PluginB", "<init>", "void"));

    CallGraph without = spark(main("PluginLoader"), true, ReflectionModel.none()).getCallGraph();
    assertTrue(inits.stream().noneMatch(without::containsMethod));

    CallGraph with = spark(main("PluginLoader"), true, model).getCallGraph();
    assertTrue(with.callTargetsFrom(load).containsAll(inits));
  }

  @Test
  public void otfDispatchesReflectiveInvokeOnReceiverPointsTo() {
    // synthetic virtualinvoke on the reflective receiver - OTF dispatches on its alloc type (Sub)
    MethodSignature entry = main("InvokeOnSubclass");
    MethodSignature subFoo = sig("Sub", "foo", "java.lang.Object", "java.lang.Object");
    ReflectionModel model = new TamiflexReflectionModel(view, DIR + "refl.log");
    assertTrue(!spark(entry, true, ReflectionModel.none()).getCallGraph().containsMethod(subFoo));
    assertTrue(spark(entry, true, model).getCallGraph().callTargetsFrom(entry).contains(subFoo));
  }

  @Test
  public void unifiedConfigPassesModelToChaAndPag() {
    MethodSignature entry = main("InvokeOnSubclass");
    MethodSignature subFoo = sig("Sub", "foo", "java.lang.Object", "java.lang.Object");
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(entry))
            .reflectionModel(new TamiflexReflectionModel(view, DIR + "refl.log"))
            .into(SparkCallGraphConfig::from)
            .build()
            .computeCallGraph();
    assertTrue(cg.callTargetsFrom(entry).contains(subFoo));
  }

  @Test
  public void reflectiveAllocationFlowsThroughCast() {
    // p = (Plugin) Class.forName(..).newInstance(); p.start() - needs cast propagation
    MethodSignature entry = main("PluginLoader");
    ReflectionModel model = new TamiflexReflectionModel(view, DIR + "refl.log");
    CallGraph cg = spark(entry, true, model).getCallGraph();
    assertTrue(
        cg.callTargetsFrom(entry)
            .containsAll(
                List.of(sig("PluginA", "start", "void"), sig("PluginB", "start", "void"))));
  }
}
