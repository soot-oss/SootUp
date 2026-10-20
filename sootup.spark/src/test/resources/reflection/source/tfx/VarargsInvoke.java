package tfx;

/** Target takes varargs; reflective arg array wraps a String[]. */
public class VarargsInvoke {
  public static void main(String[] args) throws Exception {
    Object n =
        VarargsTarget.class
            .getMethod("count", String[].class)
            .invoke(null, (Object) new String[] {"a", "b"});
  }
}
