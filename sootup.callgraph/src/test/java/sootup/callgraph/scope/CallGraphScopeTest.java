package sootup.callgraph.scope;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

@Tag("Java8")
@Tag("Java9")
public class CallGraphScopeTest {

  private final JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

  private JavaView createLibraryView() {
    String classPath = "src/test/resources/callgraph/Library/binary/";
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(new DefaultRuntimeAnalysisInputLocation());
    inputLocations.add(
        new JavaClassPathAnalysisInputLocation(classPath + "application/", SourceType.Application));
    inputLocations.add(
        new JavaClassPathAnalysisInputLocation(classPath + "library/", SourceType.Library));
    return new JavaView(inputLocations);
  }

  @Test
  public void testExcludedCallsCollectingCallResolverRecordsExcludedMethods() {
    JavaView view = createLibraryView();
    MethodSignature mainMethodSignature =
        identifierFactory.getMethodSignature(
            "app.Application", "main", "void", Collections.singletonList("java.lang.String[]"));

    ClassType libraryClassType = identifierFactory.getClassType("lib.Library");
    MethodSignature libraryConstructor =
        identifierFactory.getMethodSignature(
            libraryClassType, "<init>", "void", Collections.emptyList());
    MethodSignature libraryUse =
        identifierFactory.getMethodSignature(
            libraryClassType, "use", "void", Collections.emptyList());
    MethodSignature libraryA =
        identifierFactory.getMethodSignature(
            libraryClassType, "a", "void", Collections.emptyList());

    ExcludedCallsCollectingCallResolver resolver = new ExcludedCallsCollectingCallResolver(view);
    ClassHierarchyAnalysisAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view, resolver);
    CallGraph cg = cha.initialize(Collections.singletonList(mainMethodSignature));

    // the call edges into the library are still discovered...
    assertTrue(cg.callTargetsFrom(mainMethodSignature).contains(libraryConstructor));
    assertTrue(cg.callTargetsFrom(mainMethodSignature).contains(libraryUse));
    assertTrue(cg.containsMethod(libraryConstructor));
    assertTrue(cg.containsMethod(libraryUse));

    // ...but library methods are not expanded further, since the resolver excludes them
    assertTrue(cg.callsFrom(libraryConstructor).isEmpty());
    assertTrue(cg.callsFrom(libraryUse).isEmpty());
    assertFalse(cg.containsMethod(libraryA));

    // and the resolver recorded exactly the excluded methods that were actually visited
    assertEquals(2, resolver.getVisitedExcludedMethods().size());
    assertTrue(resolver.getVisitedExcludedMethods().contains(libraryConstructor));
    assertTrue(resolver.getVisitedExcludedMethods().contains(libraryUse));
    assertFalse(resolver.getVisitedExcludedMethods().contains(libraryA));
  }

  private JavaView createViewForClassPath(String classPath) {
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(new DefaultRuntimeAnalysisInputLocation());
    inputLocations.add(new JavaClassPathAnalysisInputLocation(classPath, SourceType.Application));
    return new JavaView(inputLocations);
  }

  /**
   * Regression test for the {@code advanceCall}/{@code Frontier.notYetExpanded} promotion
   * mechanism: a target reached first by a {@code STOP_AFTER_CALL} edge must still be expanded once
   * a later edge to the same target admits it with {@code EXPLORE_METHOD} — expansion is the
   * logical OR across all incoming edges, not "whichever edge discovers it first."
   */
  @Test
  public void testStopAfterCallTargetIsPromotedByLaterExploreEdge() {
    JavaView view = createViewForClassPath("src/test/resources/callgraph/ScopePromotion/binary/");

    MethodSignature mainMethodSignature =
        identifierFactory.getMethodSignature(
            "promo.Main", "main", "void", Collections.singletonList("java.lang.String[]"));
    MethodSignature fooSignature =
        identifierFactory.getMethodSignature(
            "promo.Target", "foo", "void", Collections.emptyList());
    MethodSignature barSignature =
        identifierFactory.getMethodSignature(
            "promo.Target", "bar", "void", Collections.emptyList());

    // promo.Main.main() calls promo.Target.foo() twice (two distinct call sites, same target):
    // the first edge admits it as STOP_AFTER_CALL (don't expand yet), the second as
    // EXPLORE_METHOD (do expand) - foo() must end up expanded regardless of visit order.
    Set<MethodSignature> seenOnce = new HashSet<>();
    VirtualCallResolver resolver =
        (caller, callee, statement) ->
            seenOnce.add(callee)
                ? ExplorationVerdict.STOP_AFTER_CALL
                : ExplorationVerdict.EXPLORE_METHOD;

    ClassHierarchyAnalysisAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view, resolver);
    CallGraph cg = cha.initialize(Collections.singletonList(mainMethodSignature));

    assertTrue(cg.containsMethod(fooSignature), "foo() should be a graph node (edge admitted)");
    assertTrue(
        cg.containsMethod(barSignature),
        "foo() should have been expanded once the second edge promoted it");
    assertTrue(
        cg.callsFrom(fooSignature).stream()
            .anyMatch(call -> call.targetMethodSignature().equals(barSignature)),
        "foo() -> bar() edge should exist once foo() was expanded");
  }

  /**
   * Regression test for the pre-dispatch {@code tryAdvance} consistency fix: excluding a caller's
   * statements must prune identically regardless of which of the three call-resolution paths in
   * {@code AbstractCallGraphAlgorithm} would have resolved them - an ordinary explicit call, an
   * implicit {@code Thread.start()}/{@code run()} edge, and a static-initializer trigger. Before
   * the fix, {@code STOP_AFTER_CALL} was treated as "don't explore" only at the explicit-call site
   * and as "go ahead" at the other two.
   */
  @Test
  public void testExcludedCallerPrunesAllThreeCallResolutionPaths() {
    JavaView view = createViewForClassPath("src/test/resources/callgraph/ScopePolarity/binary/");

    MethodSignature mainMethodSignature =
        identifierFactory.getMethodSignature(
            "polarity.Main", "main", "void", Collections.singletonList("java.lang.String[]"));
    MethodSignature excludedCallerSignature =
        identifierFactory.getMethodSignature(
            "polarity.Main", "excludedCaller", "void", Collections.emptyList());
    MethodSignature targetMethodSignature =
        identifierFactory.getMethodSignature(
            "polarity.Target", "method", "void", Collections.emptyList());
    MethodSignature triggerRunSignature =
        identifierFactory.getMethodSignature(
            "polarity.Trigger", "run", "void", Collections.emptyList());
    MethodSignature withClinitClinitSignature =
        identifierFactory.getStaticInitializerSignature(
            identifierFactory.getClassType("polarity.WithClinit"));

    CallResolver resolver =
        (caller, statement) ->
            caller.getName().equals("excludedCaller")
                ? ExplorationVerdict.STOP_AFTER_CALL
                : ExplorationVerdict.EXPLORE_METHOD;

    ClassHierarchyAnalysisAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view, resolver);
    CallGraph cg = cha.initialize(Collections.singletonList(mainMethodSignature));

    assertTrue(
        cg.containsMethod(excludedCallerSignature),
        "excludedCaller() is reached from main() and must appear as a node");
    assertTrue(
        cg.callsFrom(excludedCallerSignature).isEmpty(),
        "excludedCaller() must have zero outgoing edges: none of its statements should resolve");
    assertFalse(
        cg.containsMethod(targetMethodSignature), "explicit call target must not be discovered");
    assertFalse(
        cg.containsMethod(triggerRunSignature),
        "implicit Thread.start()->run() target must not be discovered");
    assertFalse(
        cg.containsMethod(withClinitClinitSignature),
        "static initializer target must not be discovered");
  }
}
