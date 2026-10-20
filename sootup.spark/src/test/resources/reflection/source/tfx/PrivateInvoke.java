package tfx;

import java.lang.reflect.Method;

/** getDeclaredMethod + setAccessible on a private method. */
public class PrivateInvoke {
  public static void main(String[] args) throws Exception {
    Method m = Base.class.getDeclaredMethod("secret");
    m.setAccessible(true);
    Object r = m.invoke(new Base());
  }
}
