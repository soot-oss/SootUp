package sootup.callgraph;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.views.JavaView;

/**
 * Tests that {@link FastClassHierarchyAnalysisAlgorithm} resolves virtual calls over the subtypes
 * of the static type of the receiver local, as Soot's CHA does, where {@link
 * ClassHierarchyAnalysisAlgorithm} uses the class the invoked method reference names.
 */
public class FastClassHierarchyAnalysisAlgorithmTest extends CallGraphTest {

  @Override
  protected AbstractCallGraphAlgorithm createAlgorithm(JavaView view) {
    return new FastClassHierarchyAnalysisAlgorithm(view);
  }

  private MethodSignature method(String className, String name) {
    return identifierFactory.getMethodSignature(
        identifierFactory.getClassType(className), name, "void", Collections.emptyList());
  }

  private MethodSignature toString(String className) {
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
    InvokableStmt call = getInvokableStmt(mainMethodSignature, toString("java.lang.Object"));

    assertEquals(Set.of(toString("receiver.Main$Square")), targetsOf(cg, call));
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
}
