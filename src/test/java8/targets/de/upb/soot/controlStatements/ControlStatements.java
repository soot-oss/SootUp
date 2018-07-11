package de.upb.soot.controlStatements;

public class ControlStatements {

  public void simpleIfElse(int a, int b, int c) {
    // Simple if else
    if (a < b) {
      System.out.println("IF: " + a + " is smaller than " + b);
    } else if (a < c) {
      System.out.println("ELSE IF: " + a + " is smaller than " + c);
    } else {
      System.out.println("ELSE: " + a + " is greater than " + b + " and " + c);
    }
  }

  public void simpleSwitchCase(int a, int b, int c) {
    // Simple switch case
    switch (a) {
      case 10:
        System.out.println("SWITCH CASE: a is equal to " + a + " and b is equal to " + b);
      case 20:
        System.out.println("SWITCH CASE: a is equal to " + a + "and b is equal to " + b);
      default:
        System.out.println("SWITCH DEFAULT: a is equal to " + a + " and b is equal to " + b);
    }
  }

  public void simpleWhile(int a, int b) {
    // Simple while
    while (a < b) {
      System.out.println("a is smaller than b");
    }
  }

  public void simpleDoWhile(int a, int b) {
    // Simple do while
    do {
      System.out.println("a is " + a);
    } while (a < b);
  }

  public void simpleFor(int a, int b) {
    // Simple for
    for (int i = 0; i < a; i++) {
      b++;
      System.out.println("FOR: Value of b is " + b);
    }
  }
}
