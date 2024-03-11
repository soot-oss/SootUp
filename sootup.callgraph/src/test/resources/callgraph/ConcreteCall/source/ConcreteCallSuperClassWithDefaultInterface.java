// cvc/Class.java
package cvcscwi;

class Class extends SuperClass implements Interface{

  public static void main(String[] args){
    Class cls = new Class();
    cls.target();
  }
}

class SuperClass {

  public void target(){ }

}

class Interface {

  default void target(){ }

}