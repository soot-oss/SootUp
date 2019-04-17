package de.upb.soot.frontends.java;

import static de.upb.soot.frontends.java.Utils.assertEquiv;
import static de.upb.soot.frontends.java.Utils.assertInstanceOfSatisfying;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.soot.DefaultFactories;
import de.upb.soot.core.Body;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.common.constant.IntConstant;
import de.upb.soot.jimple.common.expr.JAddExpr;
import de.upb.soot.jimple.common.expr.JAndExpr;
import de.upb.soot.jimple.common.expr.JCastExpr;
import de.upb.soot.jimple.common.expr.JDivExpr;
import de.upb.soot.jimple.common.expr.JEqExpr;
import de.upb.soot.jimple.common.expr.JGeExpr;
import de.upb.soot.jimple.common.expr.JGtExpr;
import de.upb.soot.jimple.common.expr.JLeExpr;
import de.upb.soot.jimple.common.expr.JLtExpr;
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
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.types.DefaultTypeFactory;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.PrimitiveType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class BinaryOpInstructionConversionTest {
  private WalaClassLoader loader;
  private DefaultSignatureFactory sigFactory;
  private DefaultTypeFactory typeFactory;
  private JavaClassType declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "src/test/resources/selected-java-target/";
    loader = new WalaClassLoader(srcDir, null);
    DefaultFactories factories = DefaultFactories.create();
    sigFactory = factories.getSignatureFactory();
    typeFactory = factories.getTypeFactory();
    declareClassSig = typeFactory.getClassType("BinaryOperations");
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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$b0", PrimitiveType.getByte()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getByte(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$b1", PrimitiveType.getByte()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getByte(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$b0", PrimitiveType.getByte()), PrimitiveType.getInt()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$b1", PrimitiveType.getByte()), PrimitiveType.getInt()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i2", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JAddExpr(
                  new Local("$i0", PrimitiveType.getInt()),
                  new Local("$i1", PrimitiveType.getInt())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(6),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$b2", PrimitiveType.getByte()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$i2", PrimitiveType.getInt()), PrimitiveType.getByte()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(7),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$b2", PrimitiveType.getByte()), stmt.getOp()));
  }

  @Test
  public void testAddDouble() {
    // TODO: failed test

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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$d0", PrimitiveType.getDouble()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getDouble(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$f0", PrimitiveType.getFloat()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getFloat(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$d1", PrimitiveType.getDouble()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$f0", PrimitiveType.getFloat()), PrimitiveType.getDouble()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$d2", PrimitiveType.getDouble()), stmt.getLeftOp());
          assertEquiv(
              new JAddExpr(
                  new Local("$d0", PrimitiveType.getDouble()),
                  new Local("$d1", PrimitiveType.getDouble())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$d2", PrimitiveType.getDouble()), stmt.getOp()));
  }

  @Test
  public void testMulDouble() {
    // TODO: failed test

    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "mulDouble", declareClassSig, "double", Arrays.asList("double", "double")));
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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$d0", PrimitiveType.getDouble()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getDouble(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$d1", PrimitiveType.getDouble()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getDouble(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$d2", PrimitiveType.getDouble()), stmt.getLeftOp());
          assertEquiv(
              new JMulExpr(
                  new Local("$d0", PrimitiveType.getDouble()),
                  new Local("$d1", PrimitiveType.getDouble())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JReturnStmt.class,
        stmt -> {
          assertEquiv(new Local("$d2", PrimitiveType.getDouble()), stmt.getOp());
        });
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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$c0", PrimitiveType.getChar()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getChar(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$c1", PrimitiveType.getChar()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getChar(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$c0", PrimitiveType.getChar()), PrimitiveType.getInt()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$c1", PrimitiveType.getChar()), PrimitiveType.getInt()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i2", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JSubExpr(
                  new Local("$i0", PrimitiveType.getInt()),
                  new Local("$i1", PrimitiveType.getInt())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(6),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$c2", PrimitiveType.getChar()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$i2", PrimitiveType.getInt()), PrimitiveType.getChar()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(7),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$c2", PrimitiveType.getChar()), stmt.getOp()));
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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$s0", PrimitiveType.getShort()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getShort(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$s1", PrimitiveType.getShort()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getShort(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$s0", PrimitiveType.getShort()), PrimitiveType.getInt()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$s1", PrimitiveType.getShort()), PrimitiveType.getInt()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i2", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JMulExpr(
                  new Local("$i0", PrimitiveType.getInt()),
                  new Local("$i1", PrimitiveType.getInt())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(6),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$s2", PrimitiveType.getShort()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$i2", PrimitiveType.getInt()), PrimitiveType.getShort()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(7),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$s2", PrimitiveType.getShort()), stmt.getOp()));
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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getInt(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getInt(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i2", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JDivExpr(
                  new Local("$i0", PrimitiveType.getInt()),
                  new Local("$i1", PrimitiveType.getInt())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$i2", PrimitiveType.getInt()), stmt.getOp()));
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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$c0", PrimitiveType.getChar()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getChar(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$c1", PrimitiveType.getChar()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getChar(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$c0", PrimitiveType.getChar()), PrimitiveType.getInt()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$c1", PrimitiveType.getChar()), PrimitiveType.getInt()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i2", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JRemExpr(
                  new Local("$i0", PrimitiveType.getInt()),
                  new Local("$i1", PrimitiveType.getInt())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(6),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$c2", PrimitiveType.getChar()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$i2", PrimitiveType.getInt()), PrimitiveType.getChar()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(7),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$c2", PrimitiveType.getChar()), stmt.getOp()));
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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$s0", PrimitiveType.getShort()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getShort(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$s0", PrimitiveType.getShort()), PrimitiveType.getInt()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JAddExpr(new Local("$i0", PrimitiveType.getInt()), IntConstant.getInstance(1)),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$s1", PrimitiveType.getShort()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$i1", PrimitiveType.getInt()), PrimitiveType.getShort()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$s0", PrimitiveType.getShort()), stmt.getOp()));
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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getInt(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JSubExpr(new Local("$i0", PrimitiveType.getInt()), IntConstant.getInstance(1)),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$i1", PrimitiveType.getInt()), stmt.getOp()));
  }

  @Test
  public void testOrLong() {
    // TODO: failed test

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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$l0", PrimitiveType.getLong()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getLong(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$l1", PrimitiveType.getLong()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getLong(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$l2", PrimitiveType.getLong()), stmt.getLeftOp());
          assertEquiv(
              new JOrExpr(
                  new Local("$l0", PrimitiveType.getLong()),
                  new Local("$l1", PrimitiveType.getLong())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$l2", PrimitiveType.getLong()), stmt.getOp()));
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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getInt(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getInt(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i2", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JXorExpr(
                  new Local("$i0", PrimitiveType.getInt()),
                  new Local("$i1", PrimitiveType.getInt())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$i2", PrimitiveType.getInt()), stmt.getOp()));
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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$c0", PrimitiveType.getChar()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getChar(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$c1", PrimitiveType.getChar()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getChar(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$c0", PrimitiveType.getChar()), PrimitiveType.getInt()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$c1", PrimitiveType.getChar()), PrimitiveType.getInt()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i2", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JAndExpr(
                  new Local("$i0", PrimitiveType.getInt()),
                  new Local("$i1", PrimitiveType.getInt())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(6),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$c2", PrimitiveType.getChar()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$i2", PrimitiveType.getInt()), PrimitiveType.getChar()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(7),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$c2", PrimitiveType.getChar()), stmt.getOp()));
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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$b0", PrimitiveType.getByte()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getByte(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$b0", PrimitiveType.getByte()), PrimitiveType.getInt()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JShlExpr(new Local("$i0", PrimitiveType.getInt()), IntConstant.getInstance(2)),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$b1", PrimitiveType.getByte()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$i1", PrimitiveType.getInt()), PrimitiveType.getByte()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$b1", PrimitiveType.getByte()), stmt.getOp()));
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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$s0", PrimitiveType.getShort()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getShort(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getInt(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$s0", PrimitiveType.getShort()), PrimitiveType.getInt()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i2", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JShrExpr(
                  new Local("$i1", PrimitiveType.getInt()),
                  new Local("$i0", PrimitiveType.getInt())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$s1", PrimitiveType.getShort()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$i2", PrimitiveType.getInt()), PrimitiveType.getShort()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(6),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$s1", PrimitiveType.getShort()), stmt.getOp()));
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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$l0", PrimitiveType.getLong()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getLong(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$l1", PrimitiveType.getLong()), stmt.getLeftOp());
          assertEquiv(new JNegExpr(new Local("$l0", PrimitiveType.getLong())), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$l1", PrimitiveType.getLong()), stmt.getOp()));
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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getInt(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getInt(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i2", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JUshrExpr(
                  new Local("$i0", PrimitiveType.getInt()),
                  new Local("$i1", PrimitiveType.getInt())),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$i2", PrimitiveType.getInt()), stmt.getOp()));
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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$z0", PrimitiveType.getBoolean()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getBoolean(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$z1", PrimitiveType.getBoolean()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getBoolean(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JIfStmt.class,
        stmt -> {
          assertEquiv(
              new JEqExpr(new Local("$z0", PrimitiveType.getBoolean()), IntConstant.getInstance(0)),
              stmt.getCondition());
          assertInstanceOfSatisfying(
              stmt.getTarget(),
              JAssignStmt.class,
              target -> {
                assertEquiv(new Local("$z2", PrimitiveType.getBoolean()), target.getLeftOp());
                assertEquiv(IntConstant.getInstance(0), target.getRightOp());
              });
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$z2", PrimitiveType.getBoolean()), stmt.getLeftOp());
          assertEquiv(new Local("$z1", PrimitiveType.getBoolean()), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JGotoStmt.class,
        stmt ->
            assertInstanceOfSatisfying(
                stmt.getTarget(),
                JReturnStmt.class,
                target ->
                    assertEquiv(new Local("$z2", PrimitiveType.getBoolean()), target.getOp())));

    assertInstanceOfSatisfying(
        stmts.get(6),
        JAssignStmt.class,
        target -> {
          assertEquiv(new Local("$z2", PrimitiveType.getBoolean()), target.getLeftOp());
          assertEquiv(IntConstant.getInstance(0), target.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(7),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$z2", PrimitiveType.getBoolean()), stmt.getOp()));
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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$z0", PrimitiveType.getBoolean()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getBoolean(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$z1", PrimitiveType.getBoolean()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getBoolean(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JIfStmt.class,
        stmt -> {
          assertEquiv(
              new JEqExpr(new Local("$z0", PrimitiveType.getBoolean()), IntConstant.getInstance(0)),
              stmt.getCondition());
          assertInstanceOfSatisfying(
              stmt.getTarget(),
              JAssignStmt.class,
              target -> {
                assertEquiv(new Local("$z2", PrimitiveType.getBoolean()), target.getLeftOp());
                assertEquiv(new Local("$z1", PrimitiveType.getBoolean()), target.getRightOp());
              });
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$z2", PrimitiveType.getBoolean()), stmt.getLeftOp());
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
                    assertEquiv(new Local("$z2", PrimitiveType.getBoolean()), target.getOp())));

    assertInstanceOfSatisfying(
        stmts.get(6),
        JAssignStmt.class,
        target -> {
          assertEquiv(new Local("$z2", PrimitiveType.getBoolean()), target.getLeftOp());
          assertEquiv(new Local("$z1", PrimitiveType.getBoolean()), target.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(7),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$z2", PrimitiveType.getBoolean()), stmt.getOp()));
  }

  @Test
  public void testNot() {
    // TODO: failed test

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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$z0", PrimitiveType.getBoolean()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getBoolean(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$z1", PrimitiveType.getBoolean()), stmt.getLeftOp());
          assertEquiv(
              new JNegExpr(new Local("$z0", PrimitiveType.getBoolean())), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$z1", PrimitiveType.getBoolean()), stmt.getOp()));
  }

  @Test
  public void testEqual() {
    // TODO: failed test

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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getInt(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getInt(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$z0", PrimitiveType.getBoolean()), stmt.getLeftOp());
          assertInstanceOfSatisfying(
              stmt.getRightOp(),
              JEqExpr.class,
              expr -> {
                assertEquiv(new Local("$i0", PrimitiveType.getInt()), expr.getOp1());
                assertEquiv(new Local("$i1", PrimitiveType.getInt()), expr.getOp2());
              });
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$z0", PrimitiveType.getBoolean()), stmt.getOp()));
  }

  @Test
  public void testNotEqual() {
    // TODO: failed test

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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$f0", PrimitiveType.getFloat()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getFloat(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$f1", PrimitiveType.getFloat()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getFloat(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$z0", PrimitiveType.getBoolean()), stmt.getLeftOp());
          assertInstanceOfSatisfying(
              stmt.getRightOp(),
              JNeExpr.class,
              expr -> {
                assertEquiv(new Local("$f0", PrimitiveType.getFloat()), expr.getOp1());
                assertEquiv(new Local("$f1", PrimitiveType.getFloat()), expr.getOp2());
              });
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$z0", PrimitiveType.getBoolean()), stmt.getOp()));
  }

  @Test
  public void testGreater() {
    // TODO: failed test

    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "greater", declareClassSig, "boolean", Arrays.asList("double", "double")));
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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$d0", PrimitiveType.getDouble()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getDouble(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$d1", PrimitiveType.getDouble()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getDouble(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$z0", PrimitiveType.getBoolean()), stmt.getLeftOp());
          assertInstanceOfSatisfying(
              stmt.getRightOp(),
              JGtExpr.class,
              expr -> {
                assertEquiv(new Local("$d0", PrimitiveType.getDouble()), expr.getOp1());
                assertEquiv(new Local("$d1", PrimitiveType.getDouble()), expr.getOp2());
              });
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$z0", PrimitiveType.getBoolean()), stmt.getOp()));
  }

  @Test
  public void testSmaller() {
    // TODO: failed test

    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "smaller", declareClassSig, "boolean", Arrays.asList("long", "long")));
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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$l0", PrimitiveType.getLong()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getLong(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$l1", PrimitiveType.getLong()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getLong(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$z0", PrimitiveType.getBoolean()), stmt.getLeftOp());
          assertInstanceOfSatisfying(
              stmt.getRightOp(),
              JLtExpr.class,
              expr -> {
                assertEquiv(new Local("$l0", PrimitiveType.getLong()), expr.getOp1());
                assertEquiv(new Local("$l1", PrimitiveType.getLong()), expr.getOp2());
              });
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$z0", PrimitiveType.getBoolean()), stmt.getOp()));
  }

  @Test
  public void testGreaterEqual() {
    // TODO: failed test

    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "greaterEqual", declareClassSig, "boolean", Arrays.asList("char", "char")));
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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$c0", PrimitiveType.getChar()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getChar(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$c1", PrimitiveType.getChar()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getChar(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$c0", PrimitiveType.getChar()), PrimitiveType.getInt()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$c1", PrimitiveType.getChar()), PrimitiveType.getInt()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$z0", PrimitiveType.getBoolean()), stmt.getLeftOp());
          assertInstanceOfSatisfying(
              stmt.getRightOp(),
              JGeExpr.class,
              expr -> {
                assertEquiv(new Local("$i0", PrimitiveType.getInt()), expr.getOp1());
                assertEquiv(new Local("$i1", PrimitiveType.getInt()), expr.getOp2());
              });
        });

    assertInstanceOfSatisfying(
        stmts.get(6),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$z0", PrimitiveType.getBoolean()), stmt.getOp()));
  }

  @Test
  public void testSmallerEqual() {
    // TODO: failed test

    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "smallerEqual", declareClassSig, "boolean", Arrays.asList("byte", "byte")));
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
          assertEquiv(
              new Local("r0", typeFactory.getClassType("BinaryOperations")), stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(typeFactory.getClassType("BinaryOperations")), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$b0", PrimitiveType.getByte()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getByte(), 0), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(new Local("$b1", PrimitiveType.getByte()), stmt.getLeftOp());
          assertEquiv(Jimple.newParameterRef(PrimitiveType.getByte(), 1), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$b0", PrimitiveType.getByte()), PrimitiveType.getInt()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i1", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(
              new JCastExpr(new Local("$b1", PrimitiveType.getByte()), PrimitiveType.getInt()),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$z0", PrimitiveType.getBoolean()), stmt.getLeftOp());
          assertInstanceOfSatisfying(
              stmt.getRightOp(),
              JLeExpr.class,
              expr -> {
                assertEquiv(new Local("$i0", PrimitiveType.getInt()), expr.getOp1());
                assertEquiv(new Local("$i1", PrimitiveType.getInt()), expr.getOp2());
              });
        });

    assertInstanceOfSatisfying(
        stmts.get(6),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$z0", PrimitiveType.getBoolean()), stmt.getOp()));
  }
}
