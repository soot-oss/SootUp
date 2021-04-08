package basic;

import benchmark.internal.Benchmark;
import benchmark.objects.A;
import benchmark.objects.B;

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
public class ReturnValue3 {

  public static A id(A x) {
    A y = new A();
    Benchmark.alloc(1);
    y.f = new B();
    return y;
  }

  public static void main(String[] args) {

    A a = new A();
    A b = id(a);
    B x = b.f;
    B y = a.f;
    Benchmark.test("x",
        "{allocId:1, mayAlias:[x], notMayAlias:[a,b,y], mustAlias:[x], notMustAlias:[a,b,y]}");
  }
}
