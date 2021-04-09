package basic;

import benchmark.internal.Benchmark;
import benchmark.objects.A;

/*
 * @testcase Branching1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Condition. a and b alias on one path, not on the other
 */
public class Branching1 {

  public static void main(String[] args) {
    int i = 0;

    Benchmark.alloc(1);
    A a = new A();
    Benchmark.alloc(2);
    A b = new A();

    if (i < 0)
      a = b;

//    Benchmark.test("a",
//        "{allocId:1, mayAlias:[a], notMayAlias:[i,b], mustAlias:[a], notMustAlias:[i,b]},"
//            + "{allocId:2, mayAlias:[a,b], notMayAlias:[i], mustAlias:[a], notMustAlias:[i,b]}");
  }
}
