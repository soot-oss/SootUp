package sootup.java.core.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.graph.MutableStmtGraph;
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

    MutableStmtGraph stmtGraph = bodyBuilder.getStmtGraph();
    stmtGraph.setStartingStmt(firstStmt);
    stmtGraph.putEdge(firstStmt, returnVoidStmt);

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

    JavaSootClass mainClass =
        new JavaSootClass(
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
                Collections.emptyList()),
            SourceType.Application);

    assertEquals(mainClass.getMethods().size(), 1);

    assertTrue(
        mainClass
            .getMethod(methodSignature.getSubSignature())
            .orElseThrow(() -> new RuntimeException("Failed getting method " + methodSignature))
            .hasBody());
  }

  @Test
  public void constructorTest() {
    IdentifierFactory idf = JavaIdentifierFactory.getInstance();

    SootMethod mockedConstructor = spy(new SootMethod.SootMethodBuilder().build());
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
