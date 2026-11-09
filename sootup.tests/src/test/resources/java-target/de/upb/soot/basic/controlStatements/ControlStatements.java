package de.upb.sootup.basic.controlStatements;

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

  public void simpleSwitchBreak(int a, int b, int c) {
    // Simple switch case
    switch (a) {
      case 10:
        System.out.println("SWITCH CASE: a is equal to " + a + " and b is equal to " + b);
        break;
      case 20:
        System.out.println("SWITCH CASE: a is equal to " + a + "and b is equal to " + b);
        break;
      default:
        System.out.println("SWITCH DEFAULT: a is equal to " + a + " and b is equal to " + b);
    }
  }

  public void tableSwitch(int a) {
    // should generate a tableswitch
    switch (a) {
      case 0:
        System.out.println("zero");
      case 1:
        System.out.println("one");
      case 2:
        System.out.println("two");
      case 3:
        System.out.println("three");
      case 4:
        System.out.println("four");
      default:
        System.out.println("unspecified");
    }
  }

  public void lookupSwitch(int a) {
    // should generate a lookupswitch
    switch (a) {
      case 1:
        System.out.println("one");
      case 10:
        System.out.println("ten");
      case 1000:
        System.out.println("thousand");
      case 10000000:
        System.out.println("a lot");
      default:
        System.out.println("unspecified");
    }
  }

  public void gotoStmt() {

    label1: while (true) {
      if (false) {
        continue label1;
      }
    }

  }

  public void throwSth() throws Exception {

    throw new Exception("Banana");

  }

  public void throwAndCatch() {

    try {
      throw new Exception("Banana");
    } catch (Exception e) {
      System.out.println("Exception catched");
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

  public void monitor() {

    StringBuffer sb = new StringBuffer();
    synchronized (sb) {
      sb.append("monitored");
    }
    sb.append("unmonitored");
    System.out.println(sb.toString());
  }

}
