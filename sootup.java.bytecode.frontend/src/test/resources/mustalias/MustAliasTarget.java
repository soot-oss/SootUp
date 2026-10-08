public class MustAliasTarget {

  static Object sink(Object a, Object b) {
    return a;
  }

  static Object copies() {
    Object a = new Object();
    Object b = a;
    Object c = new Object();
    return sink(b, c);
  }

  static Object merge(boolean p) {
    Object a = new Object();
    Object b;
    if (p) {
      b = a;
    } else {
      b = a;
    }
    return sink(a, b);
  }

  static Object loop(int n) {
    Object last = null;
    Object current = null;
    for (int i = 0; i < n; i++) {
      last = current;
      current = new Object();
    }
    return sink(last, current);
  }
}
