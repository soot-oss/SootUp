package tfx;

import java.lang.reflect.Method;

/** Method looked up on Base, invoked on a Sub receiver. */
public class InvokeOnSubclass {
  public static void main(String[] args) throws Exception {
    Method m = Base.class.getMethod("foo", Object.class);
    Base recv = new Sub();
    m.invoke(recv, new Object());
  }
}
