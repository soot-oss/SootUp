package de.upb.swt.soot.test.java.bytecode.interceptors.typeresolving;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.expr.*;
import de.upb.swt.soot.core.jimple.common.ref.JCaughtExceptionRef;
import de.upb.swt.soot.core.jimple.common.ref.JFieldRef;
import de.upb.swt.soot.core.jimple.common.ref.JThisRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.VoidType;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.AugEvalFunction;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.Typing;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.JavaSootMethod;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.views.JavaView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class AugEvalFunctionTest {

  JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
  Typing typing = new Typing(new ArrayList<>());
  ClassType classType;
  SootClass clazz;
  JavaView view;
  AugEvalFunction evalFunction;

  Body body;

  @Before
  public void setup() {
    String baseDir = "../shared-test-resources/bytecodeTestSuite/";
    JavaClassPathAnalysisInputLocation analysisInputLocation =
        new JavaClassPathAnalysisInputLocation(baseDir);
    JavaClassPathAnalysisInputLocation rtJar =
        new JavaClassPathAnalysisInputLocation(System.getProperty("java.home") + "/lib/rt.jar");
    JavaProject project =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(analysisInputLocation)
            .addInputLocation(rtJar)
            .build();
    view = project.createOnDemandView();
    classType = identifierFactory.getClassType("ByteCodeTypeTest");
    clazz = view.getClass(classType).get();
    evalFunction = new AugEvalFunction(view);
  }

  @Test
  public void testImmediate() {
    Type actual;
    Type expected = identifierFactory.getClassType("java.lang.String");
    Stmt stmt = null;
    Value value = null;
    setMethodBody("constant", "void");
    for (Stmt s : body.getStmts()) {
      if (s.toString().equals("l1 = \"example\"")) {
        value = s.getUses().get(0);
        stmt = s;
      }
    }
    actual = evalFunction.evaluate(typing, value, stmt, body);
    Assert.assertEquals(expected, actual);

    setMethodBody("reflection", "void");
    for (Stmt s : body.getStmts()) {
      if (s.toString().equals("l1 = class \"LA;\"")) {
        value = s.getUses().get(0);
        stmt = s;
      }
    }
    expected = identifierFactory.getClassType("java.lang.Class");
    actual = evalFunction.evaluate(typing, value, stmt, body);
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testExpr() {
    Type actual;
    Type expected = PrimitiveType.getInt();
    Stmt stmt = null;
    Value value = null;
    setMethodBody("condition", "void");
    for (Stmt s : body.getStmts()) {
      if (s.toString().equals("if l1 >= l2")) {
        for (Value use : s.getUses()) {
          if (use instanceof AbstractConditionExpr) {
            value = use;
            stmt = s;
          }
        }
      }
    }
    actual = evalFunction.evaluate(typing, value, stmt, body);
    Assert.assertEquals(expected, actual);

    setMethodBody("length", "void");
    for (Stmt s : body.getStmts()) {
      if (s.toString().equals("l2 = lengthof l1")) {
        for (Value use : s.getUses()) {
          if (use instanceof JLengthExpr) {
            value = use;
            stmt = s;
          }
        }
      }
    }
    actual = evalFunction.evaluate(typing, value, stmt, body);
    Assert.assertEquals(expected, actual);

    setMethodBody("instanceOf", "boolean");
    for (Stmt s : body.getStmts()) {
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
    actual = evalFunction.evaluate(typing, value, stmt, body);
    Assert.assertEquals(expected, actual);

    setMethodBody("arrayRef", "void");
    for (Stmt s : body.getStmts()) {
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
    actual = evalFunction.evaluate(typing, value, stmt, body);
    Assert.assertEquals(expected, actual);

    setMethodBody("invoke", "void");
    for (Stmt s : body.getStmts()) {
      if (s.toString().equals("specialinvoke $stack2.<A: void <init>()>()")) {
        for (Value use : s.getUses()) {
          if (use instanceof AbstractInvokeExpr) {
            value = use;
            stmt = s;
            expected = VoidType.getInstance();
            actual = evalFunction.evaluate(typing, value, stmt, body);
            Assert.assertEquals(expected, actual);
          }
        }
      } else if (s.toString().equals("$stack3 = virtualinvoke l1.<A: B method()>()")) {
        for (Value use : s.getUses()) {
          if (use instanceof AbstractInvokeExpr) {
            value = use;
            stmt = s;
            expected = identifierFactory.getClassType("B");
            actual = evalFunction.evaluate(typing, value, stmt, body);
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
    setMethodBody("caughtException1", "void");
    for (Stmt s : body.getStmts()) {
      if (s.toString().equals("$stack2 := @caughtexception")) {
        for (Value use : s.getUses()) {
          if (use instanceof JCaughtExceptionRef) {
            value = use;
            stmt = s;
          }
        }
      }
    }
    actual = evalFunction.evaluate(typing, value, stmt, body);
    Assert.assertEquals(expected, actual);

    setMethodBody("caughtException2", "void");
    for (Stmt s : body.getStmts()) {
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
    actual = evalFunction.evaluate(typing, value, stmt, body);
    Assert.assertEquals(expected, actual);

    setMethodBody("fieldRef", "void");
    for (Stmt s : body.getStmts()) {
      if (s.toString().equals("l1 = l0.<ByteCodeTypeTest: A field>")) {
        for (Value use : s.getUses()) {
          if (use instanceof JFieldRef) {
            value = use;
            stmt = s;
            expected = identifierFactory.getClassType("A");
            actual = evalFunction.evaluate(typing, value, stmt, body);
            Assert.assertEquals(expected, actual);
          }
        }
      } else if (s.toString().equals("l0 := @this: ByteCodeTypeTest")) {
        for (Value use : s.getUses()) {
          if (use instanceof JThisRef) {
            value = use;
            stmt = s;
            expected = identifierFactory.getClassType("ByteCodeTypeTest");
            actual = evalFunction.evaluate(typing, value, stmt, body);
            Assert.assertEquals(expected, actual);
          }
        }
      }
    }
  }

  private void setMethodBody(String methodName, String returnType) {
    MethodSignature methodSignature =
        identifierFactory.getMethodSignature(
            classType, methodName, returnType, Collections.emptyList());
    Optional<JavaSootMethod> methodOptional = clazz.getMethod(methodSignature.getSubSignature());
    JavaSootMethod method = methodOptional.get();
    body = method.getBody();
  }
}
