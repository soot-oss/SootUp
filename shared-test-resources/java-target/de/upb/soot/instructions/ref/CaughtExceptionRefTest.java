package de.upb.soot.instructions.ref;

public class CaughtExceptionRefTest {

  void throwExceptionAndCatch() {

    try {
      throw new Exception();
    } catch (Exception e) {
      System.out.println("exception");
    }

  }

  void throwException() throws Exception {
    throw new Exception();
  }

}
