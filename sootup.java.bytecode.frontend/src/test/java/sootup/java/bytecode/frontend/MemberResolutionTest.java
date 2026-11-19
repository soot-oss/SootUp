package sootup.java.bytecode.frontend;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import sootup.core.IdentifierFactory;
import sootup.core.model.MemberResolution;
import sootup.core.model.SootMethod;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JrtFileSystemAnalysisInputLocation;
import sootup.java.core.views.JavaView;

public class MemberResolutionTest {

  private final JavaView view =
      new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/memberresolution"));

  private SootMethod resolve(String signature) {
    return MemberResolution.resolveMethod(
            view, view.getIdentifierFactory().parseMethodSignature(signature))
        .orElse(null);
  }

  @Test
  public void methodDeclaredInSuperclass() {
    SootMethod m = resolve("<ResChild: void inherited()>");
    assertNotNull(m);
    assertEquals("ResBase", m.getDeclaringClassType().getClassName());
  }

  @Test
  public void classMethodBeforeInterfaceMethod() {
    SootMethod m = resolve("<ResChild: java.lang.String abstractMethod()>");
    assertEquals("ResBase", m.getDeclaringClassType().getClassName());
    assertFalse(m.isAbstract());
  }

  @Test
  public void defaultMethodFromInterface() {
    SootMethod m = resolve("<ResChild: java.lang.String defaultMethod()>");
    assertEquals("ResIface", m.getDeclaringClassType().getClassName());
  }

  @Test
  public void abstractMethodFromInterface() {
    SootMethod m = resolve("<ResAbstract: java.lang.String abstractMethod()>");
    assertEquals("ResIface", m.getDeclaringClassType().getClassName());
    assertTrue(m.isAbstract());
  }

  @Test
  public void maximallySpecificDefaultMethodIsChosen() {
    // ResPick implements ResOuter before ResInner, but ResInner.pick overrides ResOuter.pick
    SootMethod m = resolve("<ResPick: java.lang.String pick()>");
    assertEquals("ResInner", m.getDeclaringClassType().getClassName());
  }

  @Test
  public void staticAndPrivateInterfaceMethodsAreNotInherited() {
    assertNull(resolve("<ResUtilUser: java.lang.String util()>"));
    assertNull(resolve("<ResUtilUser: java.lang.String hidden()>"));
    // declared in the referenced interface itself
    assertEquals(
        "ResUtil",
        resolve("<ResUtil: java.lang.String util()>").getDeclaringClassType().getClassName());
  }

  @Test
  public void interfaceReferenceResolvesOnlyPublicMethodsOfObject() {
    JavaView withJdk =
        new JavaView(
            Arrays.asList(
                new JavaClassPathAnalysisInputLocation("src/test/resources/memberresolution"),
                new JrtFileSystemAnalysisInputLocation()));
    IdentifierFactory ids = withJdk.getIdentifierFactory();
    Optional<? extends SootMethod> toString =
        MemberResolution.resolveMethod(
            withJdk, ids.parseMethodSignature("<ResOuter: java.lang.String toString()>"));
    assertEquals(
        "java.lang.Object", toString.get().getDeclaringClassType().getFullyQualifiedName());
    // Object.clone is protected
    assertFalse(
        MemberResolution.resolveMethod(
                withJdk, ids.parseMethodSignature("<ResOuter: java.lang.Object clone()>"))
            .isPresent());
  }

  @Test
  public void signaturePolymorphicMethodsMatchAnyDescriptor() {
    JavaView jdk = new JavaView(new JrtFileSystemAnalysisInputLocation());
    IdentifierFactory ids = jdk.getIdentifierFactory();
    SootMethod invokeExact =
        MemberResolution.resolveMethod(
                jdk,
                ids.parseMethodSignature(
                    "<java.lang.invoke.MethodHandle: java.lang.String invokeExact(java.lang.String,int)>"))
            .get();
    assertEquals("invokeExact", invokeExact.getName());
    assertEquals(
        "java.lang.invoke.MethodHandle",
        invokeExact.getDeclaringClassType().getFullyQualifiedName());
    // referenced through a subclass of MethodHandle
    SootMethod invokeBasic =
        MemberResolution.resolveMethod(
                jdk,
                ids.parseMethodSignature(
                    "<java.lang.invoke.DirectMethodHandle: java.lang.Object invokeBasic(java.lang.Object)>"))
            .get();
    assertEquals(
        "java.lang.invoke.MethodHandle",
        invokeBasic.getDeclaringClassType().getFullyQualifiedName());
  }

  @Test
  public void unknownMethodsAndClassesAreNotResolved() {
    assertNull(resolve("<ResChild: void doesNotExist()>"));
    assertNull(resolve("<does.not.Exist: void m()>"));
  }

  @Test
  public void fieldsFromSuperclassesAndInterfaces() {
    assertEquals(
        "ResBase",
        MemberResolution.resolveField(
                view,
                view.getIdentifierFactory()
                    .getFieldSignature(
                        "field",
                        view.getIdentifierFactory().getClassType("ResChild"),
                        "java.lang.String"))
            .get()
            .getDeclaringClassType()
            .getClassName());
    assertEquals(
        "ResIface",
        MemberResolution.resolveField(
                view,
                view.getIdentifierFactory()
                    .getFieldSignature(
                        "CONST", view.getIdentifierFactory().getClassType("ResChild"), "int"))
            .get()
            .getDeclaringClassType()
            .getClassName());
    assertFalse(
        MemberResolution.resolveField(
                view,
                view.getIdentifierFactory()
                    .getFieldSignature(
                        "missing", view.getIdentifierFactory().getClassType("ResChild"), "int"))
            .isPresent());
  }

  /** Selects {@code method} for a receiver of class {@code receiver}, as the declaring class. */
  private String select(String receiver, String method) {
    IdentifierFactory ids = view.getIdentifierFactory();
    SootMethod resolved = view.getMethod(ids.parseMethodSignature(method)).get();
    return MemberResolution.selectMethod(view, ids.getClassType(receiver), resolved)
        .map(m -> m.getDeclaringClassType().getFullyQualifiedName())
        .orElse(null);
  }

  @Test
  public void selectionTakesTheNearestOverride() {
    assertEquals("SelSub", select("SelSubSub", "<SelBase: java.lang.String n()>"));
    assertEquals("SelBase", select("SelBase", "<SelBase: java.lang.String n()>"));
  }

  @Test
  public void privateMethodsDoNotOverride() {
    // SelPrivate.m has the same signature, but SelIface.m runs
    assertEquals("SelIface", select("SelPrivateChild", "<SelIface: java.lang.String m()>"));
  }

  @Test
  public void packagePrivateMethodsAreOnlyOverriddenInTheirPackage() {
    // selq.B.m does not override selp.A.m
    assertEquals("selp.A", select("selq.B", "<selp.A: void m()>"));
    assertEquals("selq.B", select("selq.B", "<selq.B: void m()>"));
    assertEquals("selp.A", select("selq.D", "<selp.A: void m()>"));
    // selq.C.m overrides selp.B1.m, which overrides selp.A.m
    assertEquals("selq.C", select("selq.C", "<selp.A: void m()>"));
  }
}
