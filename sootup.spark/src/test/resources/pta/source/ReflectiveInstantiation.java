public class ReflectiveInstantiation {

  static class Widget {
    Widget() {
      touch();
    }

    static void touch() {}
  }

  public static void main(String[] args) throws Exception {
    Class<?> c = Class.forName("ReflectiveInstantiation$Widget");
    Object viaNewInstance = c.newInstance();

    Class<?> c2 = Class.forName("ReflectiveInstantiation$Widget");
    Object viaCtor = c2.getDeclaredConstructor().newInstance();

    Class<?> c3 = Class.forName(args[0]);
    Object viaComputedName = c3.newInstance();
  }
}
