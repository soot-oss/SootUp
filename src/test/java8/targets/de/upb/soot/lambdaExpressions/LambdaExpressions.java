package de.upb.soot.lambdaExpressions;

abstract class LambdaExpressions {
  public static void lambdaTarget() {
    A a1 = (str) -> "Hello " + str + "!";
    A a2 = (str) -> "Hello " + str + "!";

    System.out.println(a1.methodA(7));
    System.out.println(a2.methodA(9));
  }
}
