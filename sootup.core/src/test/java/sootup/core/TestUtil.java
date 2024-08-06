package sootup.core;

import java.util.Collections;
import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.SimpleStmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JInterfaceInvokeExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.FieldSubSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType.IntType;

public class TestUtil {

  /**
   * creates a dummy SimpleStmtPositionInfo lineNumber 1
   *
   * @return a dummy SimpleStmtPositionInfo
   */
  public static SimpleStmtPositionInfo createDummySimpleStmtPositionInfo() {
    return new SimpleStmtPositionInfo(1);
  }

  /**
   * creates a dummy method signature Class Type: dummy class type SubSignature: dummy method sub
   * signature
   *
   * @return a dummy method signature
   */
  public static MethodSignature createDummyMethodSignature() {
    return new MethodSignature(
        TestUtil.createDummyClassType(), TestUtil.createDummyMethodSubSignature());
  }

  /**
   * creates a dummy method sub signature name: test return type: int parameter list: empty
   *
   * @return a dummy method sub signature
   */
  public static MethodSubSignature createDummyMethodSubSignature() {
    return new MethodSubSignature("test", Collections.emptyList(), IntType.getInstance());
  }

  /**
   * creates a dummy field signature Class Type: dummy class type SubSignature: dummy field sub
   * signature
   *
   * @return a dummy field signature
   */
  public static FieldSignature createDummyFieldSignature() {
    return new FieldSignature(
        TestUtil.createDummyClassType(), TestUtil.createDummyFieldSubSignature());
  }

  /**
   * creates a dummy field sub signature name: test type: int
   *
   * @return a dummy field sub signature
   */
  public static FieldSubSignature createDummyFieldSubSignature() {
    return new FieldSubSignature("test", IntType.getInstance());
  }

  /**
   * creates a dummy Local for an Object Name a Type dummy class type
   *
   * @return a dummy Local for an Object
   */
  public static Local createDummyLocalForObject() {
    return new Local("a", TestUtil.createDummyClassType());
  }

  /**
   * creates a dummy Local for an Int Name b Type int
   *
   * @return a dummy Local for a int value
   */
  public static Local createDummyLocalForInt() {
    return new Local("b", IntType.getInstance());
  }

  /**
   * will return a dummy virtual invoke expression local is called a and has the class type Test
   * method signature is &lt;Test: int test ()&gt; arguments is an empty list
   */
  public static JVirtualInvokeExpr createDummyVirtualInvokeExpr() {
    Local local = TestUtil.createDummyLocalForObject();
    MethodSignature methodSignature = TestUtil.createDummyMethodSignature();
    return new JVirtualInvokeExpr(local, methodSignature, Collections.emptyList());
  }

  /**
   * will return a dummy special invoke expression local is called a and has the class type Test
   * method signature is &lt;Test: int test ()&gt; arguments is an empty list
   */
  public static JSpecialInvokeExpr createDummySpecialInvokeExpr() {
    Local local = TestUtil.createDummyLocalForObject();
    MethodSignature methodSignature = TestUtil.createDummyMethodSignature();
    return new JSpecialInvokeExpr(local, methodSignature, Collections.emptyList());
  }

  /**
   * will return a dummy interface invoke expression local is called a and has the class type Test
   * method signature is &lt;Test: int test ()&gt; arguments is an empty list
   */
  public static JInterfaceInvokeExpr createDummyInterfaceInvokeExpr() {
    Local local = TestUtil.createDummyLocalForObject();
    MethodSignature methodSignature = TestUtil.createDummyMethodSignature();
    return new JInterfaceInvokeExpr(local, methodSignature, Collections.emptyList());
  }

  /**
   * will return a dummy static invoke expression method signature is &lt;Test: int test ()&gt;
   * arguments is an empty list
   */
  public static JStaticInvokeExpr createDummyStaticInvokeExpr() {
    MethodSignature methodSignature = TestUtil.createDummyMethodSignature();
    return new JStaticInvokeExpr(methodSignature, Collections.emptyList());
  }

  /**
   * creates a dummy static field reference Signature: dummy Field Signature
   *
   * @return a dummy JStaticFieldRef
   */
  public static JStaticFieldRef createDummyStaticFieldRef() {
    return new JStaticFieldRef(TestUtil.createDummyFieldSignature());
  }

  /**
   * creates a dummy instance field reference local: dummy local Signature: dummy Field Signature
   *
   * @return a dummy JInstanceFieldRef
   */
  public static JInstanceFieldRef createDummyInstanceFieldRef() {
    return new JInstanceFieldRef(
        TestUtil.createDummyLocalForInt(), TestUtil.createDummyFieldSignature());
  }

  /**
   * creates a dummy Class type Classname Test Package name test Fully Qualified Name test.Test
   *
   * @return a dummy class type
   */
  public static ClassType createDummyClassType() {
    return new ClassType() {

      @Override
      public String getFullyQualifiedName() {
        return "Test";
      }

      @Override
      public String getClassName() {
        return "Test";
      }

      @Override
      public PackageName getPackageName() {
        return new PackageName("test");
      }
    };
  }

  /**
   * will return a dummy assignment statement with an invoke expression the left value will be the
   * dummy int local the right value will be the given invoke expression stmt position is the dummy
   * SimpleStatementPositionInfo
   *
   * @param invokeExpr the invokeExpr in the dummy assignment statement
   * @return a dummy JAssignStmt with a static invoke expression
   */
  public static JAssignStmt createDummyAssignStmtWithExpr(AbstractInvokeExpr invokeExpr) {
    Local local = TestUtil.createDummyLocalForInt();
    SimpleStmtPositionInfo pos = TestUtil.createDummySimpleStmtPositionInfo();
    return new JAssignStmt(local, invokeExpr, pos);
  }

  /**
   * will return a dummy assignment statement the left value will be the dummy static field ref the
   * right value will be the dummy local stmt position is the dummy SimpleStatementPositionInfo
   *
   * @return a dummy JAssignStmt with a static field ref on the left side
   */
  public static JAssignStmt createDummyAssignStmtWithStaticFieldRefLeft() {
    JFieldRef fieldRef = TestUtil.createDummyStaticFieldRef();
    SimpleStmtPositionInfo pos = TestUtil.createDummySimpleStmtPositionInfo();
    return new JAssignStmt(fieldRef, TestUtil.createDummyLocalForInt(), pos);
  }

  /**
   * will return a dummy assignment statement stmt position is the dummy SimpleStatementPositionInfo
   *
   * @param left defines the left value of the dummy assign statement
   * @param right defines the right value of the dummy assign statement
   * @return a dummy JAssignStmt with an instance field ref on the left side
   */
  public static JAssignStmt createDummyAssignStmt(LValue left, Value right) {
    SimpleStmtPositionInfo pos = TestUtil.createDummySimpleStmtPositionInfo();
    return new JAssignStmt(left, right, pos);
  }

  /**
   * will return a dummy assignment statement the right value will be the dummy local the left value
   * will be the dummy local stmt position is the dummy SimpleStatementPositionInfo
   *
   * @return a dummy JAssignStmt with a static field ref on the left side
   */
  public static JAssignStmt createDummyAssignStmtWithLocals() {
    SimpleStmtPositionInfo pos = TestUtil.createDummySimpleStmtPositionInfo();
    return new JAssignStmt(
        TestUtil.createDummyLocalForInt(), TestUtil.createDummyLocalForInt(), pos);
  }
}
