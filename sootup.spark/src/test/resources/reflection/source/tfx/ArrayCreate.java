package tfx;

import java.lang.reflect.Array;

/** Array.newInstance. */
public class ArrayCreate {
  public static void main(String[] args) throws Exception {
    Object arr = Array.newInstance(Base.class, 3);
    Array.set(arr, 0, new Base());
    Object e = Array.get(arr, 0);
  }
}
