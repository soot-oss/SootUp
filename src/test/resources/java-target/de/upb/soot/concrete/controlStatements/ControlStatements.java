package de.upb.soot.concrete.controlStatements;

import java.util.ArrayList;

public class ControlStatements {

  public void simpleIfElseIfTakeThen() {
    int a = 1;
    int b = 2;
    int c = 3;
    if (a < b) {
      System.out.println(a);
    } else if (a < c) {
      System.out.println(b);
    } else {
      System.out.println(c);
    }
  }

  public void simpleIfElseIfTakeElseIf() {
    int a = 2;
    int b = 1;
    int c = 3;
    if (a < b) {
      System.out.println(a);
    } else if (a < c) {
      System.out.println(b);
    } else {
      System.out.println(c);
    }
  }

  public void simpleIfElseIfTakeElse() {
    int a = 3;
    int b = 2;
    int c = 1;
    if (a < b) {
      System.out.println(a);
    } else if (a < c) {
      System.out.println(b);
    } else {
      System.out.println(c);
    }
  }

  public void simpleIfElseTakeThen() {
    int a = 1;
    int b = 2;
    if (a < b) {
      System.out.println(a);
    } else {
      System.out.println(b);
    }
  }

  public void simpleIfElseTakeElse() {
    int a = 2;
    int b = 1;
    if (a < b) {
      System.out.println(a);
    } else {
      System.out.println(b);
    }
  }

  public void tableSwitch() {
    int a = 1;
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
    switch (a) {
      case 1:
        System.out.println(a);
      case 2:
        System.out.println(a);
      default:
        System.out.println(a);
    }
  }

  public void tableSwitchNoDefault() {
    int a = 3;
    switch (a) {
      case 1:
        System.out.println(a);
      case 2:
        System.out.println(a);
    }

    System.out.println(a);
  }

  public void lookupSwitch() {
    String a = "foo";
    switch (a) {
      case "foo":
        System.out.println(a);
      case "bar":
        System.out.println(a);
      default:
        System.out.println(a);
    }
  }

  public void lookupSwitchDefault() {
    String a = "baz";
    switch (a) {
      case "foo":
        System.out.println(a);
      case "bar":
        System.out.println(a);
      default:
        System.out.println(a);
    }
  }

  public void lookupSwitchNoDefault() {
    String a = "baz";
    switch (a) {
      case "foo":
        System.out.println(a);
      case "bar":
        System.out.println(a);
    }
    System.out.println(a);
  }

  public void simpleWhile() {
    int a = 1;
    int b = 2;
    while (a < b) {
      System.out.println(a);
      a++;
    }
  }

  public void simpleDoWhile() {
    int a = 1;
    int b = 2;
    do {
      System.out.println(a);
      a++;
    } while (a < b);
  }

  public void simpleFor() {
    int a = 1;
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
