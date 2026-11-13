package sootup.examples.callgraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import qilin.callgraph.QilinCallGraphConfig;
import qilin.core.PTA;
import qilin.core.PointerAnalysisFactory;
import qilin.core.builder.callgraph.OnFlyCallGraph;
import qilin.core.config.ContextSensitivity;
import qilin.core.config.PointerAnalysisConfig;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphConfig;
import sootup.callgraph.invokedynamic.DynamicInvokeResolver;
import sootup.callgraph.scope.SuppressClinitCallResolver;
import sootup.callgraph.scope.VirtualCallResolver;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.core.views.JavaView;

/** Qilin examples, included in {@code docs/qilin.md}. */
public class QilinCallGraphTest {

  final JavaView view =
      CallGraphOptionsTest.viewOf("src/test/resources/CallgraphComparison/binary");
  final ClassType mainClass = view.getIdentifierFactory().getClassType("Main");
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
    // --8<-- [start:all-options]
    PointerAnalysisConfig.Builder config =
        PointerAnalysisConfig.builder()
            // what to analyze and how precisely
            .contextSensitivity(ContextSensitivity.objectSensitive(2, 1)) // default: insensitive()
            .heapAbstractionPolicy(PointerAnalysisConfig.HeapAbstractionPolicy.ALLOC_SITE)
            .singleEntry(true) // default false: also model JVM start-up (needs a JDK 8 rt)
            .preciseArrayElement(false)
            .stringConstants(false) // true: one object per string constant
            .preciseExceptions(false)
            .enforceEmptyContextForIgnoreTypes(false)
            // <clinit> modelling, see the call graph tutorial
            .seedEntryPointClinits(false)
            .clinitVirtualCallResolver(VirtualCallResolver.all())
            // reflection and invokedynamic
            .reflectionLogPath(null) // path to a TamiFlex refl.log
            .reflectionModel(null) // or any sootup.callgraph.reflection.ReflectionModel
            .resolveDynamicInvoke(true)
            .dynamicInvokeResolver(DynamicInvokeResolver.bootstrapMethodHandles())
            // guided / debloated analyses
            .preAnalysisOnly(false) // only run the pre-analysis of a guided analysis
            .ctxDebloating(false)
            .debloatApproach(PointerAnalysisConfig.DebloatApproach.CONCH)
            .turnerConfig(PointerAnalysisConfig.TurnerConfig.DEFAULT)
            // output
            .analysisName(null) // name used for the output directory
            .outputDirectory("")
            .dumpJimple(false) // currently has no effect
            .dumpPointsToSet(false)
            .dumpLibraryPointsToSet(false)
            .dumpStats(false)
            .evaluate(false); // default true: compute and print statistics afterwards
    // --8<-- [end:all-options]

    // --8<-- [start:via-config]
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(mainMethod))
            .into(QilinCallGraphConfig::from)
            .mainClass(mainClass) // required: Qilin analyzes exactly one main class
            .pointerAnalysisConfig(config) // optional, default: PointerAnalysisConfig.builder()
            .build()
            .computeCallGraph();
    // --8<-- [end:via-config]
    // object sensitivity uses receiver objects as context - the static id() has none, so 2-obj is
    // as imprecise as the context-insensitive analysis here; callSite(1) is what separates the
    // calls
    assertEquals(
        Set.of("Square", "Triangle"), areaTargets(cg, CallGraphComparisonTest.CALL_SITE_2));
  }

  @Test
  public void commonSettingsOverride() {
    // --8<-- [start:common-override]
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(mainMethod))
            // these overwrite the matching PointerAnalysisConfig settings:
            .seedEntryPointClinits(true)
            .virtualCallResolver(
                new SuppressClinitCallResolver(view)) // -> clinitVirtualCallResolver
            .dynamicInvokeResolver(DynamicInvokeResolver.none())
            .into(QilinCallGraphConfig::from)
            .mainClass(mainClass)
            .pointerAnalysisConfig(
                PointerAnalysisConfig.builder()
                    .contextSensitivity(ContextSensitivity.callSite(1))
                    .singleEntry(true)
                    .evaluate(false))
            .build()
            .computeCallGraph();
    // --8<-- [end:common-override]
    assertEquals(Set.of("Square"), areaTargets(cg, CallGraphComparisonTest.CALL_SITE_2));
  }

  @Test
  public void heapMerge() {
    // --8<-- [start:heap-merge]
    PointerAnalysisConfig.Builder config =
        PointerAnalysisConfig.builder()
            .contextSensitivity(ContextSensitivity.insensitive())
            // one object per type for StringBuffer, StringBuilder and Throwables
            .heapAbstractionPolicy(PointerAnalysisConfig.HeapAbstractionPolicy.HEURISTIC_MERGE)
            .singleEntry(true)
            .evaluate(false);
    // --8<-- [end:heap-merge]
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(mainMethod))
            .into(QilinCallGraphConfig::from)
            .mainClass(mainClass)
            .pointerAnalysisConfig(config)
            .build()
            .computeCallGraph();
    assertTrue(cg.containsMethod(mainMethod));
  }

  @Test
  public void debloating() {
    // --8<-- [start:debloating]
    PointerAnalysisConfig.Builder config =
        PointerAnalysisConfig.builder()
            .contextSensitivity(ContextSensitivity.objectSensitive(2, 1))
            .ctxDebloating(true) // only on object-sensitive variants
            .debloatApproach(PointerAnalysisConfig.DebloatApproach.DEBLOATERX)
            .singleEntry(true)
            .evaluate(false);
    // --8<-- [end:debloating]
    assertNotNull(config.build());
  }

  @Test
  public void directPta() {
    // --8<-- [start:direct]
    PointerAnalysisConfig config =
        PointerAnalysisConfig.builder()
            .contextSensitivity(ContextSensitivity.callSite(1))
            .singleEntry(true)
            .evaluate(false)
            .build();
    PTA pta = PointerAnalysisFactory.create(view, mainClass, config);
    pta.run();
    OnFlyCallGraph cg = pta.getCallGraph(); // implements sootup.callgraph.CallGraph
    // --8<-- [end:direct]
    assertEquals(Set.of("Square"), areaTargets(cg, CallGraphComparisonTest.CALL_SITE_2));
    assertFalse(pta.getNakedReachableMethods().isEmpty());
  }
}
