package de.upb.swt.soot.test.callgraph.typehierarchy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.callgraph.typehierarchy.MethodDispatchResolver;
import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.common.expr.JSpecialInvokeExpr;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;

public class MethodDispatchResolverTest {

  private View view;
  public static final String jarFile = "../shared-test-resources/Soot-4.0-SNAPSHOT.jar";

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
    Project p =
        JavaProject.builder(new JavaLanguage(8)).addClassPath(analysisInputLocation).build();
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
        "AbstractNamespace.toString() should resolve to Object.toString()",
        factory.parseMethodSignature("java.lang.Object#toString(): java.lang.String"),
        MethodDispatchResolver.resolveConcreteDispatch(
            view,
            factory.parseMethodSignature(
                "de.upb.soot.namespaces.AbstractNamespace#toString(): java.lang.String")));
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
        factory.parseMethodSignature(
            "de.upb.soot.namespaces.JavaClassPathNamespace#explode(java.lang.String): java.util.stream.Stream");
    JSpecialInvokeExpr privateExplodeInvoke =
        new JSpecialInvokeExpr(
            new Local("jcp", factory.getClassType("de.upb.soot.namespaces.JavaClassPathNamespace")),
            privateExplode,
            ImmutableUtils.immutableList(JavaJimple.getInstance().newStringConstant("abc")));
    assertEquals(
        privateExplode + " is private and should resolve to itself",
        privateExplode,
        MethodDispatchResolver.resolveSpecialDispatch(view, privateExplodeInvoke, strToStringSig));
  }
}
