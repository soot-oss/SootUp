package cornerCases;

import benchmark.internal.Benchmark;
import benchmark.objects.A;

/*
 * @testcase ContextSensitivity2
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Object sensitive alias from caller object (2-CS)
 */
public class ContextSensitivity2 {

  public ContextSensitivity2() {}

  public void callee(A a, A b) {
//    Benchmark.test("b",
//        "{allocId:1, mayAlias:[a,b], notMayAlias:[], mustAlias:[a,b], notMustAlias:[]},"
//            + "{allocId:2, mayAlias:[a], notMayAlias:[b], mustAlias:[a], notMustAlias:[b]}");
  }

  public void test1() {
    Benchmark.alloc(1);
    A a1 = new A();
    A b1 = a1;
    test11(a1, b1);
  }

  private void test11(A a1, A b1) {
    callee(a1, b1);
  }

  public void test2() {
    A a2 = new A();
    Benchmark.alloc(2);
    A b2 = new A();
    test22(a2, b2);
  }

  private void test22(A a2, A b2) {
    callee(a2, b2);
  }

  public static void main(String[] args) {
    ContextSensitivity2 cs1 = new ContextSensitivity2();
    cs1.test1();
    cs1.test2();
  }
}
