package de.upb.soot.Operators;

public class Operators {
  // Addition
  public void addition(int a, int b) {
    int d = a + b;
  }

  // Subtraction
  public void subtraction(int a, int b) {
    int d = b - a;
  }

  // Multiplication
  public void multiplication(int a, int b) {
    int d = b * a;
  }

  // Division
  public void division(int a, int b) {
    int d = b / a;
  }

  // Modulus
  public void modulus(int a, int b) {
    int d = b % a;
  }

  // Increment
  public void increment(int a, int b) {
    int d = a++;
  }

  // Decrement
  public void decrement(int a, int b) {
    int d = a--;
  }

  // Simple assignment operator
  public void simple_assignment_operators(int a) {
    int d = a;
  }

  // Add AND assignment operator
  public void add_assignment_operator(int a) {
    int d = 0;
    d += a;
  }

  // Subtract AND assignment operator
  public void subtract_assignment_operator(int a) {
    int d = 0;
    d -= a;
  }

  // Multiply AND assignment operator
  public void multiply_assignment_operator(int a) {
    int d = 0;
    d *= a;
  }

  // Divide AND assignment operator
  public void divide_assignment_operator(int a) {
    int d = 0;
    d /= a;
  }

  // Modulus AND assignment operator
  public void modulus_asssignment_operator(int a) {
    int d = 0;
    d %= a;
  }

  // Left shift AND assignment operator
  public void left_shift_assignment_operator(int a) {
    int d = 0;
    d <<= 2;
  }

  // Right shift AND assignment operator
  public void right_shift_assignment_operator(int a) {
    int d = 0;
    d >>= 2;
  }

  // Bitwise AND assignment operator
  public void bitwise_and(int a) {
    int d = 0;
    d &= 2;
  }

  // Bitwise XOR and assignment operator
  public void bitwise_xor(int a) {
    int d = 0;
    d ^= 2;
  }

  // Bitwise inclusive OR and assignment operator
  public void bitwise_inc_or_assignment_operator(int a) {
    int d = 0;
    d |= 2;
  }

  public void relational_equal_to(int a, int b) {
    // Equal to
    boolean result = (a == b);
  }

  public void relational_not_equal_to(int a, int b) {
    // Not equal to
   boolean result = (a != b);
  }

  public void relational_greater_than(int a, int b) {
    // Greater than
    boolean result = (a > b);
  }

  public void relational_less_than(int a, int b) {
    // Less than
    boolean result = (a < b);
  }

  public void relational_greater_than_equalto(int a, int b) {
    // Greater than equal to
    boolean result = (a >= b);
  }

  public void relational_less_than_equalto(int a, int b) {
    // Less than equal to
    boolean result = (a <= b);
  }

  public void logical_OR(int a, int b) {
    // Logical OR
    boolean result = (a == b || a > b);
  }

  public void logical_AND(int a, int b) {
    // Logical AND
    boolean result = (a == b && a > b);
  }

  public void logical_NOT(int a, int b) {
    // Logical NOT
    boolean result = !(a == b);
  }

  public void bitwise_AND(int a, int b) {
    // Bitwise AND
    int d = a & b;
  }

  public void bitwise_OR(int a, int b) {
    // Bitwise OR
    int d = a | b;
  }

  public void bitwise_XOR(int a, int b) {
    // Bitwise XOR
    int d = a ^ b;
  }

  public void bitwise_compliment(int a, int b) {
    // Bitwise compliment
    int d = ~a;
  }

  public void bitwise_left_shift(int a, int b) {
    // Left shift
    int d = a << 2;
  }

  public void bitwise_right_shift(int a, int b) {
    // Right shift
    int d = a >> 2;
  }

  public void bitwise_right_shift_zerofill(int a, int b) {
    // Shift right zero fill
    int d = a >>> 2;
  }

  // Other miscellaneous operators
  public void conditional_operator(int a) {
    // Ternary operator
    int d = (a == 10) ? 50 : 70;
    d = (a == 0) ? 50 : 70;
  }

  public void instanceof_operator() {
    String name = "Java";
    boolean result = name instanceof String;
  }
}
