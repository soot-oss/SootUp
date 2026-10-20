package tfx;

import java.lang.reflect.Constructor;

/** Two-arg constructor via Constructor.newInstance. */
public class CtorWithArgs {
  public static void main(String[] args) throws Exception {
    Constructor<Base> c = Base.class.getConstructor(Object.class, Object.class);
    Base b = c.newInstance("x", "y");
  }
}
