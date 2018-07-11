package de.upb.soot.operators;

public class Operators {
  // Addition
  public void addition(int a, int b) {
    int d = a + b;
    System.out.println(d);
  }

  // Subtraction
  public void subtraction(int a, int b) {
    int d = b - a;
    System.out.println(d);
  }

  // Multiplication
  public void multiplication(int a, int b) {
    int d = b * a;
    System.out.println(d);
  }

  // Division
  public void division(int a, int b) {
    int d = b / a;
    System.out.println(d);
  }

  // Modulus
  public void modulus(int a, int b) {
    int d = b % a;
    System.out.println(d);
  }

  // Increment
  public void increment(int a, int b) {
    int d = a++;
    System.out.println(d);
  }

  // Decrement
  public void decrement(int a, int b) {
    int d = a--;
    System.out.println(d);
  }

  // Simple assignment operator
  public void simpleAssignmentOperator(int a) {
    int d = a;
    System.out.println(d);
  }

  // Add AND assignment operator
  public void addAssignmentOperator(int a) {
    int d = 0;
    d += a;
    System.out.println(d);
  }

  // Subtract AND assignment operator
  public void subtractAssignmentOperator(int a) {
    int d = 0;
    d -= a;
  }

  // Multiply AND assignment operator
  public void multiplyAssignmentOperator(int a) {
    int d = 0;
    d *= a;
    System.out.println(d);
  }

  // Divide AND assignment operator
  public void divideAssignmentOperator(int a) {
    int d = 0;
    d /= a;
    System.out.println(d);
  }

  // Modulus AND assignment operator
  public void modulusAssignmentOperator(int a) {
    int d = 0;
    d %= a;
    System.out.println(d);
  }

  // Left shift AND assignment operator
  public void leftShiftAssignmentOperator(int a) {
    int d = 0;
    d <<= 2;
    System.out.println(d);
  }

  // Right shift AND assignment operator
  public void rightShiftAssignmentOperator(int a) {
    int d = 0;
    d >>= 2;
    System.out.println(d);
  }

  // Bitwise AND assignment operator
  public void bitwiseAND(int a) {
    int d = 0;
    d &= 2;
    System.out.println(d);
  }

  // Bitwise XOR and assignment operator
  public void bitwiseXOR(int a) {
    int d = 0;
    d ^= 2;
    System.out.println(d);
  }

  // Bitwise inclusive OR and assignment operator
  public void bitwiseIncORAssignmentOperator(int a) {
    int d = 0;
    d |= 2;
    System.out.println(d);
  }

  public void relationalEqualTo(int a, int b) {
    // Equal to
    boolean result = (a == b);
    System.out.println(result);
  }

  public void relationalNotEqualTo(int a, int b) {
    // Not equal to
    boolean result = (a != b);
    System.out.println(result);
  }

  public void relationalGreaterThan(int a, int b) {
    // Greater than
    boolean result = (a > b);
    System.out.println(result);
  }

  public void relationalLessThan(int a, int b) {
    // Less than
    boolean result = (a < b);
    System.out.println(result);
  }

  public void relationalGreaterThanEqualTo(int a, int b) {
    // Greater than equal to
    boolean result = (a >= b);
    System.out.println(result);
  }

  public void relationalLessThanEqualTo(int a, int b) {
    // Less than equal to
    boolean result = (a <= b);
    System.out.println(result);
  }

  public void logicalOR(int a, int b) {
    // Logical OR
    boolean result = (a == b || a > b);
    System.out.println(result);
  }

  public void logicalAND(int a, int b) {
    // Logical AND
    boolean result = (a == b && a > b);
    System.out.println(result);
  }

  public void logicalNOT(int a, int b) {
    // Logical NOT
    boolean result = !(a == b);
    System.out.println(result);
  }

  public void bitwiseAND(int a, int b) {
    // Bitwise AND
    int d = a & b;
    System.out.println(d);
  }

  public void bitwiseOR(int a, int b) {
    // Bitwise OR
    int d = a | b;
    System.out.println(d);
  }

  public void bitwiseXOR(int a, int b) {
    // Bitwise XOR
    int d = a ^ b;
    System.out.println(d);
  }

  public void bitwiseCompliment(int a, int b) {
    // Bitwise compliment
    int d = ~a;
    System.out.println(d);
  }

  public void bitwiseLeftShift(int a, int b) {
    // Left shift
    int d = a << 2;
    System.out.println(d);
  }

  public void bitwiseRightShift(int a, int b) {
    // Right shift
    int d = a >> 2;
    System.out.println(d);
  }

  public void bitwiseRightShiftZerofill(int a, int b) {
    // Shift right zero fill
    int d = a >>> 2;
    System.out.println(d);
  }

  // Other miscellaneous operators
  public void conditionalOperator(int a) {
    // Ternary operator
    int d = (a == 10) ? 50 : 70;
    d = (a == 0) ? 50 : 70;
    System.out.println(d);
  }

  public void instanceofOperator() {
    String name = "Java";
    boolean result = name instanceof String;
    System.out.println(result);
  }
}
