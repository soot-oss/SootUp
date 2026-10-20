package tfx;

public class VarargsTarget {
  public static int count(String... parts) {
    return parts.length;
  }
}
