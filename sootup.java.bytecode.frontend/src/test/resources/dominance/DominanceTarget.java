public class DominanceTarget {

  static int diamond(int x) {
    int r;
    if (x > 0) {
      r = 1;
    } else {
      r = 2;
    }
    return r;
  }

  static int multipleExits(int x) {
    if (x > 0) {
      return 1;
    }
    if (x < 0) {
      throw new IllegalArgumentException();
    }
    return 0;
  }

  static int tryCatch(int x) {
    try {
      x = x / 2;
    } catch (ArithmeticException e) {
      return -1;
    }
    return x;
  }

  static int spin(int x) {
    if (x > 0) {
      return x;
    }
    while (true) {
      x++;
    }
  }
}
