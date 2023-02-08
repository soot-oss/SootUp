package sootup.java.sourcecode.frontend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static sootup.core.util.Utils.assertEquiv;
import static sootup.core.util.Utils.assertInstanceOfSatisfying;

import categories.Java8Test;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.constant.BooleanConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JAddExpr;
import sootup.core.jimple.common.expr.JAndExpr;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.expr.JDivExpr;
import sootup.core.jimple.common.expr.JEqExpr;
import sootup.core.jimple.common.expr.JGeExpr;
import sootup.core.jimple.common.expr.JGtExpr;
import sootup.core.jimple.common.expr.JLeExpr;
import sootup.core.jimple.common.expr.JLtExpr;
import sootup.core.jimple.common.expr.JMulExpr;
import sootup.core.jimple.common.expr.JNeExpr;
import sootup.core.jimple.common.expr.JNegExpr;
import sootup.core.jimple.common.expr.JOrExpr;
import sootup.core.jimple.common.expr.JRemExpr;
import sootup.core.jimple.common.expr.JShlExpr;
import sootup.core.jimple.common.expr.JShrExpr;
import sootup.core.jimple.common.expr.JSubExpr;
import sootup.core.jimple.common.expr.JUshrExpr;
import sootup.core.jimple.common.expr.JXorExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.types.PrimitiveType;
import sootup.core.util.Utils;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;
import sootup.java.sourcecode.WalaClassLoaderTestUtils;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class BinaryOpInstructionConversionTest {
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
  public void testAddByte() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "addByte", "byte", Arrays.asList("byte", "byte")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(8, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "addDouble", "double", Arrays.asList("double", "float")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(6, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "mulDouble", "double", Arrays.asList("double", "double")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(5, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "subChar", "char", Arrays.asList("char", "char")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(8, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "mulShort", "short", Arrays.asList("short", "short")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(8, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "divInt", "int", Arrays.asList("int", "int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(5, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "modChar", "char", Arrays.asList("char", "char")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(8, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "incShort", "short", Collections.singletonList("short")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(6, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
          assertEquiv(new Local("$s1", PrimitiveType.getShort()), stmt.getLeftOp());
          assertEquiv(new Local("$s0", PrimitiveType.getShort()), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$s2", PrimitiveType.getShort()), stmt.getLeftOp());
          assertEquiv(
              new JAddExpr(new Local("$s0", PrimitiveType.getShort()), IntConstant.getInstance(1)),
              stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$s0", PrimitiveType.getShort()), stmt.getLeftOp());
          assertEquiv(new Local("$s2", PrimitiveType.getShort()), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$s1", PrimitiveType.getShort()), stmt.getOp()));
  }

  @Test
  public void testDecInt() {
    // TODO: failed test
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "decInt", "int", Collections.singletonList("int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(5, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
        JAssignStmt.class,
        stmt -> {
          assertEquiv(new Local("$i0", PrimitiveType.getInt()), stmt.getLeftOp());
          assertEquiv(new Local("$i1", PrimitiveType.getInt()), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JReturnStmt.class,
        stmt -> assertEquiv(new Local("$i1", PrimitiveType.getInt()), stmt.getOp()));
  }

  @Test
  public void testOrLong() {
    // TODO: failed test

    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "orLong", "long", Arrays.asList("long", "long")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(5, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "xorInt", "int", Arrays.asList("int", "int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(5, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "andChar", "char", Arrays.asList("char", "char")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(8, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "lshiftByte", "byte", Collections.singletonList("byte")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(6, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "rshiftShort", "short", Arrays.asList("short", "int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(7, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "negLong", "long", Collections.singletonList("long")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(4, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "zeroFillRshiftInt", "int", Arrays.asList("int", "int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(5, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "logicalAnd", "boolean", Arrays.asList("boolean", "boolean")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(8, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
          // [ms] bounds are validated in Body
          assertInstanceOfSatisfying(
              stmt.getTargetStmts(body).get(0),
              JAssignStmt.class,
              target -> {
                assertEquiv(new Local("$z2", PrimitiveType.getBoolean()), target.getLeftOp());
                assertEquiv(BooleanConstant.getFalse(), target.getRightOp());
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
                stmt.getTargetStmts(body).get(0),
                JReturnStmt.class,
                target ->
                    assertEquiv(new Local("$z2", PrimitiveType.getBoolean()), target.getOp())));

    assertInstanceOfSatisfying(
        stmts.get(6),
        JAssignStmt.class,
        target -> {
          assertEquiv(new Local("$z2", PrimitiveType.getBoolean()), target.getLeftOp());
          assertEquiv(BooleanConstant.getFalse(), target.getRightOp());
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
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "logicalOr", "boolean", Arrays.asList("boolean", "boolean")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(8, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
          // [ms] bounds are validated in Body
          assertInstanceOfSatisfying(
              stmt.getTargetStmts(body).get(0),
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
          assertEquiv(BooleanConstant.getTrue(), stmt.getRightOp());
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JGotoStmt.class,
        stmt ->
            assertInstanceOfSatisfying(
                stmt.getTargetStmts(body).get(0),
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
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "not", "boolean", Collections.singletonList("boolean")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(4, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "equal", "boolean", Arrays.asList("int", "int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(5, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "notEqual", "boolean", Arrays.asList("float", "float")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(5, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "greater", "boolean", Arrays.asList("double", "double")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(5, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "smaller", "boolean", Arrays.asList("long", "long")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(5, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "greaterEqual", "boolean", Arrays.asList("char", "char")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(7, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "smallerEqual", "boolean", Arrays.asList("byte", "byte")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<Stmt> stmts = body.getStmts();
    assertEquals(7, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(
              new Local("r0", identifierFactory.getClassType("BinaryOperations")),
              stmt.getLeftOp());
          assertEquiv(
              Jimple.newThisRef(identifierFactory.getClassType("BinaryOperations")),
              stmt.getRightOp());
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

  @Test
  public void testString1() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig,
                "getString1",
                "java.lang.String",
                Arrays.asList("java.lang.String")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: BinaryOperations",
                "$r1 := @parameter0: java.lang.String",
                "$r3 = new java.lang.StringBuilder",
                "specialinvoke $r3.<java.lang.StringBuilder: void <init>(java.lang.String)>(\"abc\")",
                "$r4 = virtualinvoke $r3.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>($r1)",
                "$r2 = virtualinvoke $r4.<java.lang.StringBuilder: java.lang.StringBuilder toString()>()",
                "return $r2")
            .collect(Collectors.toCollection(ArrayList::new));
    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void testString2() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig,
                "getString2",
                "java.lang.String",
                Collections.singletonList("java.lang.String")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);
    Utils.print(method, false);
    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: BinaryOperations",
                "$r1 := @parameter0: java.lang.String",
                "$r3 = new java.lang.StringBuilder",
                "specialinvoke $r3.<java.lang.StringBuilder: void <init>(java.lang.String)>($r1)",
                "$r4 = virtualinvoke $r3.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(\"xyz\")",
                "$r2 = virtualinvoke $r4.<java.lang.StringBuilder: java.lang.StringBuilder toString()>()",
                "return $r2")
            .collect(Collectors.toCollection(ArrayList::new));
    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void testString3() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig,
                "getString3",
                "java.lang.String",
                Arrays.asList("java.lang.String", "java.lang.String")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: BinaryOperations",
                "$r1 := @parameter0: java.lang.String",
                "$r2 := @parameter1: java.lang.String",
                "$r4 = new java.lang.StringBuilder",
                "specialinvoke $r4.<java.lang.StringBuilder: void <init>(java.lang.String)>($r1)",
                "$r5 = virtualinvoke $r4.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>($r2)",
                "$r3 = virtualinvoke $r5.<java.lang.StringBuilder: java.lang.StringBuilder toString()>()",
                "return $r3")
            .collect(Collectors.toCollection(ArrayList::new));
    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void testString4() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "getString4", "java.lang.String", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Body body = method.getBody();
    assertNotNull(body);
    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: BinaryOperations",
                "$r2 = new java.lang.StringBuilder",
                "specialinvoke $r2.<java.lang.StringBuilder: void <init>(java.lang.String)>(\"abc\")",
                "$r3 = virtualinvoke $r2.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(\"xyz\")",
                "$r1 = virtualinvoke $r3.<java.lang.StringBuilder: java.lang.StringBuilder toString()>()",
                "$r5 = new java.lang.StringBuilder",
                "specialinvoke $r5.<java.lang.StringBuilder: void <init>(java.lang.String)>($r1)",
                "$r6 = virtualinvoke $r5.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(\"efg\")",
                "$r4 = virtualinvoke $r6.<java.lang.StringBuilder: java.lang.StringBuilder toString()>()",
                "return $r4")
            .collect(Collectors.toCollection(ArrayList::new));
    assertEquals(expectedStmts, actualStmts);
  }
}
