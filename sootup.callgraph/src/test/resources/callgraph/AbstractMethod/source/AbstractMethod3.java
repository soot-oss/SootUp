package am3;

class Main  {
  public static void main(String[] args){
    SuperClass a = new Class();
    a.method();
  }
}

abstract class SuperClass {
  public abstract void method();
}
abstract class AbstractClass extends SuperClass{

}
class Class extends AbstractClass{
  public void method(){
  }
}