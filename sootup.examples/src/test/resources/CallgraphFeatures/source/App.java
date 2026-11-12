public class App {

  static final Object LOCK = new Object(); // static field initializer: App.<clinit>

  public static void main(String[] args) throws Exception {
    Registry.register(); // runs Registry.<clinit> first

    Runnable task = () -> Helper.work(); // invokedynamic: lambda
    task.run();

    Plugin plugin = (Plugin) Class.forName("MyPlugin").newInstance(); // reflection
    plugin.start();
  }
}
