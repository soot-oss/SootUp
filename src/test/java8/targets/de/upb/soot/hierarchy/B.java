package de.upb.soot.hierarchy;

class B extends C {

  // Returns a string literal
  public void methodB() {
    B b1 = new B();
    System.out.println("method of Class B " + b1.methodC());
  }
}