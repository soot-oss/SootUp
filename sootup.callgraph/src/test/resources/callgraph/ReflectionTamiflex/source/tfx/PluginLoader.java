package tfx;

/** One reflective allocation site, two runtime targets; site lives in a helper method. */
public class PluginLoader {
  public static void main(String[] args) throws Exception {
    for (String name : new String[] {"tfx.PluginA", "tfx.PluginB"}) {
      load(name).start();
    }
  }

  static Plugin load(String name) throws Exception {
    return (Plugin) Class.forName(name).getDeclaredConstructor().newInstance();
  }
}
