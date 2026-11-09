package de.upb.sootup.instructions.ref;

public class ArrayRefTest {

  int[] arr;

  boolean[] someMethod() {

    arr = new int[123];
    arr[100] = 42;

    String[] strs = new String[2];
    strs[0] = "zero";

    return new boolean[] {};
  }

}
