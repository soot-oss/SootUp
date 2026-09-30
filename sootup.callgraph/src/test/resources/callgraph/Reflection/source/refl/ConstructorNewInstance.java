package refl;

import java.lang.reflect.Constructor;

public class ConstructorNewInstance {
  public static void main(String[] args) throws Exception {
    Constructor<?> ctor = Class.forName(args[0]).getConstructor(Object.class);
    Service s = (Service) ctor.newInstance(new Object()); // resolved
    s.serve();
  }
}
