package collections;

import java.util.HashMap;

import benchmark.internal.Benchmark;
import benchmark.objects.A;

/*
 * @testcase Map1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description HashMap
 */
public class Map1 {

  public static void main(String[] args) {

    HashMap<String, A> map = new HashMap<String, A>();
    A a = new A();
    Benchmark.alloc(1);
    A b = new A();
    map.put("first", a);
    map.put("second", b);
    A c = map.get("second");
    Benchmark.test("c",
        "{allocId:1, mayAlias:[c,b], notMayAlias:[a,map], mustAlias:[c,b], notMustAlias:[a,map]}");
  }
}
