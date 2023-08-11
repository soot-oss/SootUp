package sootup.java.bytecode.minimaltestsuite.java14;

import categories.Java9Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;

/** @author Jonas Klauke */
@Category(Java9Test.class)
public class RecordTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public JavaClassType getDeclaredClassSignature() {
    return JavaIdentifierFactory.getInstance().getClassType("RecordTest");
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
            "l0 := @this: RecordTest",
            "l1 := @parameter0: java.lang.Object",
            "$stack2 = dynamicinvoke \"equals\" <boolean (RecordTest,java.lang.Object)>(l0, l1) <java.lang.runtime.ObjectMethods: java.lang.Object bootstrap(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.invoke.TypeDescriptor,java.lang.Class,java.lang.String,java.lang.invoke.MethodHandle[])>(class \"LRecordTest;\", \"a;b\", handle: <RecordTest: int a>, handle: <RecordTest: java.lang.String b>)",
            "return $stack2")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
