package tfx;

import java.lang.reflect.Method;

/** Static target with two parameters. */
public class StaticTwoArgs {
  public static void main(String[] args) throws Exception {
    Method m = Base.class.getMethod("sbar", Object.class, Object.class);
    Object r = m.invoke(null, "a", "b");
  }
}
