package vc5;

interface Greeter {
  default void greet() {}
}

class Base {
  // private: does not override Greeter.greet
  private void greet() {}
}

class Impl extends Base implements Greeter {}

class Class {
  public static void main(String[] args) {
    Greeter g = new Impl();
    g.greet();
  }
}
