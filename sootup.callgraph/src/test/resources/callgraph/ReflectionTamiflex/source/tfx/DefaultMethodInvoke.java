package tfx;

/** Reflective call of an interface default method. */
public class DefaultMethodInvoke {
  public static void main(String[] args) throws Exception {
    Object r = Greeter.class.getMethod("greet").invoke(new PoliteGreeter());
  }
}
