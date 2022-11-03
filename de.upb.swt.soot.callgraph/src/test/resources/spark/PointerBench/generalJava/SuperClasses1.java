package generalJava;

import benchmark.internal.Benchmark;
import benchmark.objects.A;
import benchmark.objects.P;

/*
 * @testcase SuperClass1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Alias from method in super class
 */
public class SuperClasses1 {

  private static void main(String[] args) {
    Benchmark.alloc(1);
    A a = new A();
    A b = new A();

    P p = new P(a);
    p.alias(b);
    A h = p.getA();
//    Benchmark.test("h",
//        "{allocId:1, mayAlias:[h,b], notMayAlias:[a,p], mustAlias:[b,a], notMustAlias:[p]}");
    Benchmark.use(h);
  }

}
