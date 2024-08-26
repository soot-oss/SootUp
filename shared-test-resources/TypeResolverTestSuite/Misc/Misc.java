import java.util.Date;

public class Misc {
  public static void arraysMixedAssignment() {
    int[] a = new int[7];
    a[0] = 0;
    a[1] = 2;
    a[2] = -128;
    a[3] = 128;
    a[4] = -32768;
    a[5] = 32768;
    a[6] = 65536;
  }

  public static void arrayAssignBeforeInit() {
    String[] a = null;
    if (Math.random() == 0.0) {
      // This assignment is before (in source code) the initialization below,
      // so the `TypeResolver` needs to find the type of `a` before a direct assignment to `a`.
      a[0] = null;
      a[0] = "";
    } else {
      a = new String[1];
    }
  }

  public static void nullArray() {
    Object[] a = null;
    a[0] = null;
  }

  public static void objectPrimitiveArray() {
    {
      String[] a = null;
      a[0] = "";
    }
    {
      int[] a = null;
      a[0] = 0;
    }
  }

  public static void useNullArray() {
    Object[] a = null;
    Object b = a[0];
  }

  public static void usePrimitiveNullArray() {
    int[] a = null;
    int b = a[0];
    a = new int[1];
  }

  public static void mixedPrimitiveArray() {
    {
      int a = 65537;
    }
    {
      byte a = 1;
      byte[] b = new byte[1];
      b[0] = a;
    }
  }

  public static void impossibleTyping() {
    {
      boolean a = false;
      useBoolean(a);
    }
    {
      int a = 2;
    }
  }

  public static void testTaAndLnsWithoutLS() {
    int a = 10, c;
    try {
      a = 8 / 0;
      int b = a + 2;
      c = a;
    } catch (ArithmeticException ex) {
    }
    a++;
  }

  public static void testTaAndLnsWithoutLS1() {
    try {
      Object v = 5.00;
      long b = ((Number) v).longValue();
    } catch (Exception ex) {
    }
  }

  public Object testTaAndLnsWithoutLS2(Date date) {
    Date newDate = date;
    if (date == null) {
      return null;
    } else if (date == newDate){
      throw new IllegalArgumentException("Exception");
    }
    return newDate;
  }

  public static void dependentAugmentedInteger1Promotion() {
    int a = 65537;
    {
      boolean b = false;
      useInt(a);
      useBoolean(b);
    }
    {
      int b = 0;
      a = b;
    }
  }

  private static void useInt(int i) {}

  private static void useBoolean(boolean b) {}

  public static void testStringDefaultMethodsTest(){
    String someString = "Some String";
    boolean a = someString.contains("abc");
  }

  public static void arrayTest() {
    {
      double[] a = new double[1];
      a[0] = 0.0;
    }
    {
      String a = "";
    }
  }

  public static void testTAWarningWithoutRTJar() throws Throwable {
    Integer a = null;

    try {
      a = 8 / 0;
      int b = a + 2;
      if (b > 6) {
        throw new IncorrectOperationException("custom exception");
      } else {
        throw new IncorrectFileNameException("custom exception");
      }
    } catch (IncorrectOperationException | IncorrectFileNameException var4) {
      Exception ex = var4;
      System.out.println(ex);
      Throwable e = ex;
      throw e;
    } catch (Throwable var5) {
      Throwable ex = var5;
      System.out.println(ex);
      throw new IncorrectOperationException("custom exception", ex);
    }
  }

}

class IncorrectOperationException extends RuntimeException {
  public IncorrectOperationException(String errorMessage, Throwable t) {
    super(errorMessage, t);
  }

  public IncorrectOperationException(String errorMessage) {
    super(errorMessage);
  }
}

class IncorrectFileNameException extends Exception {
  public IncorrectFileNameException(String errorMessage) {
    super(errorMessage);
  }
}