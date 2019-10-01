package de.upb.soot.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.DefaultIdentifierFactory;
import de.upb.swt.soot.core.frontend.ClassSource;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.common.stmt.JIdentityStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.types.JavaClassType;
import de.upb.swt.soot.java.sourcecode.frontend.WalaClassLoader;

import java.util.Arrays;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class WitherTest {

  private WalaClassLoader loader;
  private DefaultIdentifierFactory identifierFactory;
  private JavaClassType declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "../shared-test-resources/selected-java-target/";
    loader = new WalaClassLoader(srcDir, null);
    identifierFactory = DefaultIdentifierFactory.getInstance();
    declareClassSig = identifierFactory.getClassType("BinaryOperations");
  }

  @Test
  public void testWithers() {
    Optional<ClassSource> classSource = loader.getClassSource(declareClassSig);
    assertTrue(classSource.isPresent());
    SootClass sootClass = new SootClass(classSource.get(), SourceType.Application);

    Optional<SootMethod> m =
        sootClass.getMethod(
            identifierFactory.getMethodSignature(
                "addByte", declareClassSig, "byte", Arrays.asList("byte", "byte")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    // Let's change a name of a variable deep down in the body of a method of a class
    SootClass newSootClass =
        sootClass.withOverridingClassSource(
            overridingClassSource -> {
              SootMethod newMethod =
                  method.withOverridingMethodSource(
                      methodSource -> {
                        JIdentityStmt stmt = (JIdentityStmt) body.getStmts().get(0);
                        Local local = (Local) stmt.getLeftOp();
                        Local newLocal = local.withName("newName");
                        Stmt newStmt = stmt.withLocal(newLocal);

                        return methodSource.withBodyStmts(newStmts -> newStmts.set(0, newStmt));
                      });
              return overridingClassSource.withReplacedMethod(method, newMethod);
            });

    Optional<SootMethod> newM = newSootClass.getMethod(method.getSignature());
    assertTrue(newM.isPresent());
    Body newBody = newM.get().getBody();
    assertNotNull(newBody);
    JIdentityStmt newJIdentityStmt = (JIdentityStmt) newBody.getStmts().get(0);
    assertEquals("newName", ((Local) newJIdentityStmt.getLeftOp()).getName());

    assertNotEquals(
        "newName", ((Local) ((JIdentityStmt) body.getStmts().get(0)).getLeftOp()).getName());
  }
}
