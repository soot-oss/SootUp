package de.upb.swt.soot.test.typehierarchy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.common.expr.JSpecialInvokeExpr;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.typerhierachy.MethodDispatchResolver;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.views.JavaView;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
    view = p.createOnDemandView();
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
        factory.parseMethodSignature("java.util.EnumSet#clone(): java.util.EnumSet");
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

  @Test(expected = ResolveException.class)
  public void invalidResolveConcreteDispatch() {
    IdentifierFactory factory = view.getIdentifierFactory();
    MethodDispatchResolver.resolveConcreteDispatch(
        view, factory.parseMethodSignature("java.util.Collection#size(): int"));
  }

  @Test
  public void resolveConcreteDispatch() {
    IdentifierFactory factory = view.getIdentifierFactory();
    MethodSignature strToStringSig =
        factory.parseMethodSignature("java.lang.String#toString(): java.lang.String");
    assertEquals(
        "String.toString() should resolve to itself",
        strToStringSig,
        MethodDispatchResolver.resolveConcreteDispatch(view, strToStringSig));
    assertEquals(
        "AbstractDataStructure.toString() should resolve to Object.toString()",
        factory.parseMethodSignature("java.lang.Object#toString(): java.lang.String"),
        MethodDispatchResolver.resolveConcreteDispatch(
            view,
            factory.parseMethodSignature("ds.AbstractDataStrcture#toString(): java.lang.String")));
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
