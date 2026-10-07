package tfx;

import java.lang.reflect.Method;

/** getDeclaredMethods + name filter: one invoke site, several runtime targets. */
public class DeclaredMethodsLoop {
  public static void main(String[] args) throws Exception {
    Handlers h = new Handlers();
    for (Method m : Handlers.class.getDeclaredMethods()) {
      if (m.getName().startsWith("on")) {
        m.invoke(h);
      }
    }
  }
}
