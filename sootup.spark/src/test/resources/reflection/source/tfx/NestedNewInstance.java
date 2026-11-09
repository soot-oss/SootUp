package tfx;

/** Binary name with '$' for a static nested class. */
public class NestedNewInstance {
  public static void main(String[] args) throws Exception {
    Plugin p = (Plugin) Class.forName("tfx.Outer$Inner").getDeclaredConstructor().newInstance();
    p.start();
  }
}
