// cvci/Class.java
package cvcsi;

class Class implements SubInterface{

  public static void main(String[] args){
    Class cls = new Class();
    cls.target();
  }
}

interface Interface {

  default void target(){ }

}

interface SubInterface extends Interface {

  default void target(){ }

}
