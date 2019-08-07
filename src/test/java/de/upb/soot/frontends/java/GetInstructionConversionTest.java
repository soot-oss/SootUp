package de.upb.soot.frontends.java;

import static de.upb.soot.frontends.java.Utils.assertEquiv;
import static de.upb.soot.frontends.java.Utils.assertInstanceOfSatisfying;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.soot.DefaultIdentifierFactory;
import de.upb.soot.core.Body;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.common.ref.JInstanceFieldRef;
import de.upb.soot.jimple.common.stmt.JAssignStmt;
import de.upb.soot.jimple.common.stmt.JIdentityStmt;
import de.upb.soot.jimple.common.stmt.JReturnStmt;
import de.upb.soot.jimple.common.stmt.Stmt;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.PrimitiveType;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class GetInstructionConversionTest {

  private WalaClassLoader loader;
  private DefaultIdentifierFactory typeFactory;
  private JavaClassType declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "src/test/resources/wala-tests/";
    loader = new WalaClassLoader(srcDir, null);
    typeFactory = DefaultIdentifierFactory.getInstance();
    declareClassSig = typeFactory.getClassType("alreadywalaunittests.InnerClassAA");
  }

  @Test
  public void test() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            typeFactory.getMethodSignature(
                "getA_X", declareClassSig, "int", Collections.emptyList()));
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
              fieldRef -> {
                assertEquiv(
                    new Local("r0", typeFactory.getClassType("alreadywalaunittests.InnerClassAA")),
                    fieldRef.getBase());

                FieldSignature fieldSig = fieldRef.getFieldSignature();
                assertNotNull(fieldSig);
                assertEquals("a_x", fieldSig.getName());
                assertEquals(PrimitiveType.getInt(), fieldSig.getSignature());
                assertEquals(
                    typeFactory.getClassType("alreadywalaunittests.InnerClassAA"),
                    fieldSig.getDeclClassSignature());
              });
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$i0", PrimitiveType.getInt()), stmt.getOp()));
  }
}
