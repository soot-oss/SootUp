package tfx;

public class Base {
  public static Object staticField;
  public Object field;

  public Base() {}

  public Base(Object o, Object p) {}

  public Object foo(Object o) {
    return o;
  }

  public static Object sbar(Object a, Object b) {
    return b;
  }

  private Object secret() {
    return this;
  }
}
