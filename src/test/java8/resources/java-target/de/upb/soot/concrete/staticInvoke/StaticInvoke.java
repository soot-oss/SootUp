package de.upb.soot.concrete.staticInvoke;

public class StaticInvoke {

  public static void staticInvoke() {
    callee();
  }

  private static void callee() {
    System.out.println("foo");
  }
}