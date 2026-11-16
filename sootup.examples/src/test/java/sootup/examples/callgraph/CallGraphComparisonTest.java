package sootup.examples.callgraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qilin.callgraph.QilinCallGraphConfig;
import qilin.core.config.ContextSensitivity;
import qilin.core.config.PointerAnalysisConfig;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphConfig;
import sootup.callgraph.CallGraphDifference;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;
import sootup.spark.SparkCallGraphConfig;
import sootup.spark.SparkOptions;

/**
 * Builds the call graph of {@code CallgraphComparison/source/Main.java} with CHA, RTA, Spark and
 * Qilin and shows how the targets of its two {@code area()} call sites differ. Included in {@code
 * docs/callgraphs.md}.
 */
public class CallGraphComparisonTest {

  /** Line of {@code circle.area()} in {@code Main.java}. */
  static final int CALL_SITE_1 = 9;

  /** Line of {@code square.area()} in {@code Main.java}. */
  static final int CALL_SITE_2 = 13;

  JavaView view;
  ClassType mainClass;
  MethodSignature mainMethod;

  @BeforeEach
  public void setUp() {
    // --8<-- [start:view]
    List<AnalysisInputLocation> inputLocations =
        List.of(
            // the program under analysis: SourceType.Application
            new JavaClassPathAnalysisInputLocation(
                "src/test/resources/CallgraphComparison/binary", SourceType.Application),
            // the JDK of the running JVM: SourceType.Library
            new DefaultRuntimeAnalysisInputLocation());
    JavaView view = new JavaView(inputLocations);
    // --8<-- [end:view]
    this.view = view;

    // --8<-- [start:entry]
    ClassType mainClass = view.getIdentifierFactory().getClassType("Main");
    MethodSignature mainMethod =
        view.getIdentifierFactory()
            .getMethodSignature(mainClass, view.getIdentifierFactory().getMainSubSignature());
    // --8<-- [end:entry]
    this.mainClass = mainClass;
    this.mainMethod = mainMethod;
  }

  @Test
  public void entryPointFromString() {
    // --8<-- [start:entry-string]
    MethodSignature mainMethod =
        view.getIdentifierFactory().parseMethodSignature("<Main: void main(java.lang.String[])>");
    // --8<-- [end:entry-string]
    assertEquals(this.mainMethod, mainMethod);
  }

  @Test
  public void cha() {
    // --8<-- [start:cha]
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(mainMethod))
            .cha()
            .build()
            .computeCallGraph();
    // --8<-- [end:cha]
    assertEquals(Set.of("Circle", "Square", "Triangle", "Hexagon"), areaTargets(cg, CALL_SITE_1));
    assertEquals(Set.of("Circle", "Square", "Triangle", "Hexagon"), areaTargets(cg, CALL_SITE_2));
  }

  @Test
  public void rta() {
    // --8<-- [start:rta]
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(mainMethod))
            .rta()
            .build()
            .computeCallGraph();
    // --8<-- [end:rta]
    assertEquals(Set.of("Circle", "Square", "Triangle"), areaTargets(cg, CALL_SITE_1));
    assertEquals(Set.of("Circle", "Square", "Triangle"), areaTargets(cg, CALL_SITE_2));
  }

  @Test
  public void spark() {
    // --8<-- [start:spark]
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(mainMethod))
            .into(SparkCallGraphConfig::from)
            .sparkOptions(SparkOptions.builder().onFlyCallGraph(true).build())
            .build()
            .computeCallGraph();
    // --8<-- [end:spark]
    assertEquals(Set.of("Circle"), areaTargets(cg, CALL_SITE_1));
    assertEquals(Set.of("Square", "Triangle"), areaTargets(cg, CALL_SITE_2));
  }

  @Test
  public void qilinInsensitive() {
    // --8<-- [start:qilin-insensitive]
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(mainMethod))
            .into(QilinCallGraphConfig::from)
            .mainClass(mainClass)
            .pointerAnalysisConfig(
                PointerAnalysisConfig.builder()
                    .contextSensitivity(ContextSensitivity.insensitive())
                    .singleEntry(true) // only main(), no JVM start-up model
                    .evaluate(false)) // skip the statistics printout
            .build()
            .computeCallGraph();
    // --8<-- [end:qilin-insensitive]
    assertEquals(Set.of("Circle"), areaTargets(cg, CALL_SITE_1));
    assertEquals(Set.of("Square", "Triangle"), areaTargets(cg, CALL_SITE_2));
  }

  @Test
  public void qilinCallSiteSensitive() {
    // --8<-- [start:qilin-1cfa]
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(mainMethod))
            .into(QilinCallGraphConfig::from)
            .mainClass(mainClass)
            .pointerAnalysisConfig(
                PointerAnalysisConfig.builder()
                    .contextSensitivity(ContextSensitivity.callSite(1)) // 1-call-site-sensitive
                    .singleEntry(true)
                    .evaluate(false))
            .build()
            .computeCallGraph();
    // --8<-- [end:qilin-1cfa]
    assertEquals(Set.of("Circle"), areaTargets(cg, CALL_SITE_1));
    assertEquals(Set.of("Square"), areaTargets(cg, CALL_SITE_2));
  }

  @Test
  public void diff() {
    CallGraph chaGraph =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(mainMethod))
            .cha()
            .build()
            .computeCallGraph();
    CallGraph rtaGraph =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(mainMethod))
            .rta()
            .build()
            .computeCallGraph();

    // --8<-- [start:diff]
    CallGraphDifference difference = chaGraph.diff(rtaGraph);
    // methods only CHA considers reachable
    List<MethodSignature> onlyInCha = difference.uniqueBaseGraphMethods();
    // --8<-- [end:diff]
    assertTrue(
        onlyInCha.contains(
            view.getIdentifierFactory().parseMethodSignature("<Hexagon: double area()>")));
  }

  /**
   * The simple names of the classes whose {@code area()} the call site at {@code line} of {@code
   * Main.main} may call.
   */
  // --8<-- [start:query]
  Set<String> areaTargets(CallGraph cg, int line) {
    return cg.callsFrom(mainMethod).stream()
        .filter(call -> call.getLineNumber() == line)
        .map(CallGraph.Call::targetMethodSignature)
        .filter(target -> target.getName().equals("area"))
        .map(target -> target.getDeclClassType().getClassName())
        .collect(Collectors.toCollection(TreeSet::new));
  }
  // --8<-- [end:query]
}
