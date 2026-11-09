package refl;

public class Target {
  public Target() {}

  public Target(Object o) {}

  public Object id(Object o) {
    return o;
  }

  public static Object sid(Object o) {
    return o;
  }
}
