package cornerCases;

import benchmark.internal.Benchmark;
import benchmark.objects.A;
import benchmark.objects.B;

/*
 * @testcase ObjectSensitivity1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Object sensitive alias from caller object
 */
public class ObjectSensitivity1 {

  public static void main(String[] args) {

    B b1 = new B();
    Benchmark.alloc(1);
    B b2 = new B();

    A a1 = new A(b1);
    A a2 = new A(b2);

    B b3 = a1.getF();
    B b4 = a2.getF();

//    Benchmark
//        .test(
//            "b4",
//            "{allocId:1, mayAlias:[b4,b2], notMayAlias:[a1,a2,b1,b3], mustAlias:[b4,b2], notMustAlias:[a1,a2,b1,b3]}");
  }
}
