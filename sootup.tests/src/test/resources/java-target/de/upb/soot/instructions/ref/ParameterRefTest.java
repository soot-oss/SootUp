package de.upb.sootup.instructions.ref;

public class ParameterRefTest {
  void noParameter() {
    System.out.println("zero");
  }

  void oneParameter(int a) {
    System.out.println("a =" + a);
  }

  void moreParameter(int a, double b) {
    System.out.println("a =" + a + "b = " + b);
  }

  void moreParameter(int a, double b, String str) {
    System.out.println("a =" + a + " b = " + b + " str = " + str);
  }

}
