package basic;

import benchmark.internal.Benchmark;
import benchmark.objects.A;

/*
 * @testcase ParameterAlias1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Aliasing through static method parameter
 */
public class Parameter1 {

  public static void test(A x) {
    A b = x;
    Benchmark.test("b",
        "{allocId:1, mayAlias:[b,x], notMayAlias:[], mustAlias:[b,x], notMustAlias:[]}");
  }

  public static void main(String[] args) {

    Benchmark.alloc(1);
    A a = new A();
    test(a);
  }
}
