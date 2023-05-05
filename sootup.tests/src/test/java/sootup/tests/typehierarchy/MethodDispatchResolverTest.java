package sootup.tests.typehierarchy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.ResolveException;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.MethodDispatchResolver;
import sootup.core.types.ClassType;
import sootup.core.util.ImmutableUtils;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaProject;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;

/** @author Kaustubh Kelkar update on 22.04.2020 */
@Category(Java8Test.class)
public class MethodDispatchResolverTest {

  private JavaView view;
  public static final String jarFile = "../shared-test-resources/java-miniapps/MiniApp.jar";

  @Before
  public void setUp() {
    assertTrue("File " + jarFile + " not found.", new File(jarFile).exists());
    String currentClassPath =
        System.getProperty("java.class.path")
            + File.pathSeparator
            + ManagementFactory.getRuntimeMXBean().getBootClassPath();
    String rtJarClassPath =
        Arrays.stream(currentClassPath.split(File.pathSeparator))
            .filter(pathEntry -> pathEntry.endsWith(File.separator + "rt.jar"))
            .distinct()
            .collect(Collectors.joining(File.pathSeparator));
    JavaClassPathAnalysisInputLocation analysisInputLocation =
        new JavaClassPathAnalysisInputLocation(jarFile + File.pathSeparator + rtJarClassPath);
    JavaProject p =
        JavaProject.builder(new JavaLanguage(8)).addInputLocation(analysisInputLocation).build();
    view = p.createView();
  }

  @Test
  public void resolveAbstractDispatch() {
    IdentifierFactory factory = view.getIdentifierFactory();
    MethodSignature collectionSize =
        factory.parseMethodSignature("java.util.Collection#size(): int");
    MethodSignature abstractListSize =
        factory.parseMethodSignature("java.util.AbstractList#size(): int");
    MethodSignature setSize = factory.parseMethodSignature("java.util.HashSet#size(): int");
    MethodSignature listSize = factory.parseMethodSignature("java.util.ArrayList#size(): int");
    MethodSignature enumSetClone =
        factory.parseMethodSignature("java.util.EnumSet#clone(): java.lang.Object");
    MethodSignature objectClone =
        factory.parseMethodSignature("java.lang.Object#clone(): java.lang.Object");
    MethodSignature arrayDequeueClone =
        factory.parseMethodSignature("java.util.ArrayDeque#clone(): java.util.ArrayDequeue");

    Set<MethodSignature> candidates =
        MethodDispatchResolver.resolveAbstractDispatch(view, collectionSize);
    assertTrue(collectionSize + " can resolve to " + setSize, candidates.contains(setSize));
    assertTrue(collectionSize + " can resolve to " + listSize, candidates.contains(listSize));

    assertTrue(
        abstractListSize + " can resolve to " + listSize,
        MethodDispatchResolver.resolveAbstractDispatch(view, abstractListSize).contains(listSize));

    assertTrue(
        objectClone + " can resolve to " + enumSetClone,
        MethodDispatchResolver.resolveAbstractDispatch(view, objectClone).contains(enumSetClone));
    assertFalse(
        arrayDequeueClone + " cannot resolve to " + enumSetClone,
        MethodDispatchResolver.resolveAbstractDispatch(view, arrayDequeueClone)
            .contains(enumSetClone));
  }

  @Test
  public void testResolveAllDispatchesInClasses() {
    IdentifierFactory factory = view.getIdentifierFactory();
    MethodSignature collectionSize =
        factory.parseMethodSignature("java.util.Collection#size(): int");
    MethodSignature abstractListSize =
        factory.parseMethodSignature("java.util.AbstractList#size(): int");
    MethodSignature setSize = factory.parseMethodSignature("java.util.HashSet#size(): int");
    MethodSignature listSize = factory.parseMethodSignature("java.util.ArrayList#size(): int");

    Set<ClassType> classes = new HashSet<>();
    classes.add(factory.getClassType("java.util.HashSet"));
    classes.add(factory.getClassType("java.util.ArrayList"));
    Set<MethodSignature> candidates =
        MethodDispatchResolver.resolveAllDispatchesInClasses(view, collectionSize, classes);
    assertTrue(collectionSize + " can resolve to " + setSize, candidates.contains(setSize));
    assertTrue(collectionSize + " can resolve to " + listSize, candidates.contains(listSize));
    assertFalse(
        collectionSize + " can resolve to " + abstractListSize,
        candidates.contains(abstractListSize));
  }

  @Test
  public void testCanDispatch() {
    IdentifierFactory factory = view.getIdentifierFactory();
    MethodSignature collectionSize =
        factory.parseMethodSignature("java.util.Collection#size(): int");
    MethodSignature setSize = factory.parseMethodSignature("java.util.HashSet#size(): int");
    assertTrue(
        MethodDispatchResolver.canDispatch(collectionSize, setSize, view.getTypeHierarchy()));

    MethodSignature objectClone =
        factory.parseMethodSignature("java.lang.Object#clone(): java.lang.Object");
    MethodSignature arrayDequeueClone =
        factory.parseMethodSignature("java.util.ArrayDeque#clone(): java.util.ArrayDequeue");
    assertTrue(
        MethodDispatchResolver.canDispatch(
            objectClone, arrayDequeueClone, view.getTypeHierarchy()));

    MethodSignature collectionHashCode =
        factory.parseMethodSignature("java.util.Collection#hasCode(): int");
    assertFalse(
        MethodDispatchResolver.canDispatch(
            collectionSize, collectionHashCode, view.getTypeHierarchy()));

    MethodSignature objectWait1Param =
        factory.parseMethodSignature("java.lang.Object#wait(long): void");
    MethodSignature objectWait2Param =
        factory.parseMethodSignature("java.util.Collection#hasCode(long,int): int");
    assertFalse(
        MethodDispatchResolver.canDispatch(
            objectWait1Param, objectWait2Param, view.getTypeHierarchy()));
  }

  @Test(expected = ResolveException.class)
  public void invalidResolveConcreteDispatch() {
    IdentifierFactory factory = view.getIdentifierFactory();
    MethodDispatchResolver.resolveConcreteDispatch(
        view, factory.parseMethodSignature("java.util.Collection#size(): int"));
  }

  @Test(expected = ResolveException.class)
  public void invalidResolveConcreteDispatchOfAbstractMethod() {
    IdentifierFactory factory = view.getIdentifierFactory();
    MethodDispatchResolver.resolveConcreteDispatch(
        view, factory.parseMethodSignature("java.util.AbstractList#get(int): java.lang.Object"));
  }

  @Test
  public void testResolveOfANotImplementedMethodInAbstractClass() {
    IdentifierFactory factory = view.getIdentifierFactory();
    Optional<MethodSignature> emptySig =
        MethodDispatchResolver.resolveConcreteDispatch(
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
        MethodDispatchResolver.resolveConcreteDispatch(view, strToStringSig).orElse(null);
    assertNotNull(concreteMethodSig);
    assertEquals("String.toString() should resolve to itself", strToStringSig, concreteMethodSig);

    MethodSignature concreteMethodSig2 =
        MethodDispatchResolver.resolveConcreteDispatch(
                view, factory.parseMethodSignature("ds.AbstractDataStrcture#hashCode(): int"))
            .orElse(null);
    assertNotNull(concreteMethodSig2);
    assertEquals(
        "ds.AbstractDataStrcture.hashCode() should resolve to java.lang.Object.hashCode()",
        factory.parseMethodSignature("java.lang.Object#hashCode(): int"),
        concreteMethodSig2);
  }

  @Test
  public void resolveSpecialDispatch() {
    IdentifierFactory factory = view.getIdentifierFactory();
    MethodSignature strInit =
        factory.parseMethodSignature("java.lang.String#<init>(java.lang.String): void");
    MethodSignature strToStringSig =
        factory.parseMethodSignature("java.lang.String#toString(): java.lang.String");

    JSpecialInvokeExpr strInitInvoke =
        new JSpecialInvokeExpr(
            new Local("str", factory.getClassType("java.lang.String")),
            strInit,
            ImmutableUtils.immutableList(JavaJimple.getInstance().newStringConstant("abc")));

    assertEquals(
        "String init should resolve to itself",
        strInit,
        MethodDispatchResolver.resolveSpecialDispatch(view, strInitInvoke, strToStringSig));

    MethodSignature privateExplode =
        factory.parseMethodSignature("ds.Employee#setEmpName(java.lang.String): java.lang.String");
    JSpecialInvokeExpr privateExplodeInvoke =
        new JSpecialInvokeExpr(
            new Local("jcp", factory.getClassType("ds.Employee")),
            privateExplode,
            ImmutableUtils.immutableList(JavaJimple.getInstance().newStringConstant("abc")));
    assertEquals(
        privateExplode + " is private and should resolve to itself",
        privateExplode,
        MethodDispatchResolver.resolveSpecialDispatch(view, privateExplodeInvoke, strToStringSig));
  }
}
