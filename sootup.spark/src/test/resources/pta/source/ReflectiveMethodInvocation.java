public class ReflectiveMethodInvocation {

  static class Widget {
    Widget(int seed) {
      ctorTouch();
    }

    void doWork() {
      workTouch();
    }

    private void secretWork() {
      secretTouch();
    }

    static void ctorTouch() {}

    static void workTouch() {}

    static void secretTouch() {}
  }

  public static void main(String[] args) throws Exception {
    Class<?> c = Class.forName("ReflectiveMethodInvocation$Widget");
    java.lang.reflect.Constructor<?> ctor = c.getConstructor(int.class);
    Object widget = ctor.newInstance(42);

    java.lang.reflect.Method m = c.getMethod("doWork");
    m.invoke(widget);

    java.lang.reflect.Method secret = c.getDeclaredMethod("secretWork");
    secret.invoke(widget);

    java.lang.reflect.Method computed = c.getMethod(args[0]);
    computed.invoke(widget);
  }
}
