package basic;

import benchmark.internal.Benchmark;
import benchmark.objects.A;

/*
 * @testcase ReturnValue1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Alias to a return value from a static method
 */
public class ReturnValue2 {

  public ReturnValue2() {}

  public A id(A x) {
    return x;
  }

  public static void main(String[] args) {

    Benchmark.alloc(1);
    A a = new A();
    ReturnValue2 rv2 = new ReturnValue2();
    A b = rv2.id(a);
    Benchmark.test("b",
        "{allocId:1, mayAlias:[a,b], notMayAlias:[rv2], mustAlias:[a,b], notMustAlias:[rv2]}");
  }
}
