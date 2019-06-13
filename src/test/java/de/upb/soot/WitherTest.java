package de.upb.soot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import de.upb.soot.core.Body;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootMethod;
import de.upb.soot.core.SourceType;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.java.WalaClassLoader;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.common.stmt.JIdentityStmt;
import de.upb.soot.jimple.common.stmt.Stmt;
import de.upb.soot.types.JavaClassType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class WitherTest {
  private WalaClassLoader loader;
  private DefaultIdentifierFactory identifierFactory;
  private JavaClassType declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "src/test/resources/selected-java-target/";
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
              Set<SootMethod> newMethods = new HashSet<>(sootClass.getMethods());
              newMethods.remove(method);
              SootMethod newMethod =
                  method.withOverridingMethodSource(
                      overridingMethodSource -> {
                        List<Stmt> newStmts = new ArrayList<>(body.getStmts());
                        JIdentityStmt stmt = (JIdentityStmt) newStmts.get(0);
                        Local local = (Local) stmt.getLeftOp();
                        Local newLocal = local.withName("newName");
                        Stmt newStmt = stmt.withLocal(newLocal);
                        newStmts.set(0, newStmt);
                        Body newBody = body.withStmts(newStmts);
                        return overridingMethodSource.withBody(newBody);
                      });
              newMethods.add(newMethod);
              return overridingClassSource.withMethods(newMethods);
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
