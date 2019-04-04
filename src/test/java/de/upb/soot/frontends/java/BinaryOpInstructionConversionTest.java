package de.upb.soot.frontends.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.soot.core.Body;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.EquivTo;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.common.constant.IntConstant;
import de.upb.soot.jimple.common.expr.JAddExpr;
import de.upb.soot.jimple.common.expr.JAndExpr;
import de.upb.soot.jimple.common.expr.JCastExpr;
import de.upb.soot.jimple.common.expr.JDivExpr;
import de.upb.soot.jimple.common.expr.JEqExpr;
import de.upb.soot.jimple.common.expr.JMulExpr;
import de.upb.soot.jimple.common.expr.JNeExpr;
import de.upb.soot.jimple.common.expr.JNegExpr;
import de.upb.soot.jimple.common.expr.JOrExpr;
import de.upb.soot.jimple.common.expr.JRemExpr;
import de.upb.soot.jimple.common.expr.JShlExpr;
import de.upb.soot.jimple.common.expr.JShrExpr;
import de.upb.soot.jimple.common.expr.JSubExpr;
import de.upb.soot.jimple.common.expr.JUshrExpr;
import de.upb.soot.jimple.common.expr.JXorExpr;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.stmt.JAssignStmt;
import de.upb.soot.jimple.common.stmt.JGotoStmt;
import de.upb.soot.jimple.common.stmt.JIdentityStmt;
import de.upb.soot.jimple.common.stmt.JIfStmt;
import de.upb.soot.jimple.common.stmt.JReturnStmt;
import de.upb.soot.jimple.common.type.BooleanType;
import de.upb.soot.jimple.common.type.ByteType;
import de.upb.soot.jimple.common.type.CharType;
import de.upb.soot.jimple.common.type.DoubleType;
import de.upb.soot.jimple.common.type.FloatType;
import de.upb.soot.jimple.common.type.IntType;
import de.upb.soot.jimple.common.type.LongType;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.jimple.common.type.ShortType;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.JavaClassSignature;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class BinaryOpInstructionConversionTest {
  private WalaClassLoader loader;
  private DefaultSignatureFactory sigFactory;
  private JavaClassSignature declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "src/test/resources/selected-java-target/";
    loader = new WalaClassLoader(srcDir, null);
    sigFactory = new DefaultSignatureFactory();
    declareClassSig = sigFactory.getClassSignature("BinaryOperations");
  }

  private static void assertEquiv(EquivTo expected, EquivTo actual) {
    if (!expected.equivTo(actual)) {
      throw new AssertionError("Expected '" + expected + "', actual is '" + actual + "'");
    }
  }

  private static <T> void assertInstanceOfSatisfying(
      Object actual, Class<T> tClass, Consumer<T> checker) {
    try {
      checker.accept(tClass.cast(actual));
    } catch (ClassCastException e) {
      throw new AssertionError(
          "Expected value of type "
              + tClass
              + (actual != null ? ", got type " + actual.getClass() + " with value " : ", got ")
              + actual);
    }
  }

  @Test
  public void testAddByte() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "addByte", declareClassSig, "byte", Arrays.asList("byte", "byte")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<IStmt> stmts = body.getStmts();
    assertEquals(8, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("r0", RefType.getInstance("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(RefType.getInstance("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$b0", ByteType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(ByteType.getInstance(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$b1", ByteType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(ByteType.getInstance(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$b0", ByteType.getInstance()), IntType.getInstance()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$b1", ByteType.getInstance()), IntType.getInstance()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i2", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JAddExpr(
                  new Local("$i0", IntType.getInstance()), new Local("$i1", IntType.getInstance())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(6),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$b2", ByteType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$i2", IntType.getInstance()), ByteType.getInstance()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(7),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$b2", ByteType.getInstance()), stmt.getOp()));
  }

  @Test
  public void testAddDouble() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "addDouble", declareClassSig, "double", Arrays.asList("double", "float")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<IStmt> stmts = body.getStmts();
    assertEquals(6, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("r0", RefType.getInstance("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(RefType.getInstance("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$d0", DoubleType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(DoubleType.getInstance(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$f0", FloatType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(FloatType.getInstance(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$d1", DoubleType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$f0", FloatType.getInstance()), DoubleType.getInstance()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$d2", DoubleType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JAddExpr(
                  new Local("$d0", DoubleType.getInstance()),
                  new Local("$d1", DoubleType.getInstance())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$d2", DoubleType.getInstance()), stmt.getOp()));
  }

  @Test
  public void testMulDouble() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "mulDouble", declareClassSig, "double", Arrays.asList("double", "double")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testSubChar() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "subChar", declareClassSig, "char", Arrays.asList("char", "char")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<IStmt> stmts = body.getStmts();
    assertEquals(8, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("r0", RefType.getInstance("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(RefType.getInstance("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$c0", CharType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(CharType.getInstance(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$c1", CharType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(CharType.getInstance(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$c0", CharType.getInstance()), IntType.getInstance()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$c1", CharType.getInstance()), IntType.getInstance()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i2", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JSubExpr(
                  new Local("$i0", IntType.getInstance()), new Local("$i1", IntType.getInstance())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(6),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$c2", CharType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$i2", IntType.getInstance()), CharType.getInstance()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(7),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$c2", CharType.getInstance()), stmt.getOp()));
  }

  @Test
  public void testMulShort() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "mulShort", declareClassSig, "short", Arrays.asList("short", "short")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<IStmt> stmts = body.getStmts();
    assertEquals(8, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("r0", RefType.getInstance("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(RefType.getInstance("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$s0", ShortType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(ShortType.getInstance(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$s1", ShortType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(ShortType.getInstance(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$s0", ShortType.getInstance()), IntType.getInstance()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$s1", ShortType.getInstance()), IntType.getInstance()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i2", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JMulExpr(
                  new Local("$i0", IntType.getInstance()), new Local("$i1", IntType.getInstance())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(6),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$s2", ShortType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$i2", IntType.getInstance()), ShortType.getInstance()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(7),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$s2", ShortType.getInstance()), stmt.getOp()));
  }

  @Test
  public void testDivInt() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "divInt", declareClassSig, "int", Arrays.asList("int", "int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<IStmt> stmts = body.getStmts();
    assertEquals(5, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("r0", RefType.getInstance("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(RefType.getInstance("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(IntType.getInstance(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(IntType.getInstance(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i2", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JDivExpr(
                  new Local("$i0", IntType.getInstance()), new Local("$i1", IntType.getInstance())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$i2", IntType.getInstance()), stmt.getOp()));
  }

  @Test
  public void testModChar() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "modChar", declareClassSig, "char", Arrays.asList("char", "char")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<IStmt> stmts = body.getStmts();
    assertEquals(8, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("r0", RefType.getInstance("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(RefType.getInstance("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$c0", CharType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(CharType.getInstance(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$c1", CharType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(CharType.getInstance(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$c0", CharType.getInstance()), IntType.getInstance()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$c1", CharType.getInstance()), IntType.getInstance()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i2", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JRemExpr(
                  new Local("$i0", IntType.getInstance()), new Local("$i1", IntType.getInstance())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(6),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$c2", CharType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$i2", IntType.getInstance()), CharType.getInstance()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(7),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$c2", CharType.getInstance()), stmt.getOp()));
  }

  @Test
  public void testIncShort() {
    // TODO: failed test
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "incShort", declareClassSig, "short", Collections.singletonList("short")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<IStmt> stmts = body.getStmts();
    assertEquals(6, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("r0", RefType.getInstance("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(RefType.getInstance("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$s0", ShortType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(ShortType.getInstance(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$s0", ShortType.getInstance()), IntType.getInstance()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JAddExpr(new Local("$i0", IntType.getInstance()), IntConstant.getInstance(1)),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$s1", ShortType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$i1", IntType.getInstance()), ShortType.getInstance()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$s0", ShortType.getInstance()), stmt.getOp()));
  }

  @Test
  public void testDecInt() {
    // TODO: failed test
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "decInt", declareClassSig, "int", Collections.singletonList("int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<IStmt> stmts = body.getStmts();
    assertEquals(4, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("r0", RefType.getInstance("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(RefType.getInstance("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(IntType.getInstance(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JSubExpr(new Local("$i0", IntType.getInstance()), IntConstant.getInstance(1)),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$i1", IntType.getInstance()), stmt.getOp()));
  }

  @Test
  public void testOrLong() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "orLong", declareClassSig, "long", Arrays.asList("long", "long")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<IStmt> stmts = body.getStmts();
    assertEquals(5, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("r0", RefType.getInstance("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(RefType.getInstance("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$l0", LongType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(LongType.getInstance(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$l1", LongType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(LongType.getInstance(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$l2", LongType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JOrExpr(
                  new Local("$l0", LongType.getInstance()),
                  new Local("$l1", LongType.getInstance())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$l2", LongType.getInstance()), stmt.getOp()));
  }

  @Test
  public void testXorInt() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "xorInt", declareClassSig, "int", Arrays.asList("int", "int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<IStmt> stmts = body.getStmts();
    assertEquals(5, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("r0", RefType.getInstance("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(RefType.getInstance("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(IntType.getInstance(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(IntType.getInstance(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i2", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JXorExpr(
                  new Local("$i0", IntType.getInstance()), new Local("$i1", IntType.getInstance())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$i2", IntType.getInstance()), stmt.getOp()));
  }

  @Test
  public void testAndChar() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "andChar", declareClassSig, "char", Arrays.asList("char", "char")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<IStmt> stmts = body.getStmts();
    assertEquals(8, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("r0", RefType.getInstance("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(RefType.getInstance("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$c0", CharType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(CharType.getInstance(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$c1", CharType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(CharType.getInstance(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$c0", CharType.getInstance()), IntType.getInstance()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$c1", CharType.getInstance()), IntType.getInstance()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i2", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JAndExpr(
                  new Local("$i0", IntType.getInstance()), new Local("$i1", IntType.getInstance())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(6),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$c2", CharType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$i2", IntType.getInstance()), CharType.getInstance()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(7),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$c2", CharType.getInstance()), stmt.getOp()));
  }

  @Test
  public void testLShiftByte() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "lshiftByte", declareClassSig, "byte", Collections.singletonList("byte")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<IStmt> stmts = body.getStmts();
    assertEquals(6, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("r0", RefType.getInstance("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(RefType.getInstance("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$b0", ByteType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(ByteType.getInstance(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$b0", ByteType.getInstance()), IntType.getInstance()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JShlExpr(new Local("$i0", IntType.getInstance()), IntConstant.getInstance(2)),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$b1", ByteType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$i1", IntType.getInstance()), ByteType.getInstance()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$b1", ByteType.getInstance()), stmt.getOp()));
  }

  @Test
  public void testRShiftShort() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "rshiftShort", declareClassSig, "short", Arrays.asList("short", "int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<IStmt> stmts = body.getStmts();
    assertEquals(7, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("r0", RefType.getInstance("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(RefType.getInstance("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$s0", ShortType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(ShortType.getInstance(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(IntType.getInstance(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$s0", ShortType.getInstance()), IntType.getInstance()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i2", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JShrExpr(
                  new Local("$i1", IntType.getInstance()), new Local("$i0", IntType.getInstance())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$s1", ShortType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$i2", IntType.getInstance()), ShortType.getInstance()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(6),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$s1", ShortType.getInstance()), stmt.getOp()));
  }

  @Test
  public void testNegLong() {
    // TODO: failed test
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "negLong", declareClassSig, "long", Collections.singletonList("long")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<IStmt> stmts = body.getStmts();
    assertEquals(4, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("r0", RefType.getInstance("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(RefType.getInstance("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$l0", LongType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(LongType.getInstance(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$l1", LongType.getInstance()), stmt.getLeftOp());
          assertEquiv(new JNegExpr(new Local("$l0", LongType.getInstance())), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$l1", LongType.getInstance()), stmt.getOp()));
  }

  @Test
  public void testZeroFillRshiftInt() {
    // TODO: failed test
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "zeroFillRshiftInt", declareClassSig, "int", Arrays.asList("int", "int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<IStmt> stmts = body.getStmts();
    assertEquals(5, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("r0", RefType.getInstance("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(RefType.getInstance("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(IntType.getInstance(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(IntType.getInstance(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i2", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(
              new JUshrExpr(
                  new Local("$i0", IntType.getInstance()), new Local("$i1", IntType.getInstance())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$i2", IntType.getInstance()), stmt.getOp()));
  }

  @Test
  public void testLogicalAnd() {
    // TODO: failed test
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "logicalAnd", declareClassSig, "boolean", Arrays.asList("boolean", "boolean")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<IStmt> stmts = body.getStmts();
    assertEquals(8, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("r0", RefType.getInstance("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(RefType.getInstance("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$z0", BooleanType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(BooleanType.getInstance(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$z1", BooleanType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(BooleanType.getInstance(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JIfStmt.class,
        stmt -> {
          assertEquiv(
              new JEqExpr(new Local("$z0", BooleanType.getInstance()), IntConstant.getInstance(0)),
              stmt.getCondition());
          assertInstanceOfSatisfying(
              stmt.getTarget(),
              JAssignStmt.class,
              target -> {
                assertEquiv(new Local("$z2", BooleanType.getInstance()), target.getLeftOp());
                assertEquiv(IntConstant.getInstance(0), target.getRightOp());
              });
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$z2", BooleanType.getInstance()), stmt.getLeftOp());
          assertEquiv(new Local("$z1", BooleanType.getInstance()), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JGotoStmt.class,
        stmt ->
            assertInstanceOfSatisfying(
                stmt.getTarget(),
                JReturnStmt.class,
                target ->
                    assertEquiv(new Local("$z2", BooleanType.getInstance()), target.getOp())));

    assertInstanceOfSatisfying(
        stmts.get(6),
        JAssignStmt.class,
        target -> {
          assertEquiv(new Local("$z2", BooleanType.getInstance()), target.getLeftOp());
          assertEquiv(IntConstant.getInstance(0), target.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(7),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$z2", BooleanType.getInstance()), stmt.getOp()));
  }

  @Test
  public void testLogicalOr() {
    // TODO: failed test
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "logicalOr", declareClassSig, "boolean", Arrays.asList("boolean", "boolean")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<IStmt> stmts = body.getStmts();
    assertEquals(8, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("r0", RefType.getInstance("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(RefType.getInstance("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$z0", BooleanType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(BooleanType.getInstance(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$z1", BooleanType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(BooleanType.getInstance(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JIfStmt.class,
        stmt -> {
          assertEquiv(
              new JEqExpr(new Local("$z0", BooleanType.getInstance()), IntConstant.getInstance(0)),
              stmt.getCondition());
          assertInstanceOfSatisfying(
              stmt.getTarget(),
              JAssignStmt.class,
              target -> {
                assertEquiv(new Local("$z2", BooleanType.getInstance()), target.getLeftOp());
                assertEquiv(new Local("$z1", BooleanType.getInstance()), target.getRightOp());
              });
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$z2", BooleanType.getInstance()), stmt.getLeftOp());
          assertEquiv(IntConstant.getInstance(1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JGotoStmt.class,
        stmt ->
            assertInstanceOfSatisfying(
                stmt.getTarget(),
                JReturnStmt.class,
                target ->
                    assertEquiv(new Local("$z2", BooleanType.getInstance()), target.getOp())));

    assertInstanceOfSatisfying(
        stmts.get(6),
        JAssignStmt.class,
        target -> {
          assertEquiv(new Local("$z2", BooleanType.getInstance()), target.getLeftOp());
          assertEquiv(new Local("$z1", BooleanType.getInstance()), target.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(7),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$z2", BooleanType.getInstance()), stmt.getOp()));
  }

  @Test
  public void testNot() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "not", declareClassSig, "boolean", Collections.singletonList("boolean")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<IStmt> stmts = body.getStmts();
    assertEquals(4, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("r0", RefType.getInstance("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(RefType.getInstance("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$z0", BooleanType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(BooleanType.getInstance(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$z1", BooleanType.getInstance()), stmt.getLeftOp());
          assertEquiv(new JNegExpr(new Local("$z0", BooleanType.getInstance())), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$z1", BooleanType.getInstance()), stmt.getOp()));
  }

  @Test
  public void testEqual() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "equal", declareClassSig, "boolean", Arrays.asList("int", "int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<IStmt> stmts = body.getStmts();
    assertEquals(5, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("r0", RefType.getInstance("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(RefType.getInstance("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(IntType.getInstance(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", IntType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(IntType.getInstance(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$z0", BooleanType.getInstance()), stmt.getLeftOp());
          assertInstanceOfSatisfying(
              stmt.getRightOp(),
              JEqExpr.class,
              expr -> {
                assertEquiv(new Local("$i0", IntType.getInstance()), expr.getOp1());
                assertEquiv(new Local("$i1", IntType.getInstance()), expr.getOp2());
              });
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$z0", BooleanType.getInstance()), stmt.getOp()));
  }

  @Test
  public void testNotEqual() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "notEqual", declareClassSig, "boolean", Arrays.asList("float", "float")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<IStmt> stmts = body.getStmts();
    assertEquals(5, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("r0", RefType.getInstance("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(RefType.getInstance("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$f0", FloatType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(FloatType.getInstance(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$f1", FloatType.getInstance()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(FloatType.getInstance(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$z0", BooleanType.getInstance()), stmt.getLeftOp());
          assertInstanceOfSatisfying(
              stmt.getRightOp(),
              JNeExpr.class,
              expr -> {
                assertEquiv(new Local("$f0", FloatType.getInstance()), expr.getOp1());
                assertEquiv(new Local("$f1", FloatType.getInstance()), expr.getOp2());
              });
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$z0", BooleanType.getInstance()), stmt.getOp()));
  }

  @Test
  public void testGreater() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "greater", declareClassSig, "boolean", Arrays.asList("double", "double")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testSmaller() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "smaller", declareClassSig, "boolean", Arrays.asList("long", "long")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testGreaterEqual() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "greaterEqual", declareClassSig, "boolean", Arrays.asList("char", "char")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testSmallerEqual() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "smallerEqual", declareClassSig, "boolean", Arrays.asList("byte", "byte")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }
}
