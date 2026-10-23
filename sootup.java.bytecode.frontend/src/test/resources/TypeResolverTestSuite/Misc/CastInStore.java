/**
 * Stores whose value needs a cast: in each method the two blocks reuse one local slot, typed `int`
 * by the first block, so storing it as a `byte` needs `(byte)`. Jimple only allows a local or a
 * constant as the stored value, so the cast has to go into a temp local.
 */
public class CastInStore {
  static byte staticField;
  byte instanceField;

  public static void staticFieldStore() {
    {
      int a = 65537;
    }
    {
      byte a = 1;
      staticField = a;
    }
  }

  public void instanceFieldStore() {
    {
      int a = 65537;
    }
    {
      byte a = 1;
      instanceField = a;
    }
  }

  public static void arrayStore() {
    {
      int a = 65537;
    }
    {
      byte a = 1;
      byte[] b = new byte[1];
      b[0] = a;
    }
  }
}
