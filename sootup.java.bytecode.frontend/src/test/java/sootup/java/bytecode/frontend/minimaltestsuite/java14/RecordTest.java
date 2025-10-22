package sootup.java.bytecode.frontend.minimaltestsuite.java14;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.PrimitiveType;
import sootup.java.bytecode.frontend.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;

/**
 * @author Jonas Klauke
 */
public class RecordTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public JavaClassType getDeclaredClassSignature() {
    return identifierFactory.getClassType("Record");
  }

  @Override
  public MethodSignature getMethodSignature() {
    System.out.println(getDeclaredClassSignature());
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(),
        "equals",
        "boolean",
        Collections.singletonList("java.lang.Object"));
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "this := @this: Record",
            "l1 := @parameter0: java.lang.Object",
            "$stack2 = dynamicinvoke \"equals\" <boolean (Record,java.lang.Object)>(this, l1) <java.lang.runtime.ObjectMethods: java.lang.Object bootstrap(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.invoke.TypeDescriptor,java.lang.Class,java.lang.String,java.lang.invoke.MethodHandle[])>(class \"LRecord;\", \"a;b\", methodhandle: \"REF_GET_FIELD\" <Record: int a>, methodhandle: \"REF_GET_FIELD\" <Record: java.lang.String b>)",
            "return $stack2")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    List<JDynamicInvokeExpr> dynamicInvokes =
        method.getBody().getStmts().stream()
            .filter(stmt -> stmt instanceof InvokableStmt)
            .map(stmt -> (InvokableStmt) stmt)
            .map(InvokableStmt::getInvokeExpr)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(abstractInvokeExpr -> abstractInvokeExpr instanceof JDynamicInvokeExpr)
            .map(abstractInvokeExpr -> (JDynamicInvokeExpr) abstractInvokeExpr)
            .toList();
    assertEquals(1, dynamicInvokes.size());
    JDynamicInvokeExpr invoke = dynamicInvokes.get(0);

    // test bootstrap args
    List<Immediate> bootTrapArgs = invoke.getBootstrapArgs();
    assertTrue(bootTrapArgs.contains(JavaJimple.newClassConstant("LRecord;")));
    assertTrue(bootTrapArgs.contains(JavaJimple.newStringConstant("a;b")));
    assertTrue(
        bootTrapArgs.contains(
            JavaJimple.newMethodHandle(
                new FieldSignature(
                    new JavaClassType("Record", new PackageName("")), "a", PrimitiveType.getInt()),
                1)));
    assertTrue(
        bootTrapArgs.contains(
            JavaJimple.newMethodHandle(
                new FieldSignature(
                    new JavaClassType("Record", new PackageName("")),
                    "b",
                    new JavaClassType("String", new PackageName("java.lang"))),
                1)));
  }
}
