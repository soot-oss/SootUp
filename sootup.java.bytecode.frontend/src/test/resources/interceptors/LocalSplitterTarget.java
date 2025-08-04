public class LocalSplitterTarget {
  void simpleAssignment() {
    int a = 0;
    int b = 1;
    a = b + 1;
    b = a + 1;
  }

  void selfAssignment() {
    int a = 0;
    int b = 1;
    a = a + 1;
    b = b + 1;
  }

  int branch() {
    int a = 0;
    if (a < 0) {
      a = a + 1;
    } else {
      a = a - 1;
      a = a + 2;
    }
    return a;
  }

  int branchMoreLocals() {
    int a = 0;
    if (a < 0) {
      a = a + 1;
      a = a + 2;
      a = a + 3;
    } else {
      a = a - 1;
      a = a - 2;
      a = a - 3;
    }
    return a;
  }

  int branchMoreBranches() {
    int a = 0;
    if (a < 0) {
      a = a + 1;
      a = a + 2;
    } else {
      a = a - 1;
      a = a - 2;
    }

    if (a > 1) {
      a = a + 3;
      a = a + 5;
    } else {
      a = a - 3;
      a = a - 5;
    }

    return a;
  }

  int branchElseIf() {
    int a = 0;
    if (a < 0) {
      a = a + 1;
      a = a + 2;
    } else if (a < 5) {
      a = a - 1;
      a = a - 2;
    } else {
      a = a * 1;
      a = a * 2;
    }

    return a;
  }

  int forLoop() {
    int a = 0;
    for (int i = 0; i < 10; i++) {
      i = i + 1;
      a++;
    }
    return a;
  }

  void reusedLocals() {
    // Somewhat interesting test case since the compiler will store
    // both `b`s at the same local index
    // and both `a`s at the same local index.
    // This also works without them having the same name.
    // The case for `b` is particularly interesting,
    // since without splitting the local the `TypeAssigner` will have to assign a laxer type to `b`.
    {
      Object a;
      if (Math.random() == 0.0) {
        Integer b = 1;
        a = b;
      } else {
        String b = "";
        a = b;
      }
      System.out.println(a);
    }
    {
      Object a = null;
      System.out.println(a);
    }
  }

  Object traps() {
    int a = 1;
    try {
      a = 2;
    } catch (Throwable t) {
      return a;
    }

    String b = "";
    try {
      System.out.println();
    } catch (Throwable t) {
      return b;
    }

    return 0.0;
  }
}
