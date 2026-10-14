public class LocalNamesCollisions {

  /** `i` is slot 2 in the first loop and slot 3 in the second. */
  public static int oneNameTwoSlots(int n) {
    int sum = 0;
    for (int i = 0; i < n; i++) {
      sum += i;
    }
    int k = 2;
    for (int i = 0; i < n; i++) {
      sum += i * k;
    }
    return sum;
  }

  /** As above, but `i_1` is a real variable: the second `i` has to skip to `i_2`. */
  public static int numberedNameTaken(int n) {
    int i_1 = n;
    for (int i = 0; i < n; i++) {
      i_1 += i;
    }
    int k = 3;
    for (int i = 0; i < k; i++) {
      i_1 += i;
    }
    return i_1;
  }
}
