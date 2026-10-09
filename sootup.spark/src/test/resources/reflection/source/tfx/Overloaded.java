package tfx;

import java.lang.reflect.Method;

/** Reflective sites in two overloads of the same name; log only names the method. */
public class Overloaded {
  public static void main(String[] args) throws Exception {
    run(new Base());
    run(new Base(), "x");
  }

  static void run(Base b) throws Exception {
    Base.class.getMethod("foo", Object.class).invoke(b, "p");
  }

  static void run(Base b, Object o) throws Exception {
    Base.class.getMethod("sbar", Object.class, Object.class).invoke(null, o, o);
  }
}
