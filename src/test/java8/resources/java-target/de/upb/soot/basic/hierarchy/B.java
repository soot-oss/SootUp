package de.upb.soot.basic.hierarchy;

class B extends C {

  // Returns a string literal
  public String methodB() {
    B b1 = new B();
    System.out.println("method of Class B " + b1.methodC());
    return "method of Class B";
  }
}