package sootup.java.bytecode.frontend.interceptors.typeresolving;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.ref.JThisRef;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.types.VoidType;
import sootup.interceptors.typeresolving.AugEvalFunction;
import sootup.interceptors.typeresolving.Typing;
import sootup.interceptors.typeresolving.types.AugmentIntegerTypes;
import sootup.java.core.JavaIdentifierFactory;

/**
 * @author Zun Wang
 */
public class AugEvalFunctionTest extends TypeAssignerTestSuite {

  AugEvalFunction evalFunction;

  @BeforeEach
  public void setup() {
    String baseDir = "../shared-test-resources/TypeResolverTestSuite/AugEvalFunctionTest/";
    String className = "AugEvalFunctionDemos";
    buildView(baseDir, className);
    evalFunction = new AugEvalFunction(view);
  }

  @Test
  public void testImmediate() {
    Type actual, expected;
    Stmt stmt = null;
    Value value = null;
    final Body.BodyBuilder builder = createMethodsBuilder("constant", "void");
    StmtGraph<?> graph = builder.getStmtGraph();

    Typing specTyping = new Typing(new ArrayList<>());

    for (Stmt s : graph.getStmts()) {
      String stmtStr = s.toString();
      switch (stmtStr) {
        case "l1 = 127":
          value = s.getUses().findFirst().get();
          stmt = s;
          expected = AugmentIntegerTypes.getInteger127();
          actual = evalFunction.evaluate(specTyping, value, stmt, graph);
          assertEquals(expected, actual);
          break;
        case "l1 = 32111":
          value = s.getUses().findFirst().get();
          stmt = s;
          expected = AugmentIntegerTypes.getInteger32767();
          actual = evalFunction.evaluate(specTyping, value, stmt, graph);
          assertEquals(expected, actual);
          break;
        case "l1 = -129":
          value = s.getUses().findFirst().get();
          stmt = s;
          expected = PrimitiveType.getShort();
          actual = evalFunction.evaluate(specTyping, value, stmt, graph);
          assertEquals(expected, actual);
          break;
        case "l2 = 1.0":
          value = s.getUses().findFirst().get();
          stmt = s;
          expected = PrimitiveType.getDouble();
          actual = evalFunction.evaluate(specTyping, value, stmt, graph);
          assertEquals(expected, actual);
          break;
        case "l4 = \"example\"":
          value = s.getUses().findFirst().get();
          stmt = s;
          expected = view.getIdentifierFactory().getClassType("java.lang.String");
          actual = evalFunction.evaluate(specTyping, value, stmt, graph);
          assertEquals(expected, actual);
          break;
        default:
      }
    }
    final Body.BodyBuilder builder2 = createMethodsBuilder("reflection", "void");
    StmtGraph<?> graph2 = builder2.getStmtGraph();

    for (Stmt s : graph2.getStmts()) {
      if (s.toString().equals("l1 = class \"LA;\"")) {
        value = s.getUses().findFirst().get();
        stmt = s;
        break;
      }
    }
    expected = view.getIdentifierFactory().getClassType("java.lang.Class");
    actual = evalFunction.evaluate(specTyping, value, stmt, graph);
    assertEquals(expected, actual);
  }

  @Test
  public void testIfExpr() {
    Stmt stmt = null;
    Value value = null;
    final Body.BodyBuilder builder = createMethodsBuilder("condition", "void");
    StmtGraph<?> graph = builder.getStmtGraph();

    Map<String, Type> map = new HashMap<>();
    map.put("l1", PrimitiveType.getBoolean());
    map.put("l2", PrimitiveType.getBoolean());
    Typing specTyping1 = createTyping(builder.getLocals(), map);

    for (Stmt s : graph.getStmts()) {
      if (s.toString().equals("if l1 >= l2")) {
        for (Iterator<Value> iterator =
                s.getUses().filter(use -> use instanceof AbstractConditionExpr).iterator();
            iterator.hasNext(); ) {
          value = iterator.next();
          stmt = s;
        }
      }
    }
    assertNotNull(stmt);
    assertEquals(
        PrimitiveType.getBoolean(), evalFunction.evaluate(specTyping1, value, stmt, graph));
  }

  @Test
  public void testShiftExpr() {
    Stmt stmt = null;
    Value value = null;

    final Body.BodyBuilder builder = createMethodsBuilder("shift", "void");
    StmtGraph<?> graph = builder.getStmtGraph();

    Map<String, Type> map = new HashMap<>();
    map.put("l1", PrimitiveType.getByte());
    map.put("l2", PrimitiveType.getLong());
    Typing specTyping = createTyping(builder.getLocals(), map);

    for (Stmt s : graph.getStmts()) {
      if (s.toString().equals("l4 = l2 << l1")) {
        for (Iterator<Value> iterator = s.getUses().iterator(); iterator.hasNext(); ) {
          Value use = iterator.next();
          if (use instanceof AbstractIntLongBinopExpr) {
            value = use;
            stmt = s;
          }
        }
        assertEquals(
            PrimitiveType.getLong(), evalFunction.evaluate(specTyping, value, stmt, graph));
      }

      if (s.toString().equals("l6 = l1 << $stack7")) {
        for (Iterator<Value> iterator = s.getUses().iterator(); iterator.hasNext(); ) {
          Value use = iterator.next();
          if (use instanceof AbstractIntLongBinopExpr) {
            value = use;
            stmt = s;

            assertEquals(
                PrimitiveType.getInt(), evalFunction.evaluate(specTyping, value, stmt, graph));
            return;
          }
        }
      }
    }
  }

  @Test
  public void testXorExpr1() {
    Stmt stmt = null;
    Value value = null;

    final Body.BodyBuilder builder = createMethodsBuilder("xor", "void");
    StmtGraph<?> graph = builder.getStmtGraph();

    Map<String, Type> map = new HashMap<>();
    map.put("l1", PrimitiveType.getBoolean());
    map.put("l2", PrimitiveType.getBoolean());
    map.put("l4", PrimitiveType.getLong());
    Typing specTyping = createTyping(builder.getLocals(), map);

    for (Stmt s : graph.getStmts()) {
      if (s.toString().equals("l3 = l2 ^ l1")) {
        for (Iterator<Value> iterator = s.getUses().iterator(); iterator.hasNext(); ) {
          Value use = iterator.next();
          if (use instanceof AbstractIntLongBinopExpr) {
            value = use;
            stmt = s;
          }
        }
        assertEquals(
            PrimitiveType.getBoolean(), evalFunction.evaluate(specTyping, value, stmt, graph));
      }

      if (s.toString().equals("l6 = $stack8 ^ l4")) {
        for (Iterator<Value> iterator = s.getUses().iterator(); iterator.hasNext(); ) {
          Value use = iterator.next();
          if (use instanceof AbstractIntLongBinopExpr) {
            value = use;
            stmt = s;
          }
        }
        assertEquals(
            PrimitiveType.getLong(), evalFunction.evaluate(specTyping, value, stmt, graph));
      }
    }
  }

  @Test
  public void testXorExpr2() {
    Stmt stmt = null;
    Value value = null;

    final Body.BodyBuilder builder = createMethodsBuilder("xor", "void");
    StmtGraph<?> graph = builder.getStmtGraph();

    Map<String, Type> map = new HashMap<>();
    map.put("l1", AugmentIntegerTypes.getInteger1());
    map.put("l2", PrimitiveType.getByte());
    Typing specTyping = createTyping(builder.getLocals(), map);

    for (Stmt s : graph.getStmts()) {
      if (s.toString().equals("l3 = l2 ^ l1")) {
        for (Iterator<Value> iterator = s.getUses().iterator(); iterator.hasNext(); ) {
          Value use = iterator.next();
          if (use instanceof AbstractIntLongBinopExpr) {
            value = use;
            stmt = s;
          }
        }
        assertEquals(
            PrimitiveType.getByte(), evalFunction.evaluate(specTyping, value, stmt, graph));
      }
    }
  }

  @Test
  public void testAddExpr() {
    Stmt stmt = null;
    Value value = null;

    final Body.BodyBuilder builder = createMethodsBuilder("add", "void");
    StmtGraph<?> graph = builder.getStmtGraph();

    Map<String, Type> map = new HashMap<>();
    map.put("l1", AugmentIntegerTypes.getInteger1());
    map.put("l2", PrimitiveType.getFloat());
    Typing specTyping = createTyping(builder.getLocals(), map);

    for (Stmt s : graph.getStmts()) {
      if (s.toString().equals("l3 = l2 + $stack4")) {
        for (Iterator<Value> iterator = s.getUses().iterator(); iterator.hasNext(); ) {
          Value use = iterator.next();
          if (use instanceof AbstractFloatBinopExpr) {
            value = use;
            stmt = s;
          }
        }
        assertEquals(
            PrimitiveType.getFloat(), evalFunction.evaluate(specTyping, value, stmt, graph));
      }

      if (s.toString().equals("l1 = l1 + 1")) {
        for (Iterator<Value> iterator = s.getUses().iterator(); iterator.hasNext(); ) {
          Value use = iterator.next();
          if (use instanceof AbstractFloatBinopExpr) {
            value = use;
            stmt = s;
          }
        }
        assertEquals(PrimitiveType.getInt(), evalFunction.evaluate(specTyping, value, stmt, graph));
      }
    }

    final Body.BodyBuilder builder5 = createMethodsBuilder("length", "void");
    StmtGraph<?> graph5 = builder5.getStmtGraph();

    for (Stmt s : graph5.getStmts()) {
      if (s.toString().equals("l2 = lengthof l1")) {
        for (Iterator<Value> iterator = s.getUses().iterator(); iterator.hasNext(); ) {
          Value use = iterator.next();
          if (use instanceof JLengthExpr) {
            value = use;
            stmt = s;
          }
        }
      }
    }
    assertEquals(PrimitiveType.getInt(), evalFunction.evaluate(specTyping, value, stmt, graph));

    final Body.BodyBuilder builder6 = createMethodsBuilder("instanceOf", "boolean");
    StmtGraph<?> graph6 = builder6.getStmtGraph();

    for (Stmt s : graph6.getStmts()) {
      if (s.toString().equals("$stack3 = l1 instanceof A")) {
        for (Iterator<Value> iterator = s.getUses().iterator(); iterator.hasNext(); ) {
          Value use = iterator.next();
          if (use instanceof JInstanceOfExpr) {
            value = use;
            stmt = s;
          }
        }
      }
    }
    assertEquals(PrimitiveType.getBoolean(), evalFunction.evaluate(specTyping, value, stmt, graph));

    final Body.BodyBuilder builder7 = createMethodsBuilder("newArrayExpr", "void");
    StmtGraph<?> graph7 = builder7.getStmtGraph();

    for (Stmt s : graph7.getStmts()) {
      if (s.toString().equals("l1 = newmultiarray (A)[3][3]")) {
        for (Iterator<Value> iterator = s.getUses().iterator(); iterator.hasNext(); ) {
          Value use = iterator.next();
          if (use instanceof JNewMultiArrayExpr) {
            value = use;
            stmt = s;
          }
        }
      }
    }
    JavaIdentifierFactory identifierFactory = view.getIdentifierFactory();
    Type expected = identifierFactory.getArrayType(identifierFactory.getClassType("A"), 2);
    assertEquals(expected, evalFunction.evaluate(specTyping, value, stmt, graph));

    final Body.BodyBuilder builder8 = createMethodsBuilder("invokeExpr", "void");
    StmtGraph<?> graph8 = builder8.getStmtGraph();

    for (Stmt s : graph8.getStmts()) {
      if (s.toString().equals("specialinvoke $stack2.<A: void <init>()>()")) {
        for (Iterator<Value> iterator = s.getUses().iterator(); iterator.hasNext(); ) {
          Value use = iterator.next();
          if (use instanceof AbstractInvokeExpr) {
            value = use;
            stmt = s;
            assertEquals(
                VoidType.getInstance(), evalFunction.evaluate(specTyping, value, stmt, graph));
          }
        }
      } else if (s.toString().equals("$stack3 = virtualinvoke l1.<A: B method()>()")) {
        for (Iterator<Value> iterator = s.getUses().iterator(); iterator.hasNext(); ) {
          Value use = iterator.next();
          if (use instanceof AbstractInvokeExpr) {
            value = use;
            stmt = s;
            expected = identifierFactory.getClassType("B");
            assertEquals(expected, evalFunction.evaluate(specTyping, value, stmt, graph));
          }
        }
      }
    }
  }

  @Test
  public void testRef() {

    Type actual;
    JavaIdentifierFactory identifierFactory = view.getIdentifierFactory();
    Type expected = identifierFactory.getClassType("java.lang.ArithmeticException");
    Stmt stmt = null;
    Value value = null;
    final Body.BodyBuilder builder = createMethodsBuilder("caughtException1", "void");
    StmtGraph<?> graph = builder.getStmtGraph();

    for (Stmt s : graph.getStmts()) {
      if (s.toString().equals("$stack2 := @caughtexception")) {
        for (Iterator<Value> iterator = s.getUses().iterator(); iterator.hasNext(); ) {
          Value use = iterator.next();
          if (use instanceof JCaughtExceptionRef) {
            value = use;
            stmt = s;
          }
        }
      }
    }
    actual = evalFunction.evaluate(new Typing(new ArrayList<>()), value, stmt, graph);
    assertEquals(expected, actual);

    final Body.BodyBuilder builder2 = createMethodsBuilder("caughtException2", "void");
    StmtGraph<?> graph2 = builder2.getStmtGraph();

    for (Stmt s : graph2.getStmts()) {
      if (s.toString().equals("$stack2 := @caughtexception")) {
        for (Iterator<Value> iterator = s.getUses().iterator(); iterator.hasNext(); ) {
          Value use = iterator.next();
          if (use instanceof JCaughtExceptionRef) {
            value = use;
            stmt = s;
          }
        }
      }
    }
    expected = identifierFactory.getClassType("java.lang.RuntimeException");
    actual = evalFunction.evaluate(new Typing(new ArrayList<>()), value, stmt, graph2);
    assertEquals(expected, actual);

    final Body.BodyBuilder builder3 = createMethodsBuilder("fieldRef", "void");
    StmtGraph<?> graph3 = builder3.getStmtGraph();

    for (Stmt s : graph3.getStmts()) {
      if (s.toString().equals("l1 = this.<ByteCodeTypeTest: A field>")) {
        for (Iterator<Value> iterator = s.getUses().iterator(); iterator.hasNext(); ) {
          Value use = iterator.next();
          if (use instanceof JFieldRef) {
            value = use;
            stmt = s;
            expected = identifierFactory.getClassType("A");
            actual = evalFunction.evaluate(new Typing(new ArrayList<>()), value, stmt, graph3);
            assertEquals(expected, actual);
          }
        }
      } else if (s.toString().equals("this := @this: ByteCodeTypeTest")) {
        for (Iterator<Value> iterator = s.getUses().iterator(); iterator.hasNext(); ) {
          Value use = iterator.next();
          if (use instanceof JThisRef) {
            value = use;
            stmt = s;
            expected = identifierFactory.getClassType("ByteCodeTypeTest");
            actual = evalFunction.evaluate(new Typing(new ArrayList<>()), value, stmt, graph3);
            assertEquals(expected, actual);
          }
        }
      }
    }
  }
}
