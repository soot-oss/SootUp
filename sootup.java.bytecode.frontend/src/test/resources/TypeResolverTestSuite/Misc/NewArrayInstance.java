import java.lang.reflect.Array;

public class NewArrayInstance {
  public static void entry() {
    Object array = Array.newInstance(Integer.TYPE, 10);
    int[] arrayCast = (int[]) array;
    System.out.println(array + "" +arrayCast.length );
  }
}