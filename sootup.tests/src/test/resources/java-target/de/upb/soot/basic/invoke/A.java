package de.upb.sootup.basic.invoke;

public class A {
  private String name;
  private int num;

  public A() {
  }

  public A(String name, int num) {
    this.name = name;
    this.num = num;
  }

  public static int methodA() {
    System.out.println("Method A called");
    return 0;
  }

  public static int methodB(String str) {
    System.out.println(str);
    return 0;
  }
}