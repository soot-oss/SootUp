// cvc/Class.java
package cvcscincomplete;

class Class extends SuperClass{

  public static void main(String[] args){
    Class cls = new Class();
    cls.target();
  }
}

//class file of the superclass is not in the inputlocation
class SuperClass {

  public void target(){ }

}
