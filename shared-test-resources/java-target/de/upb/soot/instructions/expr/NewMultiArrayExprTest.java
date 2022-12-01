package de.upb.sootup.instructions.expr;

public class NewMultiArrayExprTest {

  void sth() {

    int[][] i = new int[42][10];
    int len = i.length;
    System.out.println(len);

  }

  void nonPrimitive() {

    String[][] i = new String[3][10];
    int len = i[0].length;
    System.out.println(len);

  }

}
