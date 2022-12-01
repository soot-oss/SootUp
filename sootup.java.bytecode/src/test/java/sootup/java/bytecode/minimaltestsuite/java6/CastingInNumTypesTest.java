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
public class CastingInNumTypesTest extends MinimalBytecodeTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "displayNum", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * public void displayNum(){
   * byte num1 =1;
   * short num2=2;
   * int num3= 3;
   * long num4=4551598461l;
   * float num5= 5.4f;
   * double num6= 4551595484654646464654684664646846713431.265;
   *
   * System.out.println(num1);
   * System.out.println((byte)num3);
   * System.out.println((double)num2);
   * System.out.println((short)num4);
   * System.out.println((double)num5);
   * System.out.println((int)num4);
   * System.out.println((float) num6);
   * System.out.println(num6);
   *
   * double d = 4786777867867868654674678346734763478673478654478967.77;
   * System.out.println((float)d);
   * System.out.println((long)d);
   * System.out.println((int)d);
   * System.out.println((short)d);
   * System.out.println((byte)d);
   *
   * }
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: CastingInNumTypes",
            "l1 = 1",
            "l2 = 2",
            "l3 = 3",
            "l4 = 4551598461L",
            "l6 = 5.4F",
            "l7 = 4.5515954846546467E39",
            "$stack11 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack11.<java.io.PrintStream: void println(int)>(l1)",
            "$stack13 = <java.lang.System: java.io.PrintStream out>",
            "$stack12 = (byte) l3",
            "virtualinvoke $stack13.<java.io.PrintStream: void println(int)>($stack12)",
            "$stack15 = <java.lang.System: java.io.PrintStream out>",
            "$stack14 = (double) l2",
            "virtualinvoke $stack15.<java.io.PrintStream: void println(double)>($stack14)",
            "$stack18 = <java.lang.System: java.io.PrintStream out>",
            "$stack16 = (int) l4",
            "$stack17 = (short) $stack16",
            "virtualinvoke $stack18.<java.io.PrintStream: void println(int)>($stack17)",
            "$stack20 = <java.lang.System: java.io.PrintStream out>",
            "$stack19 = (double) l6",
            "virtualinvoke $stack20.<java.io.PrintStream: void println(double)>($stack19)",
            "$stack22 = <java.lang.System: java.io.PrintStream out>",
            "$stack21 = (int) l4",
            "virtualinvoke $stack22.<java.io.PrintStream: void println(int)>($stack21)",
            "$stack24 = <java.lang.System: java.io.PrintStream out>",
            "$stack23 = (float) l7",
            "virtualinvoke $stack24.<java.io.PrintStream: void println(float)>($stack23)",
            "$stack25 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack25.<java.io.PrintStream: void println(double)>(l7)",
            "l9 = 4.7867778678678685E51",
            "$stack27 = <java.lang.System: java.io.PrintStream out>",
            "$stack26 = (float) l9",
            "virtualinvoke $stack27.<java.io.PrintStream: void println(float)>($stack26)",
            "$stack29 = <java.lang.System: java.io.PrintStream out>",
            "$stack28 = (long) l9",
            "virtualinvoke $stack29.<java.io.PrintStream: void println(long)>($stack28)",
            "$stack31 = <java.lang.System: java.io.PrintStream out>",
            "$stack30 = (int) l9",
            "virtualinvoke $stack31.<java.io.PrintStream: void println(int)>($stack30)",
            "$stack34 = <java.lang.System: java.io.PrintStream out>",
            "$stack32 = (int) l9",
            "$stack33 = (short) $stack32",
            "virtualinvoke $stack34.<java.io.PrintStream: void println(int)>($stack33)",
            "$stack37 = <java.lang.System: java.io.PrintStream out>",
            "$stack35 = (int) l9",
            "$stack36 = (byte) $stack35",
            "virtualinvoke $stack37.<java.io.PrintStream: void println(int)>($stack36)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
