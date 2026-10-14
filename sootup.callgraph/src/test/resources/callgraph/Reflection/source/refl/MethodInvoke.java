package refl;

import java.lang.reflect.Method;

public class MethodInvoke {
  public static void main(String[] args) throws Exception {
    Method m = Target.class.getMethod("id", Object.class);
    Object r = m.invoke(new Target(), new Object()); // resolved
  }
}
