package de.upb.sootup.instructions.expr;

public class VirtualInvokeExprTest {

  int postalcode(int x) {
    return 33100 + x;
  }

  void invoke() {

    int code = postalcode(0);
    System.out.println(code);

  }

}
