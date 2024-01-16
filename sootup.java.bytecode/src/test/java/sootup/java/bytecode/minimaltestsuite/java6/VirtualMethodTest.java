package sootup.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
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

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class VirtualMethodTest extends MinimalBytecodeTestSuiteBase {

  // @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "virtualMethodDemo", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * public int getSalary(){ return salary;}
   * public int getSalary(){
   * return super.getSalary()+bonus;
   * }
   * public int getSalary(){
   * return super.getSalary()+raise;
   * }
   * public void virtualMethodDemo(){
   * Employee e1= new TempEmployee(1500,150);
   * Employee e2= new RegEmployee(1500,500);
   * System.out.println(e1.getSalary());
   * System.out.println(e2.getSalary());
   * }
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: VirtualMethod",
            "$stack3 = new TempEmployee",
            "specialinvoke $stack3.<TempEmployee: void <init>(int,int)>(1500, 150)",
            "l1 = $stack3",
            "$stack4 = new RegEmployee",
            "specialinvoke $stack4.<RegEmployee: void <init>(int,int)>(1500, 500)",
            "l2 = $stack4",
            "$stack5 = <java.lang.System: java.io.PrintStream out>",
            "$stack6 = virtualinvoke l1.<Employee: int getSalary()>()",
            "virtualinvoke $stack5.<java.io.PrintStream: void println(int)>($stack6)",
            "$stack7 = <java.lang.System: java.io.PrintStream out>",
            "$stack8 = virtualinvoke l2.<Employee: int getSalary()>()",
            "virtualinvoke $stack7.<java.io.PrintStream: void println(int)>($stack8)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
