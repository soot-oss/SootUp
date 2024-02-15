package sootup.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import categories.TestCategories;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Kaustubh Kelkar */
@Tag(TestCategories.JAVA_8_CATEGORY)
public class ReferencingThisTest extends MinimalBytecodeTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "thisMethod", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * ReferencingThis(){
   * this(10,20);
   * System.out.println("this() to invoke current class constructor");
   * }
   * ReferencingThis getObject(){
   * System.out.println("'this' keyword to return the current class instance");
   * return this;
   * }
   * void show(){
   * System.out.println("'this' keyword as method parameter");
   * thisDisplay(this);
   * }
   * void thisMethod(){
   * System.out.println(" this keyword as an argument in the constructor call");
   * ReferencingThis obj= new ReferencingThis(this.a, this.b);
   * obj.show();
   * }
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: ReferencingThis",
            "$stack2 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack2.<java.io.PrintStream: void println(java.lang.String)>(\" this keyword as an argument in the constructor call\")",
            "$stack5 = new ReferencingThis",
            "$stack4 = l0.<ReferencingThis: int a>",
            "$stack3 = l0.<ReferencingThis: int b>",
            "specialinvoke $stack5.<ReferencingThis: void <init>(int,int)>($stack4, $stack3)",
            "l1 = $stack5",
            "virtualinvoke l1.<ReferencingThis: void show()>()",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
