package tfx;

/** getMethod on Child finds a method declared only in Parent. */
public class InheritedGetMethod {
  public static void main(String[] args) throws Exception {
    Object r = Child.class.getMethod("inherited").invoke(new Child());
  }
}
