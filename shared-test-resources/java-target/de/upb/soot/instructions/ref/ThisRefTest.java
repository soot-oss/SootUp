package de.upb.sootup.instructions.ref;

public class ThisRefTest {
  int property = 0;

  String someMethod() {
    return "sth";
  }

  void caller() {
    System.out.println(this.property);
    System.out.println(this.someMethod());
  }

}
