package cornerCases;

import benchmark.internal.Benchmark;
import benchmark.objects.A;
import benchmark.objects.B;

/*
 * @testcase StrongUpdate1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Indirect alias of a.f and b.f through alias of a and b
 */
public class StrongUpdate2 {

  public static void main(String[] args) {

    A a = new A();
    A b = a;
    B x = b.f;
    Benchmark.alloc(1);
    a.f = new B();
    B y = b.f;
//    Benchmark.test("y",
//        "{allocId:1, mayAlias:[y], notMayAlias:[x], mustAlias:[y], notMustAlias:[x]}");
  }
}
