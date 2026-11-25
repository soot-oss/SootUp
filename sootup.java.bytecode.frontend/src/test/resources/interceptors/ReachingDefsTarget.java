public class ReachingDefsTarget {

  static void mayThrow() {}

  int constantAssignedInTryBlock(int a) {
    int c = a;
    int x = 3;
    try {
      mayThrow();
      c = x + 4;
      mayThrow();
    } catch (RuntimeException e) {
      // c is still a if the first call throws
    }
    return c;
  }
}
