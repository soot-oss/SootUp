package sootup.callgraph;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertFalse;

import categories.Java8Test;
import java.util.Collections;
import java.util.Optional;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.IdentifierFactory;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaProject;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

/** @author : Hasitha Rajapakse, Jonas Klauke * */
@Category(Java8Test.class)
public class ConcreteDispatchTest {
  public ClassType getClassType(String className) {
    return view.getIdentifierFactory().getClassType(className);
  }

  private static JavaView view;

  @BeforeClass
  public static void setUp() {
    JavaProject project =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(
                new JavaSourcePathAnalysisInputLocation(
                    Collections.singleton("src/test/resources/callgraph/ConcreteDispatch/")))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    System.getProperty("java.home") + "/lib/rt.jar"))
            .build();
    view = project.createView();
  }

  @Test
  public void invalidResolveConcreteDispatch() {
    IdentifierFactory factory = view.getIdentifierFactory();
    Optional<MethodSignature> ms =
        AbstractCallGraphAlgorithm.resolveConcreteDispatch(
            view, factory.parseMethodSignature("java.util.Collection#size(): int"));
    assertFalse(ms.isPresent());
  }

  @Test()
  public void invalidResolveConcreteDispatchOfAbstractMethod() {
    IdentifierFactory factory = view.getIdentifierFactory();
    Optional<MethodSignature> ms =
        AbstractCallGraphAlgorithm.resolveConcreteDispatch(
            view,
            factory.parseMethodSignature("java.util.AbstractList#get(int): java.lang.Object"));
    assertFalse(ms.isPresent());
  }

  @Test
  public void testResolveOfANotImplementedMethodInAbstractClass() {
    IdentifierFactory factory = view.getIdentifierFactory();
    Optional<MethodSignature> emptySig =
        AbstractCallGraphAlgorithm.resolveConcreteDispatch(
            view,
            factory.parseMethodSignature(
                "com.sun.java.util.jar.pack.ConstantPool$LiteralEntry#equals(java.lang.Object): boolean"));
    assertFalse(emptySig.isPresent());
  }

  @Test
  public void resolveConcreteDispatch() {
    IdentifierFactory factory = view.getIdentifierFactory();
    MethodSignature strToStringSig =
        factory.parseMethodSignature("java.lang.String#toString(): java.lang.String");

    MethodSignature concreteMethodSig =
        AbstractCallGraphAlgorithm.resolveConcreteDispatch(view, strToStringSig).orElse(null);
    Assert.assertNotNull(concreteMethodSig);
    Assert.assertEquals(
        "String.toString() should resolve to itself", strToStringSig, concreteMethodSig);

    MethodSignature concreteMethodSig2 =
        AbstractCallGraphAlgorithm.resolveConcreteDispatch(
                view, factory.parseMethodSignature("A#hashCode(): int"))
            .orElse(null);
    Assert.assertNotNull(concreteMethodSig2);
    Assert.assertEquals(
        "A.hashCode() should resolve to java.lang.Object.hashCode()",
        factory.parseMethodSignature("java.lang.Object#hashCode(): int"),
        concreteMethodSig2);
  }

  @Test
  public void method() {
    // test concrete method in super class
    ClassType sootClassTypeA = getClassType("A");
    ClassType sootClassTypeB = getClassType("B");

    IdentifierFactory identifierFactory = view.getIdentifierFactory();
    MethodSignature sootMethod1 =
        identifierFactory.getMethodSignature(
            sootClassTypeA, "method", "void", Collections.emptyList());
    MethodSignature sootMethod2 =
        identifierFactory.getMethodSignature(
            sootClassTypeA, "method2", "void", Collections.emptyList());
    MethodSignature sootMethod3 =
        identifierFactory.getMethodSignature(
            sootClassTypeB, "method2", "void", Collections.emptyList());

    MethodSignature candidate1 =
        AbstractCallGraphAlgorithm.resolveConcreteDispatch(view, sootMethod1).orElse(null);
    assertNotNull(candidate1);
    assertEquals(candidate1, sootMethod1);

    MethodSignature candidate2 =
        AbstractCallGraphAlgorithm.resolveConcreteDispatch(view, sootMethod2).orElse(null);
    assertNotNull(candidate2);
    assertEquals(candidate2, sootMethod3);

    // test concrete method in interface
    ClassType sootClassInterfaceI = getClassType("I");
    MethodSignature sootInterfaceMethod =
        identifierFactory.getMethodSignature(
            sootClassInterfaceI, "interfaceMethod", "void", Collections.emptyList());

    MethodSignature sootInterfaceMethodA =
        identifierFactory.getMethodSignature(
            sootClassTypeA, "interfaceMethod", "void", Collections.emptyList());

    MethodSignature candidateInterface =
        AbstractCallGraphAlgorithm.resolveConcreteDispatch(view, sootInterfaceMethodA).orElse(null);
    assertNotNull(candidateInterface);
    assertEquals(candidateInterface, sootInterfaceMethod);

    // test concrete method in super-interface
    ClassType sootClassInterfaceJ = getClassType("J");
    MethodSignature sootSuperInterfaceMethod =
        identifierFactory.getMethodSignature(
            sootClassInterfaceJ, "superInterfaceMethod", "void", Collections.emptyList());

    MethodSignature sootSuperInterfaceMethodA =
        identifierFactory.getMethodSignature(
            sootClassTypeA, "superInterfaceMethod", "void", Collections.emptyList());

    MethodSignature candidateSuperInterface =
        AbstractCallGraphAlgorithm.resolveConcreteDispatch(view, sootSuperInterfaceMethodA)
            .orElse(null);
    assertNotNull(candidateSuperInterface);
    assertEquals(candidateSuperInterface, sootSuperInterfaceMethod);

    // test concrete method with two possible default methods but one is in the sub-interface
    ClassType sootClassTypeC = getClassType("C");
    ClassType sootClassTypeD = getClassType("D");
    MethodSignature subInterfaceMethod =
        identifierFactory.getMethodSignature(
            sootClassInterfaceI, "interfaceMethod", "void", Collections.emptyList());

    MethodSignature sootSuperInterfaceMethodD =
        identifierFactory.getMethodSignature(
            sootClassTypeD, "interfaceMethod", "void", Collections.emptyList());

    MethodSignature sootSuperInterfaceMethodC =
        identifierFactory.getMethodSignature(
            sootClassTypeC, "interfaceMethod", "void", Collections.emptyList());

    MethodSignature candidateSubInterface =
        AbstractCallGraphAlgorithm.resolveConcreteDispatch(view, sootSuperInterfaceMethodD)
            .orElse(null);
    assertNotNull(candidateSubInterface);
    assertEquals(candidateSubInterface, subInterfaceMethod);
    MethodSignature candidateSubInterface2 =
        AbstractCallGraphAlgorithm.resolveConcreteDispatch(view, sootSuperInterfaceMethodC)
            .orElse(null);
    assertNotNull(candidateSubInterface2);
    assertEquals(candidateSubInterface, candidateSubInterface2);
  }
}
