package de.upb.sootup.instructions.expr;

public class InstanceOfExprTest {

  void isObject() {

    Integer someNumber = new Integer(42);
    boolean b = someNumber instanceof Object;
    System.out.println("is someNumber an Object: " + b);

  }

  void isInteger() {

    Integer someNumber = new Integer(42);
    boolean b = someNumber instanceof Integer;
    System.out.println("is someNumber an Integer: " + b);

  }

}
