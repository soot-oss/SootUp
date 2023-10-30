// cvci/Class.java
package cvci;

class Class implements SInterface{

  public static void main(String[] args){
    Class cls = new Class();
    cls.target();
  }
}

interface Interface {

  default void target(){ }

}
