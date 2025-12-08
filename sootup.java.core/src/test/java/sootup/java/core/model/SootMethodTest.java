package sootup.java.core.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.Test;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.LocalGenerator;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.model.*;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
import sootup.core.util.Utils;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.OverridingJavaClassSource;
import sootup.java.core.views.JavaView;

/**
 * @author Linghui Luo
 * @author Kaustubh Kelkar updated on 02.07.2020
 */
public class SootMethodTest {

  @Test
  public void testCreateMethod() {
    JavaView view = new JavaView(Collections.singletonList(new EagerInputLocation()));
    ClassType type = view.getIdentifierFactory().getClassType("java.lang.String");

    LocalGenerator generator = new LocalGenerator(new HashSet<>());
    MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature("dummyMain", "main", "void", Collections.emptyList());
    Body.BodyBuilder bodyBuilder = Body.builder();

    final JIdentityStmt firstStmt =
        Jimple.newIdentityStmt(
            generator.generateLocal(type),
            Jimple.newParameterRef(type, 0),
            StmtPositionInfo.getNoStmtPositionInfo());
    final JReturnVoidStmt returnVoidStmt =
        new JReturnVoidStmt(StmtPositionInfo.getNoStmtPositionInfo());

    MutableControlFlowGraph controlFlowGraph = bodyBuilder.getControlFlowGraph();
    controlFlowGraph.setStartingStmt(firstStmt);
    controlFlowGraph.putEdge(firstStmt, returnVoidStmt);

    Body body =
        bodyBuilder.setMethodSignature(methodSignature).setLocals(generator.getLocals()).build();
    assertEquals(1, body.getLocalCount());

    JavaSootMethod dummyMainMethod =
        new JavaSootMethod(
            new OverridingBodySource(methodSignature, body),
            methodSignature,
            EnumSet.of(MethodModifier.PUBLIC, MethodModifier.STATIC),
            Collections.emptyList(),
            Collections.emptyList(),
            NoPositionInformation.getInstance());

    OverridingJavaClassSource overridingJavaClassSource =
        new OverridingJavaClassSource(
            new EagerInputLocation(),
            null,
            view.getIdentifierFactory().getClassType("dummyMain"),
            null,
            Collections.emptySet(),
            null,
            Collections.emptySet(),
            Collections.singleton(dummyMainMethod),
            NoPositionInformation.getInstance(),
            EnumSet.of(ClassModifier.PUBLIC),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList());
    JavaSootClass mainClass = new JavaSootClass(overridingJavaClassSource, SourceType.Application);

    JavaSootClass javaSootClassUsingBuilder =
        JavaSootClass.JavaSootClassBuilder.builder()
            .withClassSource(overridingJavaClassSource)
            .withSourceType(null)
            .withMethods(mainClass.getMethods())
            .withFields(mainClass.getFields())
            .withModifiers(mainClass.getModifiers())
            .withInterfaces(mainClass.getInterfaces())
            .withSuperclass(mainClass.getSuperclass())
            .withOuterClass(mainClass.getOuterClass())
            .withPosition(mainClass.getPosition())
            .build();

    assertFalse(
        javaSootClassUsingBuilder.implementsInterface(
            view.getIdentifierFactory().getClassType("dummyMainInterface")));
    assertFalse(javaSootClassUsingBuilder.hasOuterClass());
    assertEquals(javaSootClassUsingBuilder.getOuterClass(), Optional.empty());
    assertFalse(javaSootClassUsingBuilder.isInnerClass());
    assertTrue(javaSootClassUsingBuilder.isPublic());
    assertEquals("dummyMain", javaSootClassUsingBuilder.toString());
    assertEquals(
        Arrays.asList(
            "public static void main()",
            "java.lang.String r0",
            "r0 := @parameter0: java.lang.String",
            "return"),
        Utils.filterJimple(javaSootClassUsingBuilder.print()));
    assertEquals(1, javaSootClassUsingBuilder.getMethods().size());
    assertFalse(javaSootClassUsingBuilder.isPrivate());
    assertFalse(javaSootClassUsingBuilder.isProtected());
    assertFalse(javaSootClassUsingBuilder.isStatic());
    assertEquals("dummyMain", javaSootClassUsingBuilder.getName());
    assertTrue(
        javaSootClassUsingBuilder
            .getMethod(methodSignature.getSubSignature())
            .orElseThrow(() -> new RuntimeException("Failed getting method " + methodSignature))
            .hasBody());
  }

  @Test
  public void constructorTest() {
    IdentifierFactory idf = JavaIdentifierFactory.getInstance();

    SootMethod mockedConstructor = spy(JavaSootMethod.JavaSootMethodBuilder.builder().build());
    MethodSignature mockedSignature = mock(MethodSignature.class);
    MethodSubSignature mockedSubSignature = mock(MethodSubSignature.class);

    when(mockedSubSignature.getName()).thenReturn("<init>");
    when(mockedSubSignature.getType()).thenReturn(VoidType.getInstance());
    when(mockedSignature.getSubSignature()).thenReturn(mockedSubSignature);
    when(mockedConstructor.getSignature()).thenReturn(mockedSignature);
    when(mockedConstructor.getParameterCount()).thenReturn(0);

    assertTrue(idf.isConstructorSubSignature(mockedSubSignature));
    assertTrue(idf.isConstructorSignature(mockedSignature));

    assertTrue(mockedConstructor.isConstructor(idf));
    assertTrue(mockedConstructor.isDefaultConstructor(idf));

    when(mockedConstructor.getParameterCount()).thenReturn(1);
    assertFalse(mockedConstructor.isDefaultConstructor(idf));

    when(mockedSubSignature.getName()).thenReturn("method");
  }
}
