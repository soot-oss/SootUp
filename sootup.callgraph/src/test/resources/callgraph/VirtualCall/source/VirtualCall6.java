package vc6;

public class Class {
  // package-private: vc6.q.Sub.m does not override it
  void m() {}

  public static void main(String[] args) {
    Class c = new vc6.q.Sub();
    c.m();
  }
}
