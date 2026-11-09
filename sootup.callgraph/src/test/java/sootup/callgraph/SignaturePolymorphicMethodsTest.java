package sootup.callgraph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

public class SignaturePolymorphicMethodsTest {

  private static final JavaIdentifierFactory factory = new JavaIdentifierFactory();
  private static final ClassType MH = factory.getClassType("java.lang.invoke.MethodHandle");
  private static final ClassType VH = factory.getClassType("java.lang.invoke.VarHandle");
  private static final String TEST_CP = "src/test/resources/callgraph/Polymorphic/binary";

  private static JavaView view;

  @BeforeAll
  static void setUp() {
    view =
        new JavaView(
            Arrays.asList(
                new DefaultRuntimeAnalysisInputLocation(SourceType.Library),
                new JavaClassPathAnalysisInputLocation(TEST_CP)));
  }

  private static MethodSignature callSite(
      ClassType owner, String name, String ret, String... params) {
    return factory.getMethodSignature(owner, name, ret, Arrays.asList(params));
  }

  private static MethodSignature declared(ClassType owner, String name, String ret) {
    return factory.getMethodSignature(
        owner, name, ret, Collections.singletonList("java.lang.Object[]"));
  }

  @Test
  public void resolvesInstancePolymorphic() {
    MethodSignature site = callSite(MH, "invokeExact", "int", "java.lang.String", "int");
    assertEquals(
        Optional.of(declared(MH, "invokeExact", "java.lang.Object")),
        SignaturePolymorphicMethods.resolve(view, site).map(SootMethod::getSignature));
    // return type not constrained: VarHandle.compareAndSet returns boolean
    MethodSignature cas =
        callSite(VH, "compareAndSet", "boolean", "java.lang.Object", "int", "int");
    assertEquals(
        Optional.of(declared(VH, "compareAndSet", "boolean")),
        SignaturePolymorphicMethods.resolve(view, cas).map(SootMethod::getSignature));
  }

  @Test
  public void resolvesStaticPolymorphic() {
    // JDK-internal, used by LambdaForms: static native @PolymorphicSignature
    MethodSignature site = callSite(MH, "linkToStatic", "void", "int", "java.lang.Object");
    Optional<? extends SootMethod> m = SignaturePolymorphicMethods.resolve(view, site);
    assertTrue(m.isPresent());
    assertTrue(m.get().isStatic());
  }

  @Test
  public void resolvesViaSubclassOwner() {
    // call-site owner is a JDK-internal MethodHandle subclass
    ClassType bound = factory.getClassType("java.lang.invoke.BoundMethodHandle");
    MethodSignature site = callSite(bound, "invokeBasic", "java.lang.Object", "int");
    assertEquals(
        Optional.of(declared(MH, "invokeBasic", "java.lang.Object")),
        SignaturePolymorphicMethods.resolve(view, site).map(SootMethod::getSignature));
  }

  @Test
  public void rejectsNonPolymorphic() {
    // regular MethodHandle method
    assertFalse(
        SignaturePolymorphicMethods.resolve(
                view, callSite(MH, "type", "java.lang.invoke.MethodType"))
            .isPresent());
    // overloaded name -> not polymorphic
    assertFalse(
        SignaturePolymorphicMethods.resolve(
                view, callSite(MH, "bindTo", "java.lang.invoke.MethodHandle", "int"))
            .isPresent());
    // other class
    ClassType str = factory.getClassType("java.lang.String");
    assertFalse(
        SignaturePolymorphicMethods.resolve(view, callSite(str, "invokeExact", "int")).isPresent());
    assertFalse(SignaturePolymorphicMethods.isHolder(str));
    assertTrue(SignaturePolymorphicMethods.isHolder(MH));
    assertTrue(SignaturePolymorphicMethods.isHolder(VH));
  }

  @Test
  public void noJdkInViewYieldsEmpty() {
    JavaView appOnly = new JavaView(new JavaClassPathAnalysisInputLocation(TEST_CP));
    MethodSignature site = callSite(MH, "invokeExact", "int", "java.lang.String");
    assertFalse(SignaturePolymorphicMethods.resolve(appOnly, site).isPresent());
    // body still converts
    MethodSignature main =
        factory.getMethodSignature(
            factory.getClassType("PolymorphicSignatureExamples"),
            "main",
            "void",
            Collections.singletonList("java.lang.String[]"));
    assertTrue(appOnly.getMethod(main).orElseThrow().hasBody());
  }

  /** Frontend keeps the call-site descriptor; resolution is the call graph's job. */
  @Test
  public void jimpleKeepsCallSiteDescriptor() {
    MethodSignature main =
        factory.getMethodSignature(
            factory.getClassType("PolymorphicSignatureExamples"),
            "main",
            "void",
            Collections.singletonList("java.lang.String[]"));
    List<MethodSignature> invoked =
        view.getMethod(main).orElseThrow().getBody().getStmts().stream()
            .filter(s -> s.isInvokableStmt())
            .flatMap(s -> s.asInvokableStmt().getInvokeExpr().stream())
            .map(AbstractInvokeExpr::getMethodSignature)
            .toList();
    MethodSignature site =
        callSite(MH, "invokeExact", "int", "java.lang.String", "java.lang.String", "int");
    assertTrue(invoked.contains(site), () -> "missing " + site + " in " + invoked);
  }
}
