package refl;

import java.lang.reflect.Method;

public class MethodInvokeStatic {
  public static void main(String[] args) throws Exception {
    Method m = Target.class.getMethod("sid", Object.class);
    Object r = m.invoke(null, new Object()); // resolved
  }
}
