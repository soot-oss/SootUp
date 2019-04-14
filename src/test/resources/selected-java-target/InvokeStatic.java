
public class InvokeStatic {
  static String string = new String();

  static String x;

  static {
    x = "abc";
  }
  public static void repro(int a, String b, boolean c) { }
}
