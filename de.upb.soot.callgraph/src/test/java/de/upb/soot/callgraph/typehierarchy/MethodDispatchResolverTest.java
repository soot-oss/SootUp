package de.upb.soot.callgraph.typehierarchy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.upb.soot.core.IdentifierFactory;
import de.upb.soot.core.Project;
import de.upb.soot.core.frontend.ResolveException;
import de.upb.soot.javabytecodefrontend.frontend.AsmJavaClassProvider;
import de.upb.soot.javabytecodefrontend.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.soot.core.jimple.basic.Local;
import de.upb.soot.core.jimple.common.constant.StringConstant;
import de.upb.soot.core.jimple.common.expr.JSpecialInvokeExpr;
import de.upb.soot.core.signatures.MethodSignature;
import de.upb.soot.core.util.ImmutableUtils;
import de.upb.soot.core.views.View;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;

public class MethodDispatchResolverTest {

  private View view;
  // TODO: hacky.. check for better way to access the test jar from other module
  public static final String jarFile =
      "../de.upb.soot.javabytecodefrontend/target/test-classes/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar";

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
        new JavaClassPathAnalysisInputLocation(
            jarFile + File.pathSeparator + rtJarClassPath, new AsmJavaClassProvider());
    Project<JavaClassPathAnalysisInputLocation> p = new Project<>(analysisInputLocation);
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
            ImmutableUtils.immutableList(StringConstant.getInstance("abc")));

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
            ImmutableUtils.immutableList(StringConstant.getInstance("abc")));
    assertEquals(
        privateExplode + " is private and should resolve to itself",
        privateExplode,
        MethodDispatchResolver.resolveSpecialDispatch(view, privateExplodeInvoke, strToStringSig));
  }
}
