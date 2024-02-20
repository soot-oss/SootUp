public class Misc {
  public static void arraysMixedAssignment() {
    int[] a = new int[7];
    a[0] = 0;
    a[1] = 2;
    a[2] = -128;
    a[3] = 128;
    a[4] = -32768;
    a[5] = 32768;
    a[6] = 65536;
  }

  public static void arrayAssignBeforeInit() {
    Object[] a = null;
    if (Math.random() == 0.0) {
      // This assignment is before (in source code) the initialization below,
      // so the `TypeResolver` needs to find the type of `a` before a direct assignment to `a`.
      a[0] = null;
      a[0] = "";
    } else {
      a = new Object[1];
    }
  }

  public static void nullArray() {
    Object[] a = null;
    a[0] = null;
  }
}
