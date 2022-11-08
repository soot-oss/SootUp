package de.upb.sse.sootup.test.java.sourcecode.frontend;

import static de.upb.sse.sootup.core.util.Utils.assertEquiv;
import static de.upb.sse.sootup.core.util.Utils.assertInstanceOfSatisfying;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.sse.sootup.core.jimple.Jimple;
import de.upb.sse.sootup.core.jimple.basic.Local;
import de.upb.sse.sootup.core.jimple.common.ref.JInstanceFieldRef;
import de.upb.sse.sootup.core.jimple.common.stmt.JAssignStmt;
import de.upb.sse.sootup.core.jimple.common.stmt.JIdentityStmt;
import de.upb.sse.sootup.core.jimple.common.stmt.JReturnStmt;
import de.upb.sse.sootup.core.jimple.common.stmt.Stmt;
import de.upb.sse.sootup.core.model.Body;
import de.upb.sse.sootup.core.model.SootMethod;
import de.upb.sse.sootup.core.signatures.FieldSignature;
import de.upb.sse.sootup.core.types.PrimitiveType;
import de.upb.sse.sootup.java.core.JavaIdentifierFactory;
import de.upb.sse.sootup.java.core.types.JavaClassType;
import de.upb.sse.sootup.java.sourcecode.WalaClassLoaderTestUtils;
import de.upb.sse.sootup.java.sourcecode.frontend.WalaJavaClassProvider;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class GetInstructionConversionTest {

  private WalaJavaClassProvider loader;
  private JavaIdentifierFactory typeFactory;
  private JavaClassType declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "../shared-test-resources/wala-tests/";
    loader = new WalaJavaClassProvider(srcDir);
    typeFactory = JavaIdentifierFactory.getInstance();
    declareClassSig = typeFactory.getClassType("alreadywalaunittests.InnerClassAA");
  }

  @Test
  public void test() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            typeFactory.getMethodSignature(
                declareClassSig, "getA_X", "int", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(3, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", typeFactory.getClassType("alreadywalaunittests.InnerClassAA")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("alreadywalaunittests.InnerClassAA")),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", PrimitiveType.getInt()), stmt.getLeftOp());
          assertInstanceOfSatisfying(
              stmt.getRightOp(),
              JInstanceFieldRef.class,
              JFieldRef -> {
                assertEquiv(
                    new Local("r0", typeFactory.getClassType("alreadywalaunittests.InnerClassAA")),
                    JFieldRef.getBase());

                FieldSignature fieldSig = JFieldRef.getFieldSignature();
                assertNotNull(fieldSig);
                assertEquals("a_x", fieldSig.getName());
                Assert.assertEquals(PrimitiveType.getInt(), fieldSig.getType());
                Assert.assertEquals(
                    typeFactory.getClassType("alreadywalaunittests.InnerClassAA"),
                    fieldSig.getDeclClassType());
              });
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$i0", PrimitiveType.getInt()), stmt.getOp()));
  }
}
