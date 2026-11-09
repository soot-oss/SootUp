package tfx;

/** Deprecated Class.newInstance. */
public class LegacyNewInstance {
  @SuppressWarnings("deprecation")
  public static void main(String[] args) throws Exception {
    Plugin p = (Plugin) Class.forName("tfx.PluginA").newInstance();
    p.start();
  }
}
