package de.upb.sootup.basic.expr;

public class InstanceOf {

  void sth() {

    Integer someNumber = new Integer(42);
    boolean b = someNumber instanceof Object;
    System.out.println("is someNumber an Object: " + b);

  }

}
