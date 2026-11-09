/** Locals redefined inside try blocks, for LocalSplitterTest. */
public class LocalSplitterExceptionsTarget {

  static class Node {
    Node next;
  }

  static void work() {}

  /** The field read can throw before it overwrites n: the handler sees the n passed in. */
  Object fieldRead(Node n) {
    try {
      n = n.next;
    } catch (NullPointerException e) {
      return n;
    }
    return n;
  }

  /** As fieldRead, but work() can throw after the overwrite: the handler sees either n. */
  Object fieldReadThenCall(Node n) {
    try {
      n = n.next;
      work();
    } catch (RuntimeException e) {
      return n;
    }
    return n;
  }

  /** a = 2 cannot throw, and no RuntimeException is asynchronous: the handler sees 2. */
  int narrowHandler() {
    int a = 1;
    try {
      a = 2;
      work();
    } catch (RuntimeException e) {
      return a;
    }
    return a;
  }

  /**
   * The same with catch (Error): a VirtualMachineError can be thrown asynchronously before a = 2
   * completes, so the handler sees 1 or 2.
   */
  int errorHandler() {
    int a = 1;
    try {
      a = 2;
      work();
    } catch (Error e) {
      return a;
    }
    return a;
  }

  /** a / 2.0f cannot throw: the handler is only entered from work(), after the division. */
  float floatDivisionByConstant(float a) {
    try {
      a = a / 2.0f;
      work();
    } catch (RuntimeException e) {
      return a;
    }
    return a;
  }

  /** The divisor is a local, whose type cannot be trusted before typing: the division can throw. */
  float floatDivisionByLocal(float a, float b) {
    try {
      a = a / b;
      work();
    } catch (RuntimeException e) {
      return a;
    }
    return a;
  }
}
