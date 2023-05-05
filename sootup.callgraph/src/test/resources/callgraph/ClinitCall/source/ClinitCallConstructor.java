// ccm/Class.java
package ccc;

class Class {

  public static void main(String[] args){
    DirectType a=new DirectType();
    int[] b= new int[5];
    ArrayType[] c =new ArrayType[5];
    ArrayDimType[][] d= new ArrayDimType[2][4];
    ArrayInArrayType[][] e= new ArrayInArrayType[2][];
  }
}

class DirectType {
  static int a=3;
}

class ArrayType {
  static int a=3;
}

class ArrayDimType {
  static int a=3;
}
class ArrayInArrayType {
  static int a=3;
}
