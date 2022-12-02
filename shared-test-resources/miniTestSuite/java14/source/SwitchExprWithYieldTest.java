class SwitchExprWithYieldTest {
  void switchSomething() {
    int k = 5;
    String s = "";

    // new arrow syntax, will not fall through
    s = switch (k) {
      case 1 -> "single";
      case 2, 3 -> "double";
      default -> "somethingElse";
    };

    // new arrow syntax + code block with new yield statement
    s = switch (k) {
      case 1 -> {
        yield "single";
      }
      case 2, 3 -> "double";
      default -> "somethingElse";
    };

    // old syntax with new yield statement
    s = switch(k) {
      case 1:
        yield "no fall through";
      case 2,3:
        yield "still no fall through";
      default: {
        yield "we will not fall through";
      }
    };

    switch (k) {
      case 1:
        s += "single";
      default:
        s += "somethingElse";
    };

    System.out.println(s);
  }
}