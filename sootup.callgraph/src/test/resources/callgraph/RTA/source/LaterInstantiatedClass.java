// ccm/Class.java
package lic;

class Class {

  public static void main(String[] args){
    later().method();
  }
  public static InstantiatedClass later(){
    return new InstantiatedClass();
  }
}

class InstantiatedClass {
  public void method() {
  }
}
