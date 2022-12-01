package de.upb.sootup.concrete.nestedControlStatements;

public class NestedControlStatements {

  private int[][] permutations3 = { { 1, 2, 3 }, { 2, 1, 3 }, { 3, 1, 2 }, { 1, 3, 2 }, { 2, 3, 1 }, { 3, 2, 1 } };

  public void nestedIfElse() {
    for (int i = 0; i < permutations3.length; i++) {
      int[] ints = permutations3[i];
      nestedIfElse(ints[0], ints[1], ints[2]);
    }
  }

  private void nestedIfElse(int a, int b, int c) {
    if (a < b) {
      System.out.println(1);
      if (b < c) {
        System.out.println(2);
      } else {
        System.out.println(3);
      }
    } else if (a < c) {
      System.out.println(4);
      if (b < c) {
        System.out.println(5);
      } else {
        System.out.println(6);
      }
    } else {
      System.out.println(7);
      if (b < c) {
        System.out.println(8);
      } else {
        System.out.println(9);
      }
    }
  }

  public void nestedSwitchIf() {
    for (int i = 0; i < permutations3.length; i++) {
      int[] ints = permutations3[i];
      nestedSwitchIf(ints[0], ints[1], ints[2]);
    }
  }

  private void nestedSwitchIf(int a, int b, int c) {
    if (a < b) {
      switch (a) {
        case 1:
          System.out.println(1);
        case 2:
          System.out.println(2);
        default:
          System.out.println(3);
      }
    } else if (a < c) {
      System.out.println(4);
      switch (a) {
        case 1:
          System.out.println(5);
        case 2:
          System.out.println(6);
        default:
          System.out.println(7);
      }
    } else {
      System.out.println(8);
      switch (a) {
        case 1:
          System.out.println(9);
        case 2:
          System.out.println(10);
        default:
          System.out.println(11);
      }
    }
  }

  public void nestedWhileIf() {
    for (int i = 0; i < permutations3.length; i++) {
      int[] ints = permutations3[i];
      nestedWhileIf(ints[0], ints[1], ints[2]);
    }
  }

  private void nestedWhileIf(int a, int b, int c) {
    // Nested while in if
    if (a < b) {
      System.out.println(1);
      while (a < b) {
        System.out.println(2);
        a++;
      }
    } else if (a < c) {
      System.out.println(3);
      while (a < b) {
        System.out.println(4);
        a++;
      }
    } else {
      System.out.println(5);
      while (a < b) {
        System.out.println(6);
        a++;
      }
    }
  }

  public void nestedForIf() {
    for (int i = 0; i < permutations3.length; i++) {
      int[] ints = permutations3[i];
      nestedForIf(ints[0], ints[1], ints[2]);
    }
  }

  private void nestedForIf(int a, int b, int c) {
    if (a < b) {
      System.out.println(1);
      for (int i = 0; i < a; i++) {
        System.out.println(2);
      }
    } else if (a < c) {
      System.out.println(3);
      for (int i = 0; i < a; i++) {
        System.out.println(4);
      }
    } else {
      System.out.println(5);
      for (int i = 0; i < a; i++) {
        System.out.println(6);
      }
    }
  }

  public void nestedSwitchSwitch() {
    for (int i = 0; i < permutations3.length; i++) {
      int[] ints = permutations3[i];
      nestedSwitchSwitch(ints[0], ints[1], ints[2]);
    }
  }

  private void nestedSwitchSwitch(int a, int b, int c) {
    // Nested switch in switch
    switch (a) {
      case 1:
        System.out.println(1);
        switch (b) {
          case 5:
            System.out.println(2);
          case 1:
            System.out.println(3);
          default:
            System.out.println(4);
        }
      case 2:
        System.out.println(5);
        switch (b) {
          case 5:
            System.out.println(6);
          case 1:
            System.out.println(7);
          default:
            System.out.println(8);
        }
      default:
        System.out.println(9);
        switch (b) {
          case 5:
            System.out.println(10);
          case 1:
            System.out.println(11);
          default:
            System.out.println(12);
        }
    }
  }

  public void nestedIfSwitch() {
    for (int i = 0; i < permutations3.length; i++) {
      int[] ints = permutations3[i];
      nestedIfSwitch(ints[0], ints[1], ints[2]);
    }
  }

  private void nestedIfSwitch(int a, int b, int c) {
    switch (a) {
      case 1:
        System.out.println(1);
        if (a < b) {
          System.out.println(2);
        } else if (a < c) {
          System.out.println(3);
        } else {
          System.out.println(4);
        }

      case 2:
        System.out.println(5);
        if (a < b) {
          System.out.println(6);
        } else if (a < c) {
          System.out.println(7);
        } else {
          System.out.println(8);
        }
      default:
        System.out.println(9);
        if (a < b) {
          System.out.println(10);
        } else if (a < c) {
          System.out.println(11);
        } else {
          System.out.println(12);
        }
    }
  }

  public void nestedWhileSwitch() {
    for (int i = 0; i < permutations3.length; i++) {
      int[] ints = permutations3[i];
      nestedWhileSwitch(ints[0], ints[1], ints[2]);
    }
  }

  private void nestedWhileSwitch(int a, int b, int c) {
    // Nested while in switch
    switch (a) {
      case 1:
        System.out.println(1);
        while (a < b) {
          a++;
          System.out.println(2);
        }
      case 2:
        System.out.println(3);
        while (a < b) {
          a++;
          System.out.println(4);
        }
      default:
        System.out.println(5);
        while (a < b) {
          a++;
          System.out.println(6);
        }
    }
  }

  public void nestedForSwitch() {
    for (int i = 0; i < permutations3.length; i++) {
      int[] ints = permutations3[i];
      nestedForSwitch(ints[0], ints[1], ints[2]);
    }
  }

  private void nestedForSwitch(int a, int b, int c) {
    // Nested for in switch
    switch (a) {
      case 1:
        System.out.println(1);
        for (int i = 0; i < a; i++) {
          System.out.println(2);
        }
      case 2:
        System.out.println(3);
        for (int i = 0; i < a; i++) {
          System.out.println(4);
        }
      default:
        System.out.println(5);
        for (int i = 0; i < a; i++) {
          System.out.println(6);
        }
    }
  }

  public void nestedWhileWhile() {
    for (int i = 0; i < permutations3.length; i++) {
      int[] ints = permutations3[i];
      nestedWhileWhile(ints[0], ints[1], ints[2]);
    }
  }

  private void nestedWhileWhile(int a, int b, int c) {
    // Nested while
    while (a < b) {
      System.out.println(1);
      while (b < c) {
        b++;
        System.out.println(2);
      }
      a++;
    }
  }

  public void nestedIfWhile() {
    for (int i = 0; i < permutations3.length; i++) {
      int[] ints = permutations3[i];
      nestedIfWhile(ints[0], ints[1], ints[2]);
    }
  }

  private void nestedIfWhile(int a, int b, int c) {
    // Nested if in while
    while (a < b) {
      System.out.println(1);
      if (a < b) {
        System.out.println(2);
      } else if (a < c) {
        System.out.println(3);
      } else {
        System.out.println(4);
      }
      a++;
    }
  }

  public void nestedSwitchWhile() {
    for (int i = 0; i < permutations3.length; i++) {
      int[] ints = permutations3[i];
      nestedSwitchWhile(ints[0], ints[1], ints[2]);
    }
  }

  private void nestedSwitchWhile(int a, int b, int c) {
    // Nested switch in while
    while (a < b) {
      System.out.println(1);
      switch (a) {
        case 1:
          System.out.println(2);
        case 2:
          System.out.println(3);
        default:
          System.out.println(4);
      }
      a++;
    }
  }

  public void nestedForWhile() {
    for (int i = 0; i < permutations3.length; i++) {
      int[] ints = permutations3[i];
      nestedForWhile(ints[0], ints[1], ints[2]);
    }
  }

  private void nestedForWhile(int a, int b, int c) {
    // Nested for in while
    while (a < b) {
      System.out.println(1);
      for (int i = 0; i < a; i++) {
        System.out.println(2);
      }
      a++;
    }
  }

  public void nestedIfFor() {
    for (int i = 0; i < permutations3.length; i++) {
      int[] ints = permutations3[i];
      nestedIfFor(ints[0], ints[1], ints[2]);
    }
  }

  private void nestedIfFor(int a, int b, int c) {
    // Nested if in for
    for (int i = 0; i < a; i++) {
      System.out.println(1);
      if (a < b) {
        System.out.println(2);
      } else if (a < c) {
        System.out.println(3);
      } else {
        System.out.println(4);
      }
    }
  }

  public void nested_switch_in_for() {
    int a = 5;
    int b = 0;
    for (int i = 0; i < a; i++) {
      b++;
      System.out.println(b);
      switch (b) {
        case 1:
          System.out.println("a");
        case 2:
          System.out.println("b");
        default:
          System.out.println(b);
      }
    }
  }

  public void nestedWhileFor() {
    int a = 5;
    int b = 3;
    for (int i = 0; i < a; i++) {
      System.out.println(i);
      while (a < b) {
        System.out.println("a is smaller than b");
        a++;
      }
    }
  }

  public void nestedSwitchFor() {
    int a = 5;
    for (int i = 0; i < a; i++) {
      switch (a) {
        case 1:
          System.out.println("foo");

        default:
          System.out.println(a);

      }
    }
  }

  public void nestedForFor() {
    int a = 5;
    for (int i = 0; i < a; i++) {
      for (int j = 0; j <= i; j++) {
        System.out.println(i + j);
      }
    }
  }
}
