import java.io.IOException;

public class TrapTightenerTarget {
  int field;

  // only the division can throw an ArithmeticException
  int tightenRange(int a, int b) {
    int c = 0;
    try {
      c = a + b;
      c = c / a;
      c = c + 1;
    } catch (ArithmeticException e) {
      c = -1;
    }
    return c;
  }

  // the addition between the divisions can not throw an ArithmeticException
  int splitRange(int a, int b) {
    int c = 0;
    try {
      c = a / b;
      c = c + a;
      c = c / b;
    } catch (ArithmeticException e) {
      c = -1;
    }
    return c;
  }

  // nothing in the try block can throw an ArithmeticException -> the handler is removed
  int noThrowingStmt(int a, int b) {
    int c = 0;
    try {
      c = a + b;
    } catch (ArithmeticException e) {
      c = -1;
    }
    return c;
  }

  // an ArithmeticException is a RuntimeException
  int catchSupertype(int a, int b) {
    int c = 0;
    try {
      c = a + b;
      c = c / a;
    } catch (RuntimeException e) {
      c = -1;
    }
    return c;
  }

  // each handler keeps only the stmts which can throw its exception
  int multipleHandlers(int[] arr, int a, int b) {
    int c = 0;
    try {
      c = a / b;
      arr[0] = c;
      c = c + 1;
    } catch (ArithmeticException e) {
      c = -1;
    } catch (NullPointerException e) {
      c = -2;
    }
    return c;
  }

  // the invoked method can throw any exception
  int invoke(int a) {
    int c = 0;
    try {
      c = a + 1;
      mayThrow();
    } catch (IOException e) {
      c = -1;
    }
    return c;
  }

  // catch-all handlers (e.g. releasing the monitor) are kept as every stmt might throw an Error
  void synchronizedBlock(int a, int b) {
    synchronized (this) {
      field = a + b;
    }
  }

  void mayThrow() throws IOException {}
}
