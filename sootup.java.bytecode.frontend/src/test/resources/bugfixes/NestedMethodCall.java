public class NestedMethodCall {
  public void nestedMethodCall() {
    int i = 0;
    String s = "abc";
    decode(s.charAt(++i), s.charAt(i++));
  }

  void decode(char first, char second) {}
}
