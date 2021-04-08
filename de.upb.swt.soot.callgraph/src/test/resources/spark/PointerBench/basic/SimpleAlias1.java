package basic;

import benchmark.internal.Benchmark;
import benchmark.objects.A;

/*
 * @testcase SimpleAlias1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Direct alias
 */
public class SimpleAlias1 {

  public static void main(String[] args) {

    Benchmark.alloc(1);
    A a = new A();

    A b = a;
    Benchmark.test("b",
        "{allocId:1, mayAlias:[a,b], notMayAlias:[], mustAlias:[a,b], notMustAlias:[]}");
  }
}
