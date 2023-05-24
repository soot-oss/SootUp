// ccm/Class.java
package cic;

class Class {

  static SuperClass sc= new SubClass();
  public static void main(String[] args){
    sc.method();
  }
}

class SuperClass {
  public void method() {
  }
}

class SubClass extends SuperClass {
  public void method() {
  }
}
