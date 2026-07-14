/**
 * @author Linghui Luo
 * @see https://docs.oracle.com/javase/7/docs/technotes/guides/language/catch-multiple.html
 */
public class CatchMultipleExceptionTypes {

  public void test(int i, int[] arr) {
    try {
      int num = 100;
      int a = 100 / i;
      int b = arr[i];
      System.out.println(a + b);
    } catch (ArithmeticException | IndexOutOfBoundsException ex) {
      throw ex;
    }
  }
}
