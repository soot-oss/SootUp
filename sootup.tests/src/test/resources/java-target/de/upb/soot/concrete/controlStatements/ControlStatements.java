package de.upb.sootup.concrete.controlStatements;

import java.util.ArrayList;

public class ControlStatements {

  public void simpleIfElseIfTakeThen(int a, int b, int c) {
    if (a < b) {
      System.out.println(a);
    } else if (a < c) {
      System.out.println(b);
    } else {
      System.out.println(c);
    }
  }

  public boolean simpleIfElse(int a, int b) {
    if (a == b) {
      return true;
    } else {
      return false;
    }
  }

  public boolean simpleIfElse(boolean a, boolean b) {
    if (a != b) {
      return true;
    } else {
      return false;
    }
  }

  public boolean simpleIf(String s) {
    if (s == null)
      return false;
    return true;
  }

  public void simpleIfElseIfTakeElse(double a, double b, double c) {
    if (a < b) {
      System.out.println(a);
    } else if (a < c) {
      System.out.println(b);
    } else {
      System.out.println(c);
    }
  }

  public void simpleIfElseTakeThen(float a, float b) {
    if (a < b) {
      System.out.println(a);
    } else {
      System.out.println(b);
    }
  }

  public void simpleIfElseTakeElse(byte a, byte b) {
    if (a < b) {
      System.out.println(a);
    } else {
      System.out.println(b);
    }
  }

  public void tableSwitch(int a) {
    switch (a) {
      case 1:
        System.out.println(a);
      case 2:
        System.out.println(a);
      default:
        System.out.println(a);
    }
  }

  public void tableSwitchDefault() {
    int a = 3;
    int b = a - 1;
    switch (b) {
      case 1:
        System.out.println(a);
      case 2:
        System.out.println(a);
      default:
        System.out.println(a);
    }
  }

  public void tableSwitchNoDefault(int a) {
    switch (a) {
      case 1:
        System.out.println(a);
      case 2:
        System.out.println(a);
    }

    System.out.println(a);
  }

  public void lookupSwitch(String a) {
    switch (a) {
      case "foo":
        System.out.println(a);
      case "bar":
        System.out.println(a);
      default:
        System.out.println(a);
    }
  }

  public void lookupSwitchDefault(String a) {
    switch (a) {
      case "foo":
        System.out.println(a);
      case "bar":
        System.out.println(a);
      default:
        System.out.println(a);
    }
  }

  public void lookupSwitchNoDefault(String a) {
    switch (a) {
      case "foo":
        System.out.println(a);
      case "bar":
        System.out.println(a);
    }
    System.out.println(a);
  }

  public void simpleWhile(int a, int b) {
    while (a < b) {
      System.out.println(a);
      a++;
    }
  }

  public void simpleDoWhile(int a, int b) {
    do {
      System.out.println(a);
      a++;
    } while (a < b);
  }

  public void simpleFor(int a) {
    for (int i = 0; i < a; i++) {
      System.out.println(i);
    }
  }

  public void forIterArr() {
    int[] arr = new int[] { 1, 2, 3 };
    for (int i : arr) {
      System.out.println(i);
    }
  }

  public void forIterList() {
    ArrayList<String> list = new ArrayList<>();
    list.add("1");
    list.add("2");
    list.add("3");
    for (String s : list) {
      System.out.println(s);
    }
  }
}
