package tfx;

/** 3-arg Class.forName with explicit loader + initialization. */
public class ForName3 {
  public static void main(String[] args) throws Exception {
    Class<?> c = Class.forName("tfx.PluginB", true, ForName3.class.getClassLoader());
    Object o = c.getDeclaredConstructor().newInstance();
  }
}
