package generalJava;

import benchmark.internal.Benchmark;
import benchmark.objects.A;

/*
 * @testcase Exception1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Alias in exception
 */
public class Exception1 {

  public static void main(String[] args) {

    Benchmark.alloc(1);
    A a = new A();
    A b = new A();

    try {
      b = a;
      //throw new RuntimeException();
    } catch (RuntimeException e) {
      //Benchmark.test("b",
      //    "{allocId:1, mayAlias:[a,b], notMayAlias:[], mustAlias:[a,b], notMustAlias:[]}");
    }

  }
}
