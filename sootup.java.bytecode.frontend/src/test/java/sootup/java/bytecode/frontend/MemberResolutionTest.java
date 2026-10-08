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
}
