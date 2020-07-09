package de.upb.swt.soot.test;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.frontend.SootClassSource;
import de.upb.swt.soot.core.graph.ImmutableStmtGraph;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.LocalGenerator;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.stmt.JIdentityStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JReturnStmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.JavaSootClassSource;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.sourcecode.frontend.WalaJavaClassProvider;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
    Optional<SootClassSource> classSource = loader.getClassSource(declareClassSig);
    assertTrue(classSource.isPresent());
    JavaSootClass sootClass =
        new JavaSootClass((JavaSootClassSource) classSource.get(), SourceType.Application);
    ClassType type = identifierFactory.getClassType("java.lang.String");

    MethodSignature methodSignature =
        identifierFactory.getMethodSignature(
            "addByte", declareClassSig, "byte", Arrays.asList("byte", "byte"));
    Optional<SootMethod> m = sootClass.getMethod(methodSignature);
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body.BodyBuilder bodyBuilder = Body.builder();
    final JIdentityStmt firstStmt =
        Jimple.newIdentityStmt(
            generator.generateLocal(declareClassSig),
            Jimple.newParameterRef(declareClassSig, 0),
            StmtPositionInfo.createNoStmtPositionInfo());
    bodyBuilder.addStmt(firstStmt, false);
    final JReturnStmt jReturnStmt =
        Jimple.newReturnStmt(
            IntConstant.getInstance(56), StmtPositionInfo.createNoStmtPositionInfo());
    bodyBuilder.addStmt(jReturnStmt);
    bodyBuilder.addFlow(firstStmt, jReturnStmt);

    Body body =
        bodyBuilder
            .setMethodSignature(methodSignature)
            .setFirstStmt(firstStmt)
            .setLocals(generator.getLocals())
            .build();
    assertNotNull(body);

    ImmutableStmtGraph immutableGraph =
        body.getStmtGraph(); // TODO [kk] How do we make use of immutable graph here?
    JIdentityStmt jIdentityStmt = (JIdentityStmt) immutableGraph.getEntryPoint();
    Local local = (Local) firstStmt.getLeftOp();
    Local newLocal = local.withName("newName");
    final JIdentityStmt firstStmtNew = firstStmt.withLocal(newLocal);

    JavaSootClass newSootClass = sootClass.withReplacedMethod(method, method.withBody(body));

    Optional<SootMethod> newMethod = newSootClass.getMethod(method.getSignature());
    assertTrue(newMethod.isPresent());
    Body newBody = newMethod.get().getBody();
    assertNotNull(newBody);
    ImmutableStmtGraph immutableGraphNew = newBody.getStmtGraph();
    assertEquals("newName", ((Local) firstStmtNew.getLeftOp()).getName());
    assertNotEquals("newName1", ((Local) firstStmtNew.getLeftOp()).getName());
  }
}
