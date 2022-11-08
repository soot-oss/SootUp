package de.upb.sootup.basic.hierarchy;

class B extends C {

  // Returns a string literal
  public String methodB() {
    B b1 = new B();
    System.out.println("methodRef of Class B " + b1.methodC());
    return "methodRef of Class B";
  }
}