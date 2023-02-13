package sootup.java.bytecode.interceptors.typeresolving;

import categories.Java8Test;
import java.util.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
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
import sootup.java.bytecode.interceptors.typeresolving.types.AugIntegerTypes;

/** @author Zun Wang */
@Category(Java8Test.class)
public class AugEvalFunctionTest extends TypeAssignerTestSuite {

  Typing typing = new Typing(new ArrayList<>());
  AugEvalFunction evalFunction;

  @Before
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

    for (Stmt s : graph.getStmts()) {
      String sn = s.toString();
      switch (sn) {
        case "l1 = 127":
          value = s.getUses().get(0);
          stmt = s;
          expected = AugIntegerTypes.getInteger127();
          actual = evalFunction.evaluate(typing, value, stmt, graph);
          Assert.assertEquals(expected, actual);
          break;
        case "l1 = 32111":
          value = s.getUses().get(0);
          stmt = s;
          expected = AugIntegerTypes.getInteger32767();
          actual = evalFunction.evaluate(typing, value, stmt, graph);
          Assert.assertEquals(expected, actual);
          break;
        case "l1 = -129":
          value = s.getUses().get(0);
          stmt = s;
          expected = PrimitiveType.getShort();
          actual = evalFunction.evaluate(typing, value, stmt, graph);
          Assert.assertEquals(expected, actual);
          break;
        case "l2 = 1.0":
          value = s.getUses().get(0);
          stmt = s;
          expected = PrimitiveType.getDouble();
          actual = evalFunction.evaluate(typing, value, stmt, graph);
          Assert.assertEquals(expected, actual);
          break;
        case "l4 = \"example\"":
          value = s.getUses().get(0);
          stmt = s;
          expected = identifierFactory.getClassType("java.lang.String");
          actual = evalFunction.evaluate(typing, value, stmt, graph);
          Assert.assertEquals(expected, actual);
          break;
        default:
      }
    }
    final Body.BodyBuilder builder2 = createMethodsBuilder("reflection", "void");
    StmtGraph<?> graph2 = builder2.getStmtGraph();

    for (Stmt s : graph2.getStmts()) {
      if (s.toString().equals("l1 = class \"LA;\"")) {
        value = s.getUses().get(0);
        stmt = s;
        break;
      }
    }
    expected = identifierFactory.getClassType("java.lang.Class");
    actual = evalFunction.evaluate(typing, value, stmt, graph);
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testExpr() {
    Type actual;
    Type expected = PrimitiveType.getBoolean();
    Stmt stmt = null;
    Value value = null;
    final Body.BodyBuilder builder = createMethodsBuilder("condition", "void");
    StmtGraph<?> graph = builder.getStmtGraph();

    for (Stmt s : graph.getStmts()) {
      if (s.toString().equals("if l1 >= l2")) {
        for (Value use : s.getUses()) {
          if (use instanceof AbstractConditionExpr) {
            value = use;
            stmt = s;
          }
        }
      }
    }
    actual = evalFunction.evaluate(typing, value, stmt, graph);
    Assert.assertEquals(expected, actual);

    final Body.BodyBuilder builder2 = createMethodsBuilder("shift", "void");
    StmtGraph<?> graph2 = builder2.getStmtGraph();

    Map<String, Type> map = new HashMap<>();
    map.put("l1", PrimitiveType.getByte());
    map.put("l2", PrimitiveType.getLong());
    Typing specTyping = createTyping(builder2.getLocals(), map);

    for (Stmt s : graph2.getStmts()) {
      if (s.toString().equals("l4 = l2 << l1")) {
        for (Value use : s.getUses()) {
          if (use instanceof AbstractIntLongBinopExpr) {
            value = use;
            stmt = s;
          }
        }
        expected = PrimitiveType.getLong();
        actual = evalFunction.evaluate(specTyping, value, stmt, graph);
        Assert.assertEquals(expected, actual);
      }

      if (s.toString().equals("l6 = l1 << $stack7")) {
        for (Value use : s.getUses()) {
          if (use instanceof AbstractIntLongBinopExpr) {
            value = use;
            stmt = s;
          }
        }
        expected = PrimitiveType.getInt();
        actual = evalFunction.evaluate(specTyping, value, stmt, graph);
        Assert.assertEquals(expected, actual);
      }
    }

    final Body.BodyBuilder builder3 = createMethodsBuilder("xor", "void");
    StmtGraph<?> graph3 = builder3.getStmtGraph();

    map.clear();
    map.put("l1", PrimitiveType.getBoolean());
    map.put("l2", PrimitiveType.getBoolean());
    map.put("l4", PrimitiveType.getLong());
    specTyping = createTyping(builder3.getLocals(), map);
    for (Stmt s : graph3.getStmts()) {
      if (s.toString().equals("l3 = l2 ^ l1")) {
        for (Value use : s.getUses()) {
          if (use instanceof AbstractIntLongBinopExpr) {
            value = use;
            stmt = s;
          }
        }
        expected = PrimitiveType.getBoolean();
        actual = evalFunction.evaluate(specTyping, value, stmt, graph);
        Assert.assertEquals(expected, actual);
      }

      if (s.toString().equals("l6 = $stack8 ^ l4")) {
        for (Value use : s.getUses()) {
          if (use instanceof AbstractIntLongBinopExpr) {
            value = use;
            stmt = s;
          }
        }
        expected = PrimitiveType.getLong();
        actual = evalFunction.evaluate(specTyping, value, stmt, graph);
        Assert.assertEquals(expected, actual);
      }
    }

    map.clear();
    map.put("l1", AugIntegerTypes.getInteger1());
    map.put("l2", PrimitiveType.getByte());
    specTyping = createTyping(builder.getLocals(), map);

    for (Stmt s : graph.getStmts()) {
      if (s.toString().equals("l3 = l2 ^ l1")) {
        for (Value use : s.getUses()) {
          if (use instanceof AbstractIntLongBinopExpr) {
            value = use;
            stmt = s;
          }
        }
        expected = PrimitiveType.getByte();
        actual = evalFunction.evaluate(specTyping, value, stmt, graph);
        Assert.assertEquals(expected, actual);
      }
    }

    final Body.BodyBuilder builder4 = createMethodsBuilder("add", "void");
    StmtGraph<?> graph4 = builder4.getStmtGraph();

    map.clear();
    map.put("l1", AugIntegerTypes.getInteger1());
    map.put("l2", PrimitiveType.getFloat());
    specTyping = createTyping(builder4.getLocals(), map);

    for (Stmt s : graph4.getStmts()) {
      if (s.toString().equals("l3 = l2 + $stack4")) {
        for (Value use : s.getUses()) {
          if (use instanceof AbstractFloatBinopExpr) {
            value = use;
            stmt = s;
          }
        }
        expected = PrimitiveType.getFloat();
        actual = evalFunction.evaluate(specTyping, value, stmt, graph);
        Assert.assertEquals(expected, actual);
      }

      if (s.toString().equals("l1 = l1 + 1")) {
        for (Value use : s.getUses()) {
          if (use instanceof AbstractFloatBinopExpr) {
            value = use;
            stmt = s;
          }
        }
        expected = PrimitiveType.getInt();
        actual = evalFunction.evaluate(specTyping, value, stmt, graph);
        Assert.assertEquals(expected, actual);
      }
    }

    final Body.BodyBuilder builder5 = createMethodsBuilder("length", "void");
    StmtGraph<?> graph5 = builder5.getStmtGraph();

    for (Stmt s : graph5.getStmts()) {
      if (s.toString().equals("l2 = lengthof l1")) {
        for (Value use : s.getUses()) {
          if (use instanceof JLengthExpr) {
            value = use;
            stmt = s;
          }
        }
      }
    }
    expected = PrimitiveType.getInt();
    actual = evalFunction.evaluate(typing, value, stmt, graph);
    Assert.assertEquals(expected, actual);

    final Body.BodyBuilder builder6 = createMethodsBuilder("instanceOf", "boolean");
    StmtGraph<?> graph6 = builder6.getStmtGraph();

    for (Stmt s : graph6.getStmts()) {
      if (s.toString().equals("$stack3 = l1 instanceof A")) {
        for (Value use : s.getUses()) {
          if (use instanceof JInstanceOfExpr) {
            value = use;
            stmt = s;
          }
        }
      }
    }
    expected = PrimitiveType.getBoolean();
    actual = evalFunction.evaluate(typing, value, stmt, graph);
    Assert.assertEquals(expected, actual);

    final Body.BodyBuilder builder7 = createMethodsBuilder("newArrayExpr", "void");
    StmtGraph<?> graph7 = builder7.getStmtGraph();

    for (Stmt s : graph7.getStmts()) {
      if (s.toString().equals("l1 = newmultiarray (A)[3][3]")) {
        for (Value use : s.getUses()) {
          if (use instanceof JNewMultiArrayExpr) {
            value = use;
            stmt = s;
          }
        }
      }
    }
    expected = identifierFactory.getArrayType(identifierFactory.getClassType("A"), 2);
    actual = evalFunction.evaluate(typing, value, stmt, graph);
    Assert.assertEquals(expected, actual);

    final Body.BodyBuilder builder8 = createMethodsBuilder("invokeExpr", "void");
    StmtGraph<?> graph8 = builder8.getStmtGraph();

    for (Stmt s : graph8.getStmts()) {
      if (s.toString().equals("specialinvoke $stack2.<A: void <init>()>()")) {
        for (Value use : s.getUses()) {
          if (use instanceof AbstractInvokeExpr) {
            value = use;
            stmt = s;
            expected = VoidType.getInstance();
            actual = evalFunction.evaluate(typing, value, stmt, graph);
            Assert.assertEquals(expected, actual);
          }
        }
      } else if (s.toString().equals("$stack3 = virtualinvoke l1.<A: B method()>()")) {
        for (Value use : s.getUses()) {
          if (use instanceof AbstractInvokeExpr) {
            value = use;
            stmt = s;
            expected = identifierFactory.getClassType("B");
            actual = evalFunction.evaluate(typing, value, stmt, graph);
            Assert.assertEquals(expected, actual);
          }
        }
      }
    }
  }

  @Test
  public void testRef() {

    Type actual;
    Type expected = identifierFactory.getClassType("java.lang.ArithmeticException");
    Stmt stmt = null;
    Value value = null;
    final Body.BodyBuilder builder = createMethodsBuilder("caughtException1", "void");
    StmtGraph<?> graph = builder.getStmtGraph();

    for (Stmt s : graph.getStmts()) {
      if (s.toString().equals("$stack2 := @caughtexception")) {
        for (Value use : s.getUses()) {
          if (use instanceof JCaughtExceptionRef) {
            value = use;
            stmt = s;
          }
        }
      }
    }
    actual = evalFunction.evaluate(typing, value, stmt, graph);
    Assert.assertEquals(expected, actual);

    final Body.BodyBuilder builder2 = createMethodsBuilder("caughtException2", "void");
    StmtGraph<?> graph2 = builder2.getStmtGraph();

    for (Stmt s : graph2.getStmts()) {
      if (s.toString().equals("$stack2 := @caughtexception")) {
        for (Value use : s.getUses()) {
          if (use instanceof JCaughtExceptionRef) {
            value = use;
            stmt = s;
          }
        }
      }
    }
    expected = identifierFactory.getClassType("java.lang.RuntimeException");
    actual = evalFunction.evaluate(typing, value, stmt, graph2);
    Assert.assertEquals(expected, actual);

    final Body.BodyBuilder builder3 = createMethodsBuilder("fieldRef", "void");
    StmtGraph<?> graph3 = builder3.getStmtGraph();

    for (Stmt s : graph3.getStmts()) {
      if (s.toString().equals("l1 = l0.<ByteCodeTypeTest: A field>")) {
        for (Value use : s.getUses()) {
          if (use instanceof JFieldRef) {
            value = use;
            stmt = s;
            expected = identifierFactory.getClassType("A");
            actual = evalFunction.evaluate(typing, value, stmt, graph3);
            Assert.assertEquals(expected, actual);
          }
        }
      } else if (s.toString().equals("l0 := @this: ByteCodeTypeTest")) {
        for (Value use : s.getUses()) {
          if (use instanceof JThisRef) {
            value = use;
            stmt = s;
            expected = identifierFactory.getClassType("ByteCodeTypeTest");
            actual = evalFunction.evaluate(typing, value, stmt, graph3);
            Assert.assertEquals(expected, actual);
          }
        }
      }
    }
  }
}
