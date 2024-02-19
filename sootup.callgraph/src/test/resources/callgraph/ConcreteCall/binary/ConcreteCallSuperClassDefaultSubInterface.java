// cvc/Class.java
package cvcscsi;

class Class extends SuperClass implements Interface{

  public static void main(String[] args){
    Class cls = new Class();
    cls.target();
  }
}

class SuperClass implements SubInterface {

}

interface Interface {
  default void target(){}
}

interface SubInterface extends Interface {
  default void target(){}
}