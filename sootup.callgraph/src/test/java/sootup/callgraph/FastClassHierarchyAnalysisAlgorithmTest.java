package sootup.callgraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.MutableTypeHierarchy;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/**
 * Tests that {@link FastClassHierarchyAnalysisAlgorithm} resolves virtual calls over the subtypes
 * of the static type of the receiver local, as Soot's CHA does, where {@link
 * ClassHierarchyAnalysisAlgorithm} uses the class the invoked method reference names.
 *
 * <p>It runs all tests of the CHA. The ones whose receiver local is typed narrower than the class
 * the call refers to are overridden with the targets of the receiver's type; Soot's CHA resolves
 * the same calls of these programs to the same targets.
 */
public class FastClassHierarchyAnalysisAlgorithmTest extends ClassHierarchyAnalysisAlgorithmTest {

  @Override
  protected ClassHierarchyAnalysisAlgorithm createAlgorithm(JavaView view) {
    return new FastClassHierarchyAnalysisAlgorithm(view);
  }

  private MethodSignature method(String className, String name) {
    return identifierFactory.getMethodSignature(
        identifierFactory.getClassType(className), name, "void", Collections.emptyList());
  }

  private MethodSignature toStringOf(String className) {
    return identifierFactory.getMethodSignature(
        identifierFactory.getClassType(className),
        "toString",
        "java.lang.String",
        Collections.emptyList());
  }

  private Set<MethodSignature> targetsOf(CallGraph cg, InvokableStmt stmt) {
    return cg.callsFrom(mainMethodSignature).stream()
        .filter(call -> call.invokableStmt().equals(stmt))
        .map(CallGraph.Call::targetMethodSignature)
        .collect(Collectors.toSet());
  }

  /**
   * {@code A b = new B(); b.virtualDispatch();} refers to A.virtualDispatch(), the local is typed
   * B. The targets are the implementations of B and its subtypes - also of F, which is never
   * instantiated - but not those of A and its other subtypes.
   */
  @Test
  public void testDispatchOnTheTypeOfTheReceiverLocal() {
    CallGraph cg = loadCallGraph("FastCHA", "fastcha.Main");
    InvokableStmt call =
        getInvokableStmt(mainMethodSignature, method("fastcha.A", "virtualDispatch"), 0);

    assertEquals(
        Set.of(method("fastcha.B", "virtualDispatch"), method("fastcha.F", "virtualDispatch")),
        targetsOf(cg, call));
  }

  /** A class of the receiver's type that does not override the method runs the inherited one */
  @Test
  public void testInheritedImplementation() {
    CallGraph cg = loadCallGraph("FastCHA", "fastcha.Main");
    InvokableStmt call =
        getInvokableStmt(mainMethodSignature, method("fastcha.A", "virtualDispatch"), 1);

    assertEquals(Set.of(method("fastcha.A", "virtualDispatch")), targetsOf(cg, call));
  }

  /** K inherits the default method of I; L, another implementation of I, is not a K */
  @Test
  public void testInheritedDefaultMethod() {
    CallGraph cg = loadCallGraph("FastCHA", "fastcha.Main");
    InvokableStmt call =
        getInvokableStmt(mainMethodSignature, method("fastcha.I", "defaultMethod"));

    assertEquals(Set.of(method("fastcha.I", "defaultMethod")), targetsOf(cg, call));
  }

  /**
   * dx and casts to Object encode a call to an Object method on an interface receiver as a call to
   * java.lang.Object, e.g. {@code virtualinvoke shape.<java.lang.Object: java.lang.String
   * toString()>()} with {@code shape} of type Shape. Only the implementations of Shape's classes
   * are targets, not every toString() of the program.
   */
  @Test
  public void testObjectMethodOnAnInterfaceReceiver() {
    CallGraph cg = loadCallGraph("ReceiverType", "receiver.Main");
    InvokableStmt call = getInvokableStmt(mainMethodSignature, toStringOf("java.lang.Object"));

    assertEquals(Set.of(toStringOf("receiver.Main$Square")), targetsOf(cg, call));
  }

  /** The classic CHA resolves the same calls over the subtypes of the referenced class */
  @Test
  public void testClassHierarchyAnalysisUsesTheReferencedClass() {
    loadCallGraph("FastCHA", "fastcha.Main");
    CallGraph cha =
        new ClassHierarchyAnalysisAlgorithm(view)
            .initialize(Collections.singletonList(mainMethodSignature));
    InvokableStmt call =
        getInvokableStmt(mainMethodSignature, method("fastcha.A", "virtualDispatch"), 0);

    assertEquals(
        Set.of(
            method("fastcha.A", "virtualDispatch"),
            method("fastcha.B", "virtualDispatch"),
            method("fastcha.F", "virtualDispatch"),
            method("fastcha.E", "virtualDispatch")),
        targetsOf(cha, call));
  }

  /**
   * {@code A objB = new B(); objB.virtualDispatch();}: the local is typed B, so the call reaches
   * B.virtualDispatch() only. The CHA also reaches A's, D's and E's.
   */
  @Override
  @Test
  public void testMiscExample1() {
    CallGraph cg = loadCallGraph("Misc", "example1.Example");
    MethodSignature virtualMethodA = method("example1.A", "virtualDispatch");
    InvokableStmt virtualCall = getInvokableStmt(mainMethodSignature, virtualMethodA);
    MethodSignature staticMethodB =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.B"),
            "staticDispatch",
            "void",
            Collections.singletonList("java.lang.Object"));

    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            method("example1.B", "<init>"),
            getInvokableStmt(mainMethodSignature, method("example1.B", "<init>"))));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            method("example1.C", "<init>"),
            getInvokableStmt(mainMethodSignature, method("example1.C", "<init>"))));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            method("example1.E", "<init>"),
            getInvokableStmt(mainMethodSignature, method("example1.E", "<init>"))));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            staticMethodB,
            getInvokableStmt(mainMethodSignature, staticMethodB)));

    assertEquals(Set.of(method("example1.B", "virtualDispatch")), targetsOf(cg, virtualCall));
    for (String other : List.of("example1.A", "example1.C", "example1.D", "example1.E")) {
      assertFalse(cg.containsMethod(method(other, "virtualDispatch")));
    }
  }

  /**
   * {@code SuperClass a = new Class(); a.method();}: the local is typed Class, so the call reaches
   * Class.method() only. The CHA also reaches SuperClass.method().
   */
  @Override
  @Test
  public void testAbstractMethodInSubClass() {
    CallGraph cg = loadCallGraph("AbstractMethod", "am2.Main");
    InvokableStmt call = getInvokableStmt(mainMethodSignature, method("am2.SuperClass", "method"));

    assertEquals(Set.of(method("am2.Class", "method")), targetsOf(cg, call));
  }

  /**
   * Like the CHA, the targets for a receiver type are computed once: 21 resolutions of {@code
   * obj.virtualDispatch()}, whose local is typed B, ask the hierarchy for B's subtypes once.
   */
  @Override
  @Test
  public void testResolveVirtualDispatchTargetsOnlyQueriesHierarchyOnce() {
    loadCallGraph("CHACache", "chacache.Example");
    ClassType classTypeB = identifierFactory.getClassType("chacache.B");
    TypeHierarchy spyHierarchy = Mockito.spy(view.getTypeHierarchy());
    JavaView spyView = Mockito.spy(view);
    Mockito.doReturn(spyHierarchy).when(spyView).getTypeHierarchy();
    ClassHierarchyAnalysisAlgorithm algorithm2 = createAlgorithm(spyView);
    SootMethod mainMethod = view.getMethod(mainMethodSignature).orElseThrow();
    InvokableStmt invokeStmt =
        getInvokableStmt(mainMethodSignature, method("chacache.A", "virtualDispatch"));

    List<MethodSignature> firstResult =
        algorithm2.resolveCall(mainMethod, invokeStmt).collect(Collectors.toList());
    assertEquals(List.of(method("chacache.B", "virtualDispatch")), firstResult);
    for (int i = 0; i < 20; i++) {
      assertEquals(
          firstResult, algorithm2.resolveCall(mainMethod, invokeStmt).collect(Collectors.toList()));
    }
    Mockito.verify(spyHierarchy, Mockito.times(1)).subtypeClassesOf(classTypeB);
  }

  /**
   * Adding a type to the hierarchy invalidates the cached targets of a receiver type, as it does
   * for the CHA. The added C extends A, not B, so it is no target of a receiver of type B.
   */
  @Override
  @Test
  public void testResolveCallReQueriesHierarchyAfterAddType() {
    loadCallGraph("CHACache", "chacache.Example");
    ClassType classTypeB = identifierFactory.getClassType("chacache.B");
    MethodSignature virtualDispatchB = method("chacache.B", "virtualDispatch");
    TypeHierarchy spyHierarchy = Mockito.spy(view.getTypeHierarchy());
    JavaView spyView = Mockito.spy(view);
    Mockito.doReturn(spyHierarchy).when(spyView).getTypeHierarchy();
    ClassHierarchyAnalysisAlgorithm algorithm2 = createAlgorithm(spyView);
    SootMethod mainMethod = view.getMethod(mainMethodSignature).orElseThrow();
    InvokableStmt invokeStmt =
        getInvokableStmt(mainMethodSignature, method("chacache.A", "virtualDispatch"));

    assertEquals(
        List.of(virtualDispatchB),
        algorithm2.resolveCall(mainMethod, invokeStmt).collect(Collectors.toList()));
    algorithm2.resolveCall(mainMethod, invokeStmt).collect(Collectors.toList());
    Mockito.verify(spyHierarchy, Mockito.times(1)).subtypeClassesOf(classTypeB);

    List<AnalysisInputLocation> extraInputLocations = new ArrayList<>();
    extraInputLocations.add(new DefaultRuntimeAnalysisInputLocation());
    extraInputLocations.add(
        new JavaClassPathAnalysisInputLocation(
            "src/test/resources/callgraph/CHACache/binary-lazy"));
    JavaView extraView = new JavaView(extraInputLocations);
    SootClass classC =
        extraView.getClass(identifierFactory.getClassType("chacache.C")).orElseThrow();
    // through the spy, which copied the modification count when it was created (see the CHA test)
    ((MutableTypeHierarchy) spyHierarchy).addType(classC);

    assertEquals(
        List.of(virtualDispatchB),
        algorithm2.resolveCall(mainMethod, invokeStmt).collect(Collectors.toList()));
    Mockito.verify(spyHierarchy, Mockito.times(2)).subtypeClassesOf(classTypeB);
  }
}
