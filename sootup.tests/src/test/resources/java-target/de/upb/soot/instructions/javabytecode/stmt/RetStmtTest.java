package de.upb.sootup.instructions.javabytecode.stmt;

public class RetStmtTest {

  int returnInt(int a) {
    return a;
  }

  double returnDouble(double b) {
    return b;
  }

  String returnString(String s) {
    return s;
  }

  RetStmtTest returnNonPrimitive(RetStmtTest r) {
    return r;
  }

}
