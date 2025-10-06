package de.upb.sootup.concrete.arrays;

public class Arrays {

  public void primitiveArray() {
    int[] arr = new int[] { 1, 2 };
    System.out.println(arr[0]);
    System.out.println(arr[1]);
  }

  public void primitiveArrayLengt() {
    int[] arr = new int[] { 1, 2 };
    System.out.println(arr.length);
  }

  public void objectArray() {
    String[] arr = new String[] { "1", "2" };
    System.out.println(arr[0]);
    System.out.println(arr[1]);
  }

  public void manualAssignment() {
    int[] arr = new int[1];
    arr[0] = 1;
    System.out.println(arr[0]);
  }

  public void twoDimensions() {
    int[][] arr = new int[2][2];
    arr[0][0] = 1;
    arr[0][1] = 2;
    arr[1][0] = 3;
    arr[1][1] = 4;
    System.out.println(arr[0][0]);
    System.out.println(arr[0][1]);
    System.out.println(arr[1][0]);
    System.out.println(arr[1][1]);
  }

}
