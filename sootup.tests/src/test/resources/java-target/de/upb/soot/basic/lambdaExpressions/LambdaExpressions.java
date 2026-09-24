package de.upb.sootup.basic.lambdaExpressions;

// There is an present while generating a jimple output for LambdaExpressions, refer here: https://mailman.cs.mcgill.ca/pipermail/soot-list/2016-November/008612.html
abstract class LambdaExpressions {
  public static void lambdaTarget() {
    A a1 = (str) -> "Hello " + str + "!";
    A a2 = (str) -> "Hello " + str + "!";

    System.out.println(a1.methodA(7));
    System.out.println(a2.methodA(9));
  }
}
