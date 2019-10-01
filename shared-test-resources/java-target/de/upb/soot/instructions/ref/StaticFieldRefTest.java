package de.upb.soot.instructions.ref;

public class StaticFieldRefTest {
  static int globalCounter = 0;

  void sth() {
    globalCounter++;
    System.out.println(globalCounter);
  }
}
