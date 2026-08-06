package sootup.callgraph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sootup.callgraph.CallGraph.Call;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.MutableTypeHierarchy;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.core.signatures.PolymorphicMethodSignature;
import sootup.core.signatures.PolymorphicMethodSubSignature;
import sootup.core.types.Type;
import sootup.java.core.views.JavaView;

/**
 * Input source examples taken from <a href="https://bitbucket.org/delors/cats/src/master/">cats</a>
 *
 * @author Markus Schmidt
 */
public class ClassHierarchyAnalysisAlgorithmTest extends CallGraphAlgorithmTest {

  // TODO: StaticInitializers, Lambdas ?

  @Override
  protected ClassHierarchyAnalysisAlgorithm createAlgorithm(JavaView view) {
    return new ClassHierarchyAnalysisAlgorithm(view);
  }

  /**
   * Testing the call graph generation using CHA on a code example
   *
   * <p>In this testcase, the call graph of Example1 in folder {@link callgraph.Misc} is created
   * using CHA. The testcase expects a call from main to the constructors of B,C, and E The virtual
   * call print is resolved to all subtypes of A, A.print B.print, C.print, D.print and E.print,
   * Overall, 8 calls are expected in the main method. the constructor of B is called directly and
   * indirectly by C, since B is the super class of C.
   */
  @Test
  public void testMiscExample1() {
    CallGraph cg = loadCallGraph("Misc", "example1.Example");
    MethodSignature constructorA =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.A"),
            "<init>",
            "void",
            Collections.emptyList());

    MethodSignature constructorB =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.B"),
            "<init>",
            "void",
            Collections.emptyList());

    MethodSignature constructorC =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.C"),
            "<init>",
            "void",
            Collections.emptyList());

    MethodSignature constructorD =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.D"),
            "<init>",
            "void",
            Collections.emptyList());

    MethodSignature constructorE =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.E"),
            "<init>",
            "void",
            Collections.emptyList());

    MethodSignature virtualMethodA =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.A"),
            "virtualDispatch",
            "void",
            Collections.emptyList());

    MethodSignature virtualMethodB =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.B"),
            "virtualDispatch",
            "void",
            Collections.emptyList());

    MethodSignature virtualMethodC =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.C"),
            "virtualDispatch",
            "void",
            Collections.emptyList());

    MethodSignature virtualMethodD =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.D"),
            "virtualDispatch",
            "void",
            Collections.emptyList());

    MethodSignature virtualMethodE =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.E"),
            "virtualDispatch",
            "void",
            Collections.emptyList());

    MethodSignature staticMethodA =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.A"),
            "staticDispatch",
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature staticMethodB =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.B"),
            "staticDispatch",
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature staticMethodC =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.C"),
            "staticDispatch",
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature staticMethodD =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.D"),
            "staticDispatch",
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature staticMethodE =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.E"),
            "staticDispatch",
            "void",
            Collections.singletonList("java.lang.Object"));

    assertFalse(
        cg.containsCall(
            mainMethodSignature,
            constructorA,
            getInvokableStmt(mainMethodSignature, constructorB)));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            constructorB,
            getInvokableStmt(mainMethodSignature, constructorB)));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            constructorC,
            getInvokableStmt(mainMethodSignature, constructorC)));

    assertFalse(
        cg.containsCall(
            mainMethodSignature,
            constructorD,
            getInvokableStmt(mainMethodSignature, constructorC)));

    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            constructorE,
            getInvokableStmt(mainMethodSignature, constructorE)));

    assertFalse(cg.containsMethod(staticMethodA));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            staticMethodB,
            getInvokableStmt(mainMethodSignature, staticMethodB)));
    assertFalse(cg.containsMethod(staticMethodC));
    assertFalse(cg.containsMethod(staticMethodD));
    assertFalse(cg.containsMethod(staticMethodE));

    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            virtualMethodA,
            getInvokableStmt(mainMethodSignature, virtualMethodA)));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            virtualMethodB,
            getInvokableStmt(mainMethodSignature, virtualMethodA)));
    assertFalse(cg.containsMethod(virtualMethodC));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            virtualMethodD,
            getInvokableStmt(mainMethodSignature, virtualMethodA)));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            virtualMethodE,
            getInvokableStmt(mainMethodSignature, virtualMethodA)));

    assertEquals(1, cg.callsTo(constructorB).size());
    assertEquals(1, cg.callsTo(constructorC).size());
    assertEquals(1, cg.callsTo(constructorE).size());
    assertEquals(1, cg.callsTo(staticMethodB).size());
    assertEquals(1, cg.callsTo(virtualMethodA).size());
    assertEquals(1, cg.callsTo(virtualMethodB).size());
    assertEquals(1, cg.callsTo(virtualMethodD).size());
    assertEquals(1, cg.callsTo(virtualMethodE).size());

    assertEquals(0, cg.callsFrom(staticMethodB).size());
    assertEquals(0, cg.callsFrom(virtualMethodA).size());
    assertEquals(0, cg.callsFrom(virtualMethodB).size());
    assertEquals(0, cg.callsFrom(virtualMethodD).size());
    assertEquals(0, cg.callsFrom(virtualMethodE).size());

    SootMethod methodData = view.getMethod(mainMethodSignature).orElse(null);
    assertNotNull(methodData);
    int prevLine =
        methodData
            .getBody()
            .getFirstNonIdentityStmt()
            .getPositionInfo()
            .getStmtPosition()
            .getFirstLine();

    for (Call call : cg.sortedCallsFrom(mainMethodSignature)) {
      assertTrue(call.getLineNumber() >= prevLine);
      prevLine = call.getLineNumber();
    }

    GraphBasedCallGraph g = new GraphBasedCallGraph(List.of(mainMethodSignature));
    g.addMethod(mainMethodSignature);

    g.addCall(
        mainMethodSignature, virtualMethodA, getInvokableStmt(mainMethodSignature, virtualMethodA));
    g.addCall(
        mainMethodSignature, staticMethodB, getInvokableStmt(mainMethodSignature, staticMethodB));

    assertEquals(2, g.callsFrom(mainMethodSignature).size());
  }

  @Test
  public void testPolymorphicSignatureExamples() {
    CallGraph cg = loadCallGraph("Polymorphic", "PolymorphicSignatureExamples");

    for (CallGraph.Call call : cg.getCalls()) {
      System.out.println(call);
    }

    ClassType methodHandleType = identifierFactory.getClassType("java.lang.invoke.MethodHandle");
    ClassType varHandleType = identifierFactory.getClassType("java.lang.invoke.VarHandle");
    Type returnType = identifierFactory.getType("java.lang.Object");
    Type parameterTypes = identifierFactory.getType("java.lang.Object[]");

    PolymorphicMethodSignature invokeExactMethodSig =
        new PolymorphicMethodSignature(
            methodHandleType,
            new PolymorphicMethodSubSignature(
                "invokeExact", Collections.singletonList(parameterTypes), returnType));
    Set<MethodSignature> callSourcesInvokeExact = cg.callSourcesTo(invokeExactMethodSig);
    assertTrue(callSourcesInvokeExact.contains(mainMethodSignature));

    PolymorphicMethodSignature invokeMethodSig =
        new PolymorphicMethodSignature(
            methodHandleType,
            new PolymorphicMethodSubSignature(
                "invoke", Collections.singletonList(parameterTypes), returnType));
    Set<MethodSignature> callSourcesInvoke = cg.callSourcesTo(invokeMethodSig);
    assertTrue(callSourcesInvoke.contains(mainMethodSignature));

    PolymorphicMethodSignature getMethodSig =
        new PolymorphicMethodSignature(
            varHandleType,
            new PolymorphicMethodSubSignature(
                "get", Collections.singletonList(parameterTypes), returnType));
    Set<MethodSignature> callSourcesGet = cg.callSourcesTo(getMethodSig);
    assertTrue(callSourcesGet.contains(mainMethodSignature));
  }

  /**
   * Verifies that CHA's per-target-method-signature cache for virtual/interface dispatch resolution
   * (see {@link ClassHierarchyAnalysisAlgorithm#resolveVirtualDispatchTargets}) is invalidated
   * after the type hierarchy is mutated via {@link MutableTypeHierarchy#addType} - i.e. a call
   * after the mutation must re-query the hierarchy rather than serve a stale cached result.
   *
   * <p>{@code chacache.C} is deliberately compiled to a separate location that is never part of the
   * main view's classpath (see {@code CHACache/binary-lazy}) and is added via a {@link SootClass}
   * resolved through a different {@link JavaView}, so it stays unresolvable by the main view's
   * {@code getClass(...)} even after {@code addType}. Since {@link
   * ClassHierarchyAnalysisAlgorithm#resolveVirtualDispatchTargets} resolves each subtype via {@code
   * view.getClass(...)}, C never actually shows up in {@code resolveCall}'s result - so a plain
   * before/after equality check on the result wouldn't prove invalidation happened, it would pass
   * identically whether invalidation is broken or not. Instead this spies on the {@link
   * TypeHierarchy} to directly verify the hierarchy is queried again after the mutation.
   */
  @Test
  public void testResolveCallReQueriesHierarchyAfterAddType() {
    loadCallGraph("CHACache", "chacache.Example");

    ClassType classTypeA = identifierFactory.getClassType("chacache.A");
    MethodSignature virtualDispatchA =
        identifierFactory.getMethodSignature(
            classTypeA, "virtualDispatch", "void", Collections.emptyList());
    MethodSignature virtualDispatchB =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("chacache.B"),
            "virtualDispatch",
            "void",
            Collections.emptyList());
    MethodSignature virtualDispatchC =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("chacache.C"),
            "virtualDispatch",
            "void",
            Collections.emptyList());

    TypeHierarchy spyHierarchy = Mockito.spy(view.getTypeHierarchy());
    JavaView spyView = Mockito.spy(view);
    Mockito.doReturn(spyHierarchy).when(spyView).getTypeHierarchy();

    // Second algorithm instance sharing the same view - and therefore the same memoized
    // TypeHierarchy - so we can call resolveCall() directly and observe its cache.
    ClassHierarchyAnalysisAlgorithm algorithm2 = createAlgorithm(spyView);
    SootMethod mainMethod = view.getMethod(mainMethodSignature).orElseThrow();
    InvokableStmt invokeStmt = getInvokableStmt(mainMethodSignature, virtualDispatchA);

    List<MethodSignature> before =
        algorithm2.resolveCall(mainMethod, invokeStmt).collect(Collectors.toList());
    assertTrue(before.contains(virtualDispatchA));
    assertTrue(before.contains(virtualDispatchB));
    assertFalse(
        before.contains(virtualDispatchC),
        "sanity check: C is not part of the main view's classpath yet");

    // Calling again without any mutation must be served from CHA's own cache.
    algorithm2.resolveCall(mainMethod, invokeStmt).collect(Collectors.toList());
    Mockito.verify(spyHierarchy, Mockito.times(1)).subtypeClassesOf(classTypeA);

    // Resolve C from its isolated classpath location and add it directly to the main view's
    // hierarchy, mirroring how a previously-unseen type is lazily discovered mid-run - even though
    // C stays unresolvable via view.getClass() (see class javadoc), addType() still mutates the
    // hierarchy and must invalidate CHA's cache.
    List<AnalysisInputLocation> extraInputLocations = new ArrayList<>();
    extraInputLocations.add(new DefaultRuntimeAnalysisInputLocation());
    extraInputLocations.add(
        new JavaClassPathAnalysisInputLocation(
            "src/test/resources/callgraph/CHACache/binary-lazy"));
    JavaView extraView = new JavaView(extraInputLocations);
    SootClass classC =
        extraView.getClass(identifierFactory.getClassType("chacache.C")).orElseThrow();

    // Mutate through spyHierarchy itself, not view.getTypeHierarchy() - Mockito.spy() clones
    // primitive fields like modificationCount at spy-creation time rather than sharing storage
    // with the original, so a mutation applied to the original wouldn't be visible through the
    // spy that algorithm2 actually reads from.
    ((MutableTypeHierarchy) spyHierarchy).addType(classC);

    List<MethodSignature> after =
        algorithm2.resolveCall(mainMethod, invokeStmt).collect(Collectors.toList());
    assertFalse(
        after.contains(virtualDispatchC),
        "C is not resolvable via the main view's getClass(), so it must not appear in the"
            + " resolved dispatch targets even though it was added to the hierarchy graph");
    assertTrue(after.contains(virtualDispatchA));
    assertTrue(after.contains(virtualDispatchB));

    // The result is unchanged, but the hierarchy must have been re-queried rather than serving a
    // stale cached CHA result - otherwise this call count would still be 1.
    Mockito.verify(spyHierarchy, Mockito.times(2)).subtypeClassesOf(classTypeA);
  }

  /**
   * Deterministic counterpart to {@link #testRepeatedResolveCallIsCheapAfterFirstResolution}:
   * instead of inferring caching from wall-clock time, this mocks the {@link TypeHierarchy} to
   * directly count how many times {@link TypeHierarchy#subtypeClassesOf} - the expensive call
   * {@link ClassHierarchyAnalysisAlgorithm#resolveVirtualDispatchTargets} is meant to avoid
   * repeating - is actually invoked. Regardless of machine speed, resolving the same target
   * signature many times must only reach the hierarchy once; every call after the first must be
   * served from {@code virtualDispatchTargetsCache}.
   */
  @Test
  public void testResolveVirtualDispatchTargetsOnlyQueriesHierarchyOnce() {
    loadCallGraph("CHACache", "chacache.Example");

    ClassType classTypeA = identifierFactory.getClassType("chacache.A");
    MethodSignature virtualDispatchA =
        identifierFactory.getMethodSignature(
            classTypeA, "virtualDispatch", "void", Collections.emptyList());

    TypeHierarchy spyHierarchy = Mockito.spy(view.getTypeHierarchy());
    JavaView spyView = Mockito.spy(view);
    Mockito.doReturn(spyHierarchy).when(spyView).getTypeHierarchy();

    ClassHierarchyAnalysisAlgorithm algorithm2 = createAlgorithm(spyView);
    SootMethod mainMethod = view.getMethod(mainMethodSignature).orElseThrow();
    InvokableStmt invokeStmt = getInvokableStmt(mainMethodSignature, virtualDispatchA);

    List<MethodSignature> firstResult =
        algorithm2.resolveCall(mainMethod, invokeStmt).collect(Collectors.toList());
    assertTrue(firstResult.contains(virtualDispatchA));

    for (int i = 0; i < 20; i++) {
      List<MethodSignature> repeated =
          algorithm2.resolveCall(mainMethod, invokeStmt).collect(Collectors.toList());
      assertEquals(firstResult, repeated);
    }

    // 21 resolveCall() invocations for the same target, but the hierarchy must only have been
    // asked for A's subtypes once - everything after the first call came from the cache.
    Mockito.verify(spyHierarchy, Mockito.times(1)).subtypeClassesOf(classTypeA);
  }

  /**
   * Demonstrates the actual performance win from {@link
   * ClassHierarchyAnalysisAlgorithm#resolveVirtualDispatchTargets}: repeatedly resolving the *same*
   * virtual call site is cheap, even when the first resolution is genuinely expensive.
   *
   * <p>The fixture calls {@code Object.toString()} against a view that includes the full JDK
   * runtime, so the first resolution has to enumerate every one of Object's several thousand
   * subtypes and check each for its own override - a deliberately expensive first call. Without
   * caching, every one of the 500 repeat calls below would redo that same work; with it, they
   * should be an O(1) map lookup. This intentionally does not assert on wall-clock time - that
   * varies too much across machines/CI to be a reliable pass/fail signal - and instead only checks
   * that repeated resolution keeps returning the same, correct result. The actual timing difference
   * (cached vs. uncached) is measured separately and reported in the PR description.
   */
  @Test
  public void testRepeatedResolveCallIsCheapAfterFirstResolution() {
    JavaView perfView = createViewForClassPath("src/test/resources/callgraph/CHAPerf/binary");
    JavaIdentifierFactory perfIdentifierFactory = perfView.getIdentifierFactory();
    MethodSignature perfMainSig =
        perfIdentifierFactory.getMethodSignature(
            perfIdentifierFactory.getClassType("chaperf.Example"),
            perfIdentifierFactory.getMainSubSignature());

    SootMethod perfMainMethod = perfView.getMethod(perfMainSig).orElseThrow();
    InvokableStmt toStringInvoke = null;
    for (Stmt stmt : perfMainMethod.getBody().getStmts()) {
      if (stmt instanceof InvokableStmt) {
        Optional<AbstractInvokeExpr> expr = ((InvokableStmt) stmt).getInvokeExpr();
        // Skip the <init>() special-invoke from "new Object()" - we specifically want the
        // virtual .toString() call, which dispatches against every class implementing Object.
        if (expr.isPresent() && !(expr.get() instanceof JSpecialInvokeExpr)) {
          toStringInvoke = (InvokableStmt) stmt;
          break;
        }
      }
    }
    assertNotNull(toStringInvoke, "expected to find the Object.toString() call site");

    ClassHierarchyAnalysisAlgorithm perfAlgorithm = createAlgorithm(perfView);

    // Cold: pays the full cost of resolving Object's subtypes across the whole JDK runtime.
    long firstCallTargets = perfAlgorithm.resolveCall(perfMainMethod, toStringInvoke).count();
    assertTrue(
        firstCallTargets > 500,
        "sanity check: expected a large subtype set for Object.toString(), got only "
            + firstCallTargets
            + " - the fixture or classpath may not be exercising a realistically large hierarchy"
            + " anymore");

    // Warm: the exact same query, repeated many times. No timing assertion - see class javadoc
    // above for why - just that the cached result stays correct across repeats.
    int repeats = 500;
    for (int i = 0; i < repeats; i++) {
      long repeatedTargets = perfAlgorithm.resolveCall(perfMainMethod, toStringInvoke).count();
      assertEquals(firstCallTargets, repeatedTargets);
    }
  }
}
