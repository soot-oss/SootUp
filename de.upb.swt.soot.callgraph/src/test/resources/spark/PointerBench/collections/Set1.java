package collections;

import java.util.HashSet;

import benchmark.internal.Benchmark;
import benchmark.objects.A;

/*
 * @testcase Set1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description HashSet
 */
public class Set1 {

  public static void main(String[] args) {

    HashSet<A> set = new HashSet<A>();
    A a = new A();
    A c = null;
    Benchmark.alloc(1);
    A b = new A();
    set.add(a);
    set.add(b);
    for (A i : set) {
      c = i;
      break;
    }
    a = null;
//    Benchmark.test("c",
//        "{allocId:1, mayAlias:[c], notMayAlias:[a,b,set], mustAlias:[c], notMustAlias:[a,b,set]}");
//    Benchmark.use(c);
  }
}
