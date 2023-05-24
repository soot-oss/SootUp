package sootup.tests;

import static org.junit.Assert.*;

import categories.Java8Test;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.frontend.SootClassSource;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.LocalGenerator;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.DoubleConstant;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootClassSource;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.types.JavaClassType;
import sootup.java.sourcecode.frontend.WalaJavaClassProvider;

/** @author Kaustubh Kelkar updated on 09.07.2020 */
@Category(Java8Test.class)
public class WitherTest {

  private WalaJavaClassProvider loader;
  private JavaIdentifierFactory identifierFactory;
  private JavaClassType declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "../shared-test-resources/selected-java-target/";
    loader = new WalaJavaClassProvider(srcDir);
    identifierFactory = JavaIdentifierFactory.getInstance();
    declareClassSig = identifierFactory.getClassType("BinaryOperations");
  }

  @Test
  public void testWithers() {

    LocalGenerator generator = new LocalGenerator(new HashSet<>());
    Optional<SootClassSource<JavaSootClass>> classSource = loader.getClassSource(declareClassSig);
    assertTrue(classSource.isPresent());
    JavaSootClass sootClass =
        new JavaSootClass((JavaSootClassSource) classSource.get(), SourceType.Application);
    ClassType type = identifierFactory.getClassType("java.lang.String");

    MethodSignature methodSignature =
        identifierFactory.getMethodSignature(
            declareClassSig, "addDouble", "double", Arrays.asList("double", "float"));
    Optional<JavaSootMethod> m = sootClass.getMethod(methodSignature.getSubSignature());
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body.BodyBuilder bodyBuilder = Body.builder();
    final JIdentityStmt firstStmt =
        Jimple.newIdentityStmt(
            generator.generateLocal(declareClassSig),
            Jimple.newParameterRef(declareClassSig, 0),
            StmtPositionInfo.createNoStmtPositionInfo());
    final JReturnStmt jReturnStmt =
        Jimple.newReturnStmt(
            DoubleConstant.getInstance(12.34), StmtPositionInfo.createNoStmtPositionInfo());
    // bodyBuilder.addFlow(firstStmt, jReturnStmt);

    Body body =
        bodyBuilder
            .setMethodSignature(methodSignature)
            .addFlow(firstStmt, jReturnStmt)
            .setStartingStmt(firstStmt)
            .setLocals(generator.getLocals())
            .build();
    assertNotNull(body);

    Local local = (Local) firstStmt.getLeftOp();
    Local newLocal = local.withName("newName");
    final JIdentityStmt firstStmtNew = firstStmt.withLocal(newLocal);

    JavaSootClass newSootClass = sootClass.withReplacedMethod(method, method.withBody(body));

    Optional<JavaSootMethod> newMethod =
        newSootClass.getMethod(method.getSignature().getSubSignature());
    assertTrue(newMethod.isPresent());
    Body newBody = newMethod.get().getBody();
    assertNotNull(newBody);
    assertEquals("newName", ((Local) firstStmtNew.getLeftOp()).getName());
    assertNotEquals("newName1", ((Local) firstStmtNew.getLeftOp()).getName());
  }
}
