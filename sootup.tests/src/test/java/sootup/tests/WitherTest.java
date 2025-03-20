package sootup.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.LocalGenerator;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.DoubleConstant;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootClassSource;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

/**
 * @author Kaustubh Kelkar updated on 09.07.2020
 */
public class WitherTest {

  private JavaView loader;
  private JavaIdentifierFactory identifierFactory;
  private JavaClassType declareClassSig;

  @BeforeEach
  public void loadClassesWithWala() {
    String classPath = "src/test/resources/wither/binary";
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(new DefaultRuntimeAnalysisInputLocation());
    inputLocations.add(new JavaClassPathAnalysisInputLocation(classPath));
    loader = new JavaView(inputLocations);
    identifierFactory = loader.getIdentifierFactory();
    declareClassSig = identifierFactory.getClassType("BinaryOperations");
  }

  @Test
  public void testWithers() {

    LocalGenerator generator = new LocalGenerator(new HashSet<>());
    JavaSootClass clazz = loader.getClass(declareClassSig).orElse(null);
    assertNotNull(clazz);
    JavaSootClassSource classSource = clazz.getClassSource();
    JavaSootClass sootClass = new JavaSootClass(classSource, SourceType.Application);

    MethodSignature methodSignature =
        identifierFactory.getMethodSignature(
            declareClassSig, "addDouble", "double", Arrays.asList("double", "float"));
    Optional<JavaSootMethod> m = sootClass.getMethod(methodSignature.getSubSignature());
    assertTrue(m.isPresent());
    JavaSootMethod method = m.get();

    Body.BodyBuilder bodyBuilder = Body.builder();
    final JIdentityStmt firstStmt =
        Jimple.newIdentityStmt(
            generator.generateLocal(declareClassSig),
            Jimple.newParameterRef(declareClassSig, 0),
            StmtPositionInfo.getNoStmtPositionInfo());
    final JReturnStmt jReturnStmt =
        Jimple.newReturnStmt(
            DoubleConstant.getInstance(12.34), StmtPositionInfo.getNoStmtPositionInfo());
    // bodyBuilder.addFlow(firstStmt, jReturnStmt);

    MutableStmtGraph stmtGraph = bodyBuilder.getStmtGraph();
    stmtGraph.setStartingStmt(firstStmt);
    stmtGraph.putEdge(firstStmt, jReturnStmt);
    Body body =
        bodyBuilder.setMethodSignature(methodSignature).setLocals(generator.getLocals()).build();
    assertNotNull(body);

    Local local = firstStmt.getLeftOp();
    Local newLocal = local.withName("newName");
    final JIdentityStmt firstStmtNew = firstStmt.withLocal(newLocal);

    JavaSootClass newSootClass = sootClass.withReplacedMethod(method, method.withBody(body));

    Optional<JavaSootMethod> newMethod =
        newSootClass.getMethod(method.getSignature().getSubSignature());
    assertTrue(newMethod.isPresent());
    Body newBody = newMethod.get().getBody();
    assertNotNull(newBody);
    assertEquals("newName", firstStmtNew.getLeftOp().getName());
    assertNotEquals("newName1", firstStmtNew.getLeftOp().getName());
  }
}
