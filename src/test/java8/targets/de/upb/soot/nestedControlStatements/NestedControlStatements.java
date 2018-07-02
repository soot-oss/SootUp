package de.upb.soot.nestedControlStatements;

public class NestedControlStatements {

  int a = 10;
  int b = 20;
  int c = 30;

  public void nested_switch_while(int a, int b, int c) {

    while (a < b) {
      // Nested: switch in while
      switch (a) {
        case 10:
          System.out.println("SWITCH CASE: " + a + " is smaller than " + b);
        default:
          System.out.println("SWITCH DEFAULT: " + a + " is smaller than " + b);
      }
    }
  }

  public void nested_if_while(int a, int b, int c) {

    while (a < b) {
      // Nested: if in while
      if (c > b) {
        System.out.println("c is greater than b");
      } else {
        System.out.println("b is greater than c");
      }
      b++;
    }
  }

  public void nested_for_while(int a, int b, int c) {

    while (a < b) {
      // Nested: for in while
      for (int j = 0; j <= a; j++) {
        System.out.println("for in while");
      }
    }
  }

  public void nested_if_for(int a, int b, int c) {
    // Nested: if in for
    for (int i = 0; i < a; i++) {
      if (b > a || c > b) {
        a++;
      } else if (a != b) {
        System.out.println("Not equal");
      } else if (a == b) {
        System.out.println("Equal");
      } else if (b > a && c > b) {
        c--;
      }
    }
  }

  public void nested_while_for(int a, int b, int c) {
    // Nested: while in for
    for (int i = 0; i < a; i++) {
      while (a < b) {
        System.out.println("a is smaller than b");
      }
    }
  }

  public void nested_switch_for(int a, int b, int c) {
    // Nested: switch in for
    for (int i = 0; i < a; i++) {
      switch (a) {
        case 10:
          System.out.println("SWITCH CASE: " + a + " is smaller than " + b);
        default:
          System.out.println("SWITCH DEFAULT: " + a + " is smaller than " + b);
      }
    }
  }

  public void nested_for_for(int a, int b, int c) {
    // Nested: for in for
    for (int i = 0; i < a; i++) {
      for (int j = 0; j <= i; j++) {
        System.out.println("for in for");
      }
    }
  }
}
